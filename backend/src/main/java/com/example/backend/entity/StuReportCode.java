package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StuReportCode {
    private Long id;
    private Long studentId;
    private String codeContent;
    private String codeImage;
    private Integer isUsed;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
