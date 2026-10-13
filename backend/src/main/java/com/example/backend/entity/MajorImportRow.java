package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MajorImportRow {

    @ExcelProperty("学院编码")
    private String collegeCode;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("专业编码")
    private String majorCode;

    @ExcelProperty("描述")
    private String description;
}
