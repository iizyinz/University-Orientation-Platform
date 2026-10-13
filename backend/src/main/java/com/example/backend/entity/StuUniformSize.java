package com.example.backend.entity;

import lombok.Data;

@Data
public class StuUniformSize {
    private Long id;
    private Long studentId;
    private String clothingSize;
    private String shoeSize;
}
