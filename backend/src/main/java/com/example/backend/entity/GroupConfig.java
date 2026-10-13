package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupConfig {
    private Long id;
    private Integer groupType;
    private String groupName;
    private String groupNo;
    private String groupQrImage;
    private Long targetId;
    private String targetName;
    private String province;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
