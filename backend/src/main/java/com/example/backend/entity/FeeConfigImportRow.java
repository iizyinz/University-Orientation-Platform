package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FeeConfigImportRow {

    @ExcelProperty("专业编码")
    private String majorCode;

    @ExcelProperty("费用项目编码")
    private String feeItemCode;

    @ExcelProperty("金额")
    private String amount;

    @ExcelProperty("备注")
    private String remark;
}
