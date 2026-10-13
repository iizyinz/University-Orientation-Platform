package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysCounselor {
    private Long id;
    private Long userId;
    private Long collegeId;
    private String collegeName;
    private String workNo;
    private String phone;
    private String email;
    private String realName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
