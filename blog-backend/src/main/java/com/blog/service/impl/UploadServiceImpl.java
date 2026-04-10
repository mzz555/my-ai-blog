package com.blog.service.impl;

import com.blog.service.UploadService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}") private String bucket;
    @Value("${minio.endpoint}") private String endpoint;

    @Override
    public String uploadImage(MultipartFile file) {
        validateImage(file);
        String ext = getExtension(file.getOriginalFilename());
        String objectName = "images/" + UUID.randomUUID() + "." + ext;
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("文件不能为空");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new IllegalArgumentException("只允许上传图片文件");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("图片大小不能超过 5MB");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
}
