package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KeyDistribution {
    private Long id;
    private Long studentId;
    private Long roomId;
    private Long volunteerId;
    private LocalDateTime distributeTime;
    private Integer status;
    private LocalDateTime createTime;
}
