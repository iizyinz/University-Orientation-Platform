package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ClassImportRow {

    @ExcelProperty("学院编码")
    private String collegeCode;

    @ExcelProperty("专业编码")
    private String majorCode;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("班级编码")
    private String classCode;

    @ExcelProperty("描述")
    private String description;
}
