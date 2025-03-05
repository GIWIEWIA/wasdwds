package com.example.progrest.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ดึง Header ที่มี JWT Token
        String authorizationHeader = request.getHeader("Authorization");
        LOGGER.info("🔍 JWT Header Received: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            LOGGER.warn("❌ JWT Token not found or invalid format!");
            filterChain.doFilter(request, response);
            return;
        }

        // ดึง Token ออกจาก Header
        String token = authorizationHeader.substring(7);

        try {
            // ตรวจสอบว่า Token เป็น null หรือไม่ ก่อนใช้งาน
            if (token == null || token.isEmpty()) {
                LOGGER.warn("⚠️ JWT Token is empty!");
                filterChain.doFilter(request, response);
                return;
            }

            // ดึง email จาก JWT Token
            String email = jwtUtil.extractEmail(token);
            LOGGER.info("🔑 Extracted User from Token: {}", email);

            // ตรวจสอบว่ามีการ Authenticate แล้วหรือยัง
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // ตรวจสอบความถูกต้องของ Token
                if (jwtUtil.validateToken(token, email)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // กำหนด Authentication ให้ Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    LOGGER.info("✅ Authentication successful for user: {}", email);
                } else {
                    LOGGER.warn("⚠️ JWT validation failed for user: {}", email);
                }
            }
        } catch (ExpiredJwtException e) {
            LOGGER.error("❌ JWT Token expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT Token expired, please use refresh token");
            return;
        } catch (MalformedJwtException | SecurityException e) {
            LOGGER.error("❌ Invalid JWT Token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token");
            return;
        } catch (Exception e) {
            LOGGER.error("❌ Unexpected error in JWT filter: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ✅ สร้างเมธอดสำหรับส่ง Error Response เพื่อลดความซ้ำซ้อน
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
