package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FaqKnowledge {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String question;
    private String answer;
    private String keywords;
    private Integer sortOrder;
    private Integer status;
    private Integer hitCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
