package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "dir", defaultValue = "common") String dir) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.error("仅支持图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        try {
            String url = s3Service.upload(file, dir);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
