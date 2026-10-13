package com.example.backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeMajorConfig {
    private Long id;
    private Long majorId;
    private Long feeItemId;
    private String majorName;
    private String collegeName;
    private String itemName;
    private BigDecimal amount;
    private String payUrl;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
