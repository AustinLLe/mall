package com.example.shopping_back.goods;

import com.example.shopping_back.common.dto.ApiResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; 
import java.io.File; 
import java.io.IOException; 
import java.util.UUID; 

@RestController
@RequestMapping("/api/upload")
public class FileController {

    // 图片存储的绝对路径（根据你电脑实际情况改，比如 D:/upload/）
    private final String uploadPath = "D:/upload/";

    @PostMapping("/image")
    public ApiResult<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ApiResult.fail(500, "文件为空");

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(uploadPath + fileName);
            
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

            file.transferTo(dest);

            String url = "http://localhost:8080/files/" + fileName;
            return ApiResult.ok(url);
        } catch (IOException e) {
            return ApiResult.fail(500, "上传失败");
        }
    }
}