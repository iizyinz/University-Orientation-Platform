package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentExportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("民族")
    private String nation;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("学院")
    private String collegeName;

    @ExcelProperty("专业")
    private String majorName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("生源地")
    private String hometown;

    @ExcelProperty("报到状态")
    private String reportStatus;

    @ExcelProperty("宿舍")
    private String dormInfo;

    @ExcelProperty("紧急联系人")
    private String emergencyContactName;

    @ExcelProperty("紧急联系人电话")
    private String emergencyContactPhone;
}