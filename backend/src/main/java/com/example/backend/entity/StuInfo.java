package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StuInfo {
    private Long id;
    private Long userId;
    private String studentNo;
    private String examNo;
    private String idCard;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private String nation;
    private String politicalStatus;
    private String province;
    private String city;
    private String district;
    private String healthStatus;
    private String phone;
    private String email;
    private String hobby;
    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private Long collegeId;
    private Long majorId;
    private Long classId;
    private String collegeName;
    private String majorName;
    private String className;
    private Integer reportStatus;
    private LocalDateTime reportTime;
    private Integer infoCompleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Scan-related fields
    private Integer hasKey;
    private Integer hasUniform;
    private String clothingSize;
    private String shoeSize;
    private Long roomId;
    private String roomNo;
    private String buildingName;
    private Integer bedNo;
}
