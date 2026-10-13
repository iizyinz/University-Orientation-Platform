package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CmsNotice {
    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer publishStatus;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
