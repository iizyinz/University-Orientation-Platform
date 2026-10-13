package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysMajor {
    private Long id;
    private Long collegeId;
    private String collegeName;
    private String majorName;
    private String majorCode;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
