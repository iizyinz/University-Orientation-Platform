package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysVolunteer {
    private Long id;
    private Long userId;
    private Integer volunteerType;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
