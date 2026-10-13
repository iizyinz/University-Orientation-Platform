package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormBuilding {
    private Long id;
    private String buildingName;
    private String buildingCode;
    private Integer floorCount;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Integer roomCount;
    private Integer usedBedCount;
    private Integer totalBedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
