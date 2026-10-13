package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StuImportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("考生号")
    private String examNo;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("学院编码")
    private String collegeCode;

    @ExcelProperty("专业编码")
    private String majorCode;

    @ExcelProperty("班级编码")
    private String classCode;
}
