package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DormChooseConfig {
    private Long id;
    private String configName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
