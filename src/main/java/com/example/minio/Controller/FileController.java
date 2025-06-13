package com.example.minio.Controller;

import com.example.minio.service.MinioService;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    /**
     * Upload file
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "objectName", required = false) String objectName) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            // Tạo object name nếu không được cung cấp
            if (objectName == null || objectName.trim().isEmpty()) {
                objectName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            }

            String fileUrl = minioService.uploadFile(file, objectName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("objectName", objectName);
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Download file
     */
    @GetMapping("/download/{objectName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String objectName) {
        try {
            InputStream inputStream = minioService.downloadFile(objectName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + objectName + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Download failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Xóa file
     */
    @DeleteMapping("/delete/{objectName}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String objectName) {
        try {
            minioService.deleteFile(objectName);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách files
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        try {
            List<String> files = minioService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("List files failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    /**
     * Lấy URL của file
     */
    @GetMapping("/url/{objectName}")
    public ResponseEntity<Map<String, String>> getFileUrl(@PathVariable String objectName) {
        try {
            String fileUrl = minioService.getFileUrl(objectName);
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            log.error("Get URL failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Get URL failed: " + e.getMessage()));
        }
    }
}