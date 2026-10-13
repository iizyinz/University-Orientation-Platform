package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CmsCampusMap {
    private Long id;
    private String mapName;
    private String mapImage;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
