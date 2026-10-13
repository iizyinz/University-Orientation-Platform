package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StuDocument {
    private Long id;
    private Long studentId;
    private Integer docType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime uploadTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}