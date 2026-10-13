package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FaqKnowledgeImportRow {

    @ExcelProperty("问题")
    private String question;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("关键词")
    private String keywords;

    @ExcelProperty("分类名称")
    private String categoryName;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private String status;
}