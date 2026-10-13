package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysCollege {
    private Long id;
    private String collegeName;
    private String collegeCode;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
