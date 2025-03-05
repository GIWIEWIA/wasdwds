package com.example.progrest.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username; // ใช้แค่ตอนรับข้อมูล ไม่ต้องมี ID
    private String email;
    private String password;
}
