package com.example.progrest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderEmail; // 📌 ดึงจาก JWT Token

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath; // ✅ เก็บ URL หรือ Path ของไฟล์

    @Column(nullable = false)
    private String fileType; // ✅ เก็บประเภทของไฟล์ (เช่น pdf, image/png)

    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status = FileStatus.PENDING;

    public String getUploadDate() {
      return uploadDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
  }
}
