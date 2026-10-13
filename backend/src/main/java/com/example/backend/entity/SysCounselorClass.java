package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysCounselorClass {
    private Long id;
    private Long counselorId;
    private Long classId;
    private String className;
    private Long majorId;
    private String majorName;
    private Long collegeId;
    private String collegeName;
    private Integer studentCount;
    private Integer reportedCount;
    private LocalDateTime createTime;
}
