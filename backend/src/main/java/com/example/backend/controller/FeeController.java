package com.example.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.backend.common.ImportResult;
import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.mapper.FeeItemMapper;
import com.example.backend.mapper.SysMajorMapper;
import com.example.backend.service.FeeItemService;
import com.example.backend.service.FeeMajorConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/fee")
@CrossOrigin
public class FeeController {

    @Autowired
    private FeeItemService feeItemService;
    @Autowired
    private FeeMajorConfigService feeMajorConfigService;
    @Autowired
    private FeeItemMapper feeItemMapper;
    @Autowired
    private SysMajorMapper majorMapper;

    // ==================== Fee Item ====================

    @GetMapping("/item/list")
    public Result<List<FeeItem>> itemList() {
        return Result.success(feeItemService.findAll());
    }

    @GetMapping("/item/{id}")
    public Result<FeeItem> itemGet(@PathVariable Long id) {
        FeeItem item = feeItemService.findById(id);
        if (item == null) return Result.error(404, "费用项目不存在");
        return Result.success(item);
    }

    @PostMapping("/item")
    public Result<FeeItem> itemCreate(@RequestBody FeeItem item) {
        try {
            return Result.success(feeItemService.create(item));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/item")
    public Result<FeeItem> itemUpdate(@RequestBody FeeItem item) {
        try {
            return Result.success(feeItemService.update(item));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/item/{id}")
    public Result<Void> itemDelete(@PathVariable Long id) {
        feeItemService.delete(id);
        return Result.success(null);
    }

    // ==================== Fee Major Config ====================

    @GetMapping("/config/list")
    public Result<List<FeeMajorConfig>> configList() {
        return Result.success(feeMajorConfigService.findAll());
    }

    @GetMapping("/config/byMajor/{majorId}")
    public Result<List<FeeMajorConfig>> configByMajor(@PathVariable Long majorId) {
        return Result.success(feeMajorConfigService.findByMajorId(majorId));
    }

    @GetMapping("/config/{id}")
    public Result<FeeMajorConfig> configGet(@PathVariable Long id) {
        FeeMajorConfig config = feeMajorConfigService.findById(id);
        if (config == null) return Result.error(404, "配置不存在");
        return Result.success(config);
    }

    @PostMapping("/config")
    public Result<FeeMajorConfig> configCreate(@RequestBody FeeMajorConfig config) {
        try {
            return Result.success(feeMajorConfigService.create(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/config")
    public Result<FeeMajorConfig> configUpdate(@RequestBody FeeMajorConfig config) {
        try {
            return Result.success(feeMajorConfigService.update(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/config/{id}")
    public Result<Void> configDelete(@PathVariable Long id) {
        feeMajorConfigService.delete(id);
        return Result.success(null);
    }

    // ==================== Fee Config Excel Import ====================

    @GetMapping("/config/template")
    public void downloadConfigTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("费用配置导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<FeeConfigImportRow> rows = new ArrayList<>();
        FeeConfigImportRow example = new FeeConfigImportRow();
        example.setMajorCode("CS01");
        example.setFeeItemCode("TUITION");
        example.setAmount("6000.00");
        example.setRemark("示例数据，可删除");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), FeeConfigImportRow.class)
                .sheet("费用配置数据").doWrite(rows);
    }

    @PostMapping("/config/import")
    public Result<ImportResult> importConfig(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<FeeConfigImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), FeeConfigImportRow.class, new ReadListener<FeeConfigImportRow>() {
                public void invoke(FeeConfigImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            FeeConfigImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getMajorCode() == null || row.getMajorCode().isBlank()) {
                    result.addError("第" + rowNum + "行：专业编码不能为空"); continue;
                }
                if (row.getFeeItemCode() == null || row.getFeeItemCode().isBlank()) {
                    result.addError("第" + rowNum + "行：费用项目编码不能为空"); continue;
                }
                if (row.getAmount() == null || row.getAmount().isBlank()) {
                    result.addError("第" + rowNum + "行：金额不能为空"); continue;
                }

                SysMajor major = majorMapper.findByCode(row.getMajorCode().trim());
                if (major == null) {
                    result.addError("第" + rowNum + "行：专业编码不存在「" + row.getMajorCode() + "」"); continue;
                }

                FeeItem feeItem = feeItemMapper.findByItemCode(row.getFeeItemCode().trim());
                if (feeItem == null) {
                    result.addError("第" + rowNum + "行：费用项目编码不存在「" + row.getFeeItemCode() + "」"); continue;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(row.getAmount().trim());
                } catch (NumberFormatException e) {
                    result.addError("第" + rowNum + "行：金额格式不正确"); continue;
                }

                // 检查是否已存在，存在则更新金额
                List<FeeMajorConfig> existing = feeMajorConfigService.findByMajorId(major.getId());
                FeeConfigImportRow finalRow = row;
                FeeMajorConfig existConfig = existing.stream()
                    .filter(c -> c.getFeeItemId() != null && c.getFeeItemId().equals(feeItem.getId()))
                    .findFirst().orElse(null);

                if (existConfig != null) {
                    existConfig.setAmount(amount);
                    existConfig.setRemark(row.getRemark());
                    feeMajorConfigService.update(existConfig);
                } else {
                    FeeMajorConfig config = new FeeMajorConfig();
                    config.setMajorId(major.getId());
                    config.setFeeItemId(feeItem.getId());
                    config.setAmount(amount);
                    config.setRemark(row.getRemark());
                    feeMajorConfigService.create(config);
                }
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }
}
