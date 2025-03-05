package com.example.progrest.config;

import com.example.progrest.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.info("🔒 Configuring Security...");

        http
            .csrf(csrf -> csrf.disable()) // ❌ Disable CSRF (Only for stateless APIs)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ Enable CORS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ Allow CORS Preflight
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll() // ✅ Public Auth APIs
                .requestMatchers(HttpMethod.DELETE, "/api/files/**").authenticated() // ✅ Allow DELETE
                .requestMatchers("/api/files/**", "/api/files/user").authenticated() // ✅ Files API Require Authentication
                .anyRequest().authenticated() // ✅ Other Requests Require Authentication
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ Stateless Session (No Cookies)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // ✅ Add JWT Filter Before Default Auth Filter

        LOGGER.info("🔐 Security Configuration Completed.");
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        LOGGER.info("🌍 Configuring CORS...");

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:4200" // ✅ Allow Frontend Development
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization")); // ✅ Expose JWT Header

        // ⚠️ If your frontend needs credentials, set this to true
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
