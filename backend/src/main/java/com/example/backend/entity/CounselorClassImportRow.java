package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CounselorClassImportRow {

    @ExcelProperty("辅导员工号")
    private String workNo;

    @ExcelProperty("班级编码")
    private String classCode;
}
