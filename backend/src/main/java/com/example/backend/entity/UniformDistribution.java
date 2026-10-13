package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UniformDistribution {
    private Long id;
    private Long studentId;
    private Long volunteerId;
    private String clothingSize;
    private String shoeSize;
    private LocalDateTime distributeTime;
    private Integer status;
    private LocalDateTime createTime;
}
