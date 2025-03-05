package com.example.progrest.controller;

import com.example.progrest.entity.Person;
import com.example.progrest.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody Person person) {
    return authService.register(person); // ✅ ลบ try-catch เพราะใช้ GlobalExceptionHandler แล้ว
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> login(@RequestBody Person person) {
    return ResponseEntity.ok(authService.login(person));
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
    return ResponseEntity.ok(authService.refreshToken(request.get("refreshToken")));
  }

  @GetMapping("/a")
  public ResponseEntity<String> getProtectedData() {
    return ResponseEntity.ok("✅ You have successfully accessed a protected API with JWT!");
  }


}
