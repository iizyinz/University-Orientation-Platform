package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormRoom {
    private Long id;
    private Long buildingId;
    private String buildingName;
    private Integer floorNo;
    private String roomNo;
    private Integer bedCount;
    private Integer usedBedCount;
    private String roomType;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
