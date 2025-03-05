package com.example.progrest.controller;

import com.example.progrest.entity.FileEntity;
import com.example.progrest.entity.FileStatus;
import com.example.progrest.exception.FileDeletionException;
import com.example.progrest.exception.FileOperationException; // ✅ Import Custom Exception
import com.example.progrest.service.FileService;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.HttpStatus;




import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:4200") // ✅ เปิดให้ Frontend เรียก API ได้
public class FileController {

    private final FileService fileService;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class); // ✅ ประกาศ Logger

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // ✅ ดึงไฟล์ทั้งหมด
    @GetMapping("/all")
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    // ✅ ดึงไฟล์ของผู้ใช้ที่ล็อกอิน
    @GetMapping("/user")
    public ResponseEntity<List<FileEntity>> getUserFiles(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("❌ Unauthorized access to /user API");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();
        logger.info("🔍 Fetching files for user: {}", email);

        List<FileEntity> files = fileService.getFilesBySender(email);
        return ResponseEntity.ok(files);
    }

    // ✅ อัปโหลดไฟล์
    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("senderEmail") String senderEmail,
            @RequestParam("fileType") String fileType) {
        try {
            logger.info("📩 Received senderEmail from Frontend: {}", senderEmail);

            FileEntity uploadedFile = fileService.uploadFile(file, senderEmail, fileType);
            logger.info("✅ File successfully uploaded and confirmed: {}", uploadedFile.getStatus());

            return ResponseEntity.ok(uploadedFile); // ✅ ส่งไฟล์ที่มี status=CONFIRMED กลับไป
        } catch (Exception e) {
            logger.error("❌ Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }






    // ✅ อัปเดตสถานะไฟล์
    @PutMapping("/update-status/{fileId}")
    public ResponseEntity<FileEntity> updateFileStatus(
            @PathVariable Long fileId,
            @RequestBody FileStatus status) {
        try {
            FileEntity updatedFile = fileService.updateFileStatus(fileId, status);
            return ResponseEntity.ok(updatedFile);
        } catch (FileOperationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ ลบไฟล์
  // ✅ ลบไฟล์
  @DeleteMapping("/{fileId}")
  public ResponseEntity<Void> deleteFile(@PathVariable("fileId") Long fileId) {
      try {
          fileService.deleteFile(fileId);
          return ResponseEntity.ok().build();
      } catch (FileDeletionException e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }







    // ✅ จัดการ Exception ที่เกิดขึ้น
    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<String> handleFileOperationException(FileOperationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
