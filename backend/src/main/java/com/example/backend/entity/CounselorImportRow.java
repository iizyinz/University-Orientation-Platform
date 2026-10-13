package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CounselorImportRow {

    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty("工号")
    private String workNo;

    @ExcelProperty("学院编码")
    private String collegeCode;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;
}
