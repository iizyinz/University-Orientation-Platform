package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StuDormChoice {
    private Long id;
    private Long studentId;
    private Long roomId;
    private Integer bedNo;
    private LocalDateTime chooseTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 关联字段
    private String buildingName;
    private Integer floorNo;
    private String roomNo;
    private String roomType;
}
