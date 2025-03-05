package com.example.progrest.exception;

public class UserNotFoundException extends RuntimeException {
    public static final String USER_NOT_FOUND = "User not found";  // กำหนดค่าคงที่

    public UserNotFoundException() {
        super(USER_NOT_FOUND);  // ใช้ค่าคงที่ใน constructor
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
