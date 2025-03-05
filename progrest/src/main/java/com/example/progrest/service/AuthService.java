package com.example.progrest.service;

import com.example.progrest.entity.Person;
import com.example.progrest.repository.PersonRepository;
import com.example.progrest.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final PersonRepository personRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_BLOCK_TIME = 5 * 60 * 1000L; // 5 นาที

    private final Map<String, Integer> loginAttempts = new HashMap<>();
    private final Map<String, Long> blockedUntil = new HashMap<>();

    public AuthService(PersonRepository personRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ ลงทะเบียนผู้ใช้ใหม่
    public ResponseEntity<String> register(Person person) {
        logger.info("📝 Registering user: {}", person.getEmail());

        if (person.getEmail() == null || person.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "กรุณากรอกอีเมลและรหัสผ่านให้ครบ");
        }

        if (personRepository.existsByEmail(person.getEmail().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "อีเมลนี้ถูกใช้ไปแล้ว");
        }

        person.setPassword(passwordEncoder.encode(person.getPassword().trim()));
        personRepository.save(person);
        return ResponseEntity.ok("สมัครสมาชิกสำเร็จ!");
    }

    // ✅ เข้าสู่ระบบ (Login)
    public Map<String, String> login(Person person) {
      String email = person.getEmail().trim();
      String password = person.getPassword().trim();

      logger.info("🔍 Checking login for: {}", email);

      if (isAccountLocked(email)) {
          throw new ResponseStatusException(HttpStatus.LOCKED, "บัญชีถูกล็อก กรุณาลองใหม่ใน 5 นาที");
      }

      Optional<Person> userOpt = personRepository.findByEmail(email);
      if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
          increaseLoginAttempts(email);
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
      }

      loginAttempts.remove(email);
      blockedUntil.remove(email);

      String accessToken = jwtUtil.generateAccessToken(email);
      String refreshToken = jwtUtil.generateRefreshToken(email);

      // ✅ คืนค่า email ไปพร้อมกับ Token
      return Map.of(
          "accessToken", accessToken,
          "refreshToken", refreshToken,
          "email", email  // ✅ เพิ่ม email ลงใน response
      );
  }


    // ✅ ออก Token ใหม่ (Refresh Token)
    public Map<String, String> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token ไม่ถูกต้อง");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // ✅ ออกจากระบบ (Logout) - ลบ Refresh Token ออกจากฐานข้อมูล (ถ้าใช้ DB)
    public ResponseEntity<String> logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh Token is required");
        }

        // ในกรณีใช้ฐานข้อมูลเก็บ Refresh Token ต้องทำให้ Token ใช้ไม่ได้อีก
        return ResponseEntity.ok("ออกจากระบบสำเร็จ!");
    }

    // ✅ ลบบัญชีผู้ใช้ (Delete Account)
    public ResponseEntity<String> deleteUser(String email) {
        Optional<Person> user = personRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบผู้ใช้");
        }

        personRepository.delete(user.get());
        return ResponseEntity.ok("ลบบัญชีสำเร็จ");
    }

    // ✅ ดึงข้อมูลผู้ใช้ (Get Profile)
    public Person getUserProfile(String email) {
        return personRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบผู้ใช้"));
    }

    // ✅ อัปเดตรหัสผ่าน (Update Password)
    public ResponseEntity<String> updatePassword(String email, String oldPassword, String newPassword) {
        Optional<Person> userOpt = personRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบผู้ใช้");
        }

        Person user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "รหัสผ่านเก่าไม่ถูกต้อง");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        personRepository.save(user);

        return ResponseEntity.ok("อัปเดตรหัสผ่านสำเร็จ");
    }

    // ✅ ฟังก์ชันช่วยเหลือ
    private void increaseLoginAttempts(String email) {
        int attempts = loginAttempts.getOrDefault(email, 0) + 1;
        loginAttempts.put(email, attempts);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockedUntil.put(email, System.currentTimeMillis() + LOGIN_BLOCK_TIME);
            throw new ResponseStatusException(HttpStatus.LOCKED, "บัญชีถูกล็อก กรุณาลองใหม่ใน 5 นาที");
        }
    }

    private boolean isAccountLocked(String email) {
        Long lockTime = blockedUntil.get(email);
        if (lockTime == null) return false;
        if (System.currentTimeMillis() > lockTime) {
            blockedUntil.remove(email);
            loginAttempts.remove(email);
            return false;
        }
        return true;
    }
}
