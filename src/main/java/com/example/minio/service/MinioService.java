package com.example.minio.service;

import com.example.minio.config.MinioConfigProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;

    /**
     * Kiểm tra bucket có tồn tại không
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            String bucket = minioConfigProperties.getBucketName();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());

            // Tạo mới khi 0 ton tai
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());

                log.info("Bucket '{}' created successfully", bucket);
            }

        } catch (Exception e) {
            log.error("Error initializing Minio Bucket", e);
        }
    }

    /**
     * Upload file lên MinIO
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfigProperties.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("File {} uploaded successfully", objectName);

            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Could not upload file", e);
        }
    }

    /**
     * Download file
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfigProperties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            throw new RuntimeException("Could not download file", e);
        }
    }

    /**
     * Xóa file
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfigProperties.getBucketName())
                    .object(objectName)
                    .build());

            log.info("File {} deleted successfully", objectName);

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new RuntimeException("Could not delete file", e);
        }
    }

    /**
     * Lấy danh sách files
     */
    public List<String> listFiles() {
        try {
            List<String> files = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfigProperties.getBucketName())
                            .build());

            for (Result<Item> result : results) {
                files.add(result.get().objectName());
            }
            return files;
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage());
            throw new RuntimeException("Could not list files", e);
        }
    }

    /**
     * Lấy URL của file
     */
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfigProperties.getBucketName())
                    .object(objectName)
                    .expiry(60, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Error getting file URL: {}", e.getMessage());
            throw new RuntimeException("Could not get file URL", e);
        }
    }

    /**
     * Lấy thông tin file
     */
    public StatObjectResponse getFileInfo(String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfigProperties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Error getting file info: {}", e.getMessage());
            throw new RuntimeException("Could not get file info", e);
        }
    }
}
