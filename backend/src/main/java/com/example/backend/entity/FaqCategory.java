package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FaqCategory {
    private Long id;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    private Integer questionCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
