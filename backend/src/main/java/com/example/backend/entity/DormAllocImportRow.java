package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DormAllocImportRow {

    @ExcelProperty("专业编码")
    private String majorCode;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("楼栋名称")
    private String buildingName;

    @ExcelProperty("房间号")
    private String roomNo;

    @ExcelProperty("分配床位数")
    private Integer allocBedCount;
}
