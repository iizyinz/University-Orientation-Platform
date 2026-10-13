package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysClass {
    private Long id;
    private Long majorId;
    private Long collegeId;
    private String majorName;
    private String collegeName;
    private String className;
    private String classCode;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
