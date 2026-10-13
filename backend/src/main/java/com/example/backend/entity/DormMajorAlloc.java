package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormMajorAlloc {
    private Long id;
    private Long majorId;
    private Long roomId;
    private Integer gender;
    private String majorName;
    private String collegeName;
    private String buildingName;
    private String roomNo;
    private Integer allocBedCount;
    private Integer usedBedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
