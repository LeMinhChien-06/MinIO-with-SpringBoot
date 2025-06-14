package com.example.minio.dto;

import lombok.Data;

@Data
public class FileUploadDto {
    private String url;
    private String ext;
    private String name;
}
