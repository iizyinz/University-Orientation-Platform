package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CmsHandbook {
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private Integer sortOrder;
    private Integer publishStatus;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
