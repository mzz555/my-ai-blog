package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.uploadImage(file));
    }
}
