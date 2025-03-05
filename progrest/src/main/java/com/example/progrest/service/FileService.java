package com.example.progrest.service;

import com.example.progrest.entity.FileEntity;
import com.example.progrest.entity.FileStatus;
import com.example.progrest.exception.FileDeletionException;
import com.example.progrest.exception.FileOperationException;
import com.example.progrest.repository.FileRepository;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final String UPLOAD_DIR = "D:/data/"; // 📂 กำหนดโฟลเดอร์อัปโหลดไฟล์
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final String ckanUrl = "http://192.168.10.107/api/3/action"; // 🔗 ใช้ CKAN Instance ของคุณ
    private final String apiKey = "YOUR_CKAN_API_KEY"; // 🔑 ใส่ API Key ที่ได้จาก CKAN
    private final RestTemplate restTemplate = new RestTemplate();

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // ✅ ดึงไฟล์ทั้งหมดจากระบบ
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    // ✅ ดึงไฟล์ทั้งหมดของผู้ใช้
    public List<FileEntity> getFilesBySender(String senderEmail) {
        return fileRepository.findBySenderEmail(senderEmail);
    }

    // ✅ อัปโหลดไฟล์
    public FileEntity uploadFile(MultipartFile file, String senderEmail, String fileType) {
        try {
            logger.info("📩 Upload request received for user: {}", senderEmail);

            // 1️⃣ **บันทึกไฟล์ลงเครื่องเซิร์ฟเวอร์**
            String fileName = file.getOriginalFilename();
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(fileName);

            // ป้องกันไฟล์ซ้ำโดยเช็คก่อนบันทึก
            int count = 1;
            while (Files.exists(targetLocation)) {
                fileName = file.getOriginalFilename().replace(".", "_" + count + ".");
                targetLocation = Paths.get(UPLOAD_DIR).resolve(fileName);
                count++;
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 2️⃣ **สร้าง Dataset บน CKAN (ถ้ายังไม่มี)**
            String datasetId = ensureDatasetExists(senderEmail);

            // 3️⃣ **อัปโหลดไฟล์ไปที่ CKAN**
            createResourceOnCKAN(datasetId, targetLocation.toString(), fileName, fileType);

            // 4️⃣ **บันทึกข้อมูลไฟล์ลง Database**
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFilePath(targetLocation.toString());
            fileEntity.setFileType(fileType);
            fileEntity.setSenderEmail(senderEmail);
            fileEntity.setUploadDate(LocalDateTime.now());
            fileEntity.setStatus(FileStatus.CONFIRMED);

            fileRepository.save(fileEntity);
            logger.info("✅ File uploaded & CKAN resource created: {}", fileName);

            return fileEntity;
        } catch (IOException e) {
            throw new FileOperationException("❌ Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }

    // ✅ ตรวจสอบว่ามี Dataset บน CKAN หรือไม่ (ถ้าไม่มีให้สร้าง)
    private String ensureDatasetExists(String datasetName) {
        String formattedName = datasetName.toLowerCase().replace(" ", "_");
        String datasetCheckUrl = ckanUrl + "/package_show?id=" + formattedName;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(datasetCheckUrl, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            if (jsonResponse.getBoolean("success")) {
                return jsonResponse.getJSONObject("result").getString("id"); // ✅ คืนค่า dataset_id
            }
        } catch (Exception e) {
            logger.info("❌ Dataset ไม่พบ, กำลังสร้างใหม่: {}", formattedName);
        }

        // 🔹 **สร้าง Dataset ใหม่บน CKAN**
        String datasetCreateUrl = ckanUrl + "/package_create";
        String datasetJson = "{ \"name\": \"" + formattedName + "\", \"title\": \"" + datasetName + "\" }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<String> request = new HttpEntity<>(datasetJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(datasetCreateUrl, HttpMethod.POST, request, String.class);

        JSONObject responseJson = new JSONObject(response.getBody());
        return responseJson.getJSONObject("result").getString("id"); // ✅ คืนค่า dataset_id
    }

    // ✅ อัปโหลด Resource ไปยัง CKAN และลิงก์ไปยังไฟล์ในเครื่อง
    private void createResourceOnCKAN(String datasetId, String filePath, String fileName, String fileType) {
        String url = ckanUrl + "/resource_create";
        String fileUrl = "file://" + filePath;

        String resourceJson = "{ \"package_id\": \"" + datasetId + "\", " +
                "\"name\": \"" + fileName + "\", \"format\": \"" + fileType + "\", " +
                "\"url\": \"" + fileUrl + "\" }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<String> request = new HttpEntity<>(resourceJson, headers);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    // ✅ อัปเดตสถานะไฟล์
    public FileEntity updateFileStatus(Long fileId, FileStatus newStatus) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileOperationException("File not found: " + fileId));
        file.setStatus(newStatus);
        return fileRepository.save(file);
    }

    // ✅ ลบไฟล์
    public void deleteFile(Long fileId) throws FileDeletionException {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> {
                    logger.error("❌ File not found with ID: {}", fileId);
                    return new FileDeletionException("File not found with ID: " + fileId);
                });

        // ✅ Construct file path
        Path filePath = Paths.get(fileEntity.getFilePath());

        // ✅ Delete the file using java.nio.file.Files
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                logger.info("🗑️ File deleted: {}", filePath);
            } catch (IOException e) {
                logger.error("❌ Failed to delete file: {}", filePath, e);
                throw new FileDeletionException("Failed to delete file: " + filePath, e);
            }
        } else {
            logger.warn("⚠️ File not found in folder, but deleting from DB.");
        }

        // ✅ Remove file metadata from database
        fileRepository.delete(fileEntity);
        logger.info("✅ File metadata removed from DB for ID: {}", fileId);
    }
}
