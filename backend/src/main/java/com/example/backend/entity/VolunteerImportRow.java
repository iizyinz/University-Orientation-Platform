package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class VolunteerImportRow {

    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("志愿者类型")
    private String volunteerType;
}
