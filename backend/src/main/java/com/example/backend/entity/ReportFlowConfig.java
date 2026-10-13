package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportFlowConfig {
    private Long id;
    private String flowName;
    private Integer flowStep;
    private String flowDesc;
    private Integer isRequired;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
