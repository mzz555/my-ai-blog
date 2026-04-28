package com.blog.service.impl;

import com.blog.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Value("${upload.dir:uploads}") private String uploadDir;

    @Override
    public String uploadImage(MultipartFile file) {
        validateImage(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, "images").toAbsolutePath();
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            log.error("本地文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
        return "/uploads/images/" + filename;
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
}
