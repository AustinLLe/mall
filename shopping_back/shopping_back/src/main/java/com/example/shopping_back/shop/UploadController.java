package com.example.shopping_back.shop;

import com.example.shopping_back.common.dto.ApiResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private final Path uploadDir;

    public UploadController(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping("/image")
    public ApiResult<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResult.fail(500, "文件为空");
        }

        try {
            Files.createDirectories(uploadDir);
            String fileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
            Path dest = uploadDir.resolve(fileName).normalize();
            if (!dest.startsWith(uploadDir)) {
                return ApiResult.fail(400, "文件名不合法");
            }
            file.transferTo(dest);
            return ApiResult.ok("/files/" + fileName);
        } catch (IOException e) {
            return ApiResult.fail(500, "上传失败");
        }
    }

    private String sanitizeFileName(String original) {
        String fallback = "product-image.jpg";
        if (original == null || original.isBlank()) {
            return fallback;
        }
        String name = Paths.get(original).getFileName().toString();
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return name.isBlank() ? fallback : name;
    }
}
