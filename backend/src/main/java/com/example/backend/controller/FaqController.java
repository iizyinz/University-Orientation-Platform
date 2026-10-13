package com.example.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.backend.common.ImportResult;
import com.example.backend.common.Result;
import com.example.backend.entity.FaqCategory;
import com.example.backend.entity.FaqKnowledge;
import com.example.backend.entity.FaqKnowledgeImportRow;
import com.example.backend.mapper.FaqCategoryMapper;
import com.example.backend.service.FaqCategoryService;
import com.example.backend.service.FaqKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faq")
@CrossOrigin
public class FaqController {

    @Autowired private FaqCategoryService categoryService;
    @Autowired private FaqKnowledgeService knowledgeService;
    @Autowired private FaqCategoryMapper categoryMapper;

    // ==================== Category ====================
    @GetMapping("/category/list")
    public Result<List<FaqCategory>> categoryList() { return Result.success(categoryService.findAll()); }
    @PostMapping("/category")
    public Result<FaqCategory> categoryCreate(@RequestBody Map<String, Object> body) {
        FaqCategory c = new FaqCategory();
        c.setCategoryName(str(body.get("categoryName")));
        c.setSortOrder(toInt(body.get("sortOrder")));
        c.setStatus(toInt(body.get("status")));
        return Result.success(categoryService.create(c));
    }
    @PutMapping("/category")
    public Result<FaqCategory> categoryUpdate(@RequestBody Map<String, Object> body) {
        FaqCategory c = new FaqCategory();
        c.setId(toLong(body.get("id")));
        c.setCategoryName(str(body.get("categoryName")));
        c.setSortOrder(toInt(body.get("sortOrder")));
        c.setStatus(toInt(body.get("status")));
        return Result.success(categoryService.update(c));
    }
    @DeleteMapping("/category/{id}")
    public Result<Void> categoryDelete(@PathVariable Long id) { categoryService.delete(id); return Result.success(null); }

    // ==================== Knowledge ====================
    @GetMapping("/knowledge/list")
    public Result<List<FaqKnowledge>> knowledgeList() { return Result.success(knowledgeService.findAll()); }
    @GetMapping("/knowledge/byCategory/{categoryId}")
    public Result<List<FaqKnowledge>> knowledgeByCategory(@PathVariable Long categoryId) { return Result.success(knowledgeService.findByCategoryId(categoryId)); }
    @PostMapping("/knowledge")
    public Result<FaqKnowledge> knowledgeCreate(@RequestBody Map<String, Object> body) {
        FaqKnowledge k = new FaqKnowledge();
        k.setCategoryId(toLong(body.get("categoryId")));
        k.setQuestion(str(body.get("question")));
        k.setAnswer(str(body.get("answer")));
        k.setKeywords(str(body.get("keywords")));
        k.setSortOrder(toInt(body.get("sortOrder")));
        k.setStatus(toInt(body.get("status")));
        return Result.success(knowledgeService.create(k));
    }
    @PutMapping("/knowledge")
    public Result<FaqKnowledge> knowledgeUpdate(@RequestBody Map<String, Object> body) {
        FaqKnowledge k = new FaqKnowledge();
        k.setId(toLong(body.get("id")));
        k.setCategoryId(toLong(body.get("categoryId")));
        k.setQuestion(str(body.get("question")));
        k.setAnswer(str(body.get("answer")));
        k.setKeywords(str(body.get("keywords")));
        k.setSortOrder(toInt(body.get("sortOrder")));
        k.setStatus(toInt(body.get("status")));
        return Result.success(knowledgeService.update(k));
    }
    @DeleteMapping("/knowledge/{id}")
    public Result<Void> knowledgeDelete(@PathVariable Long id) { knowledgeService.delete(id); return Result.success(null); }

    // ==================== Import ====================
    @GetMapping("/knowledge/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("FAQ问答导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<FaqKnowledgeImportRow> templateRows = new ArrayList<>();
        FaqKnowledgeImportRow example = new FaqKnowledgeImportRow();
        example.setQuestion("如何办理学生卡？");
        example.setAnswer("请持本人身份证到学校服务中心办理。");
        example.setKeywords("学生卡,办理,校园卡");
        example.setCategoryName("报到相关");
        example.setSortOrder(1);
        example.setStatus("启用");
        templateRows.add(example);

        EasyExcel.write(response.getOutputStream(), FaqKnowledgeImportRow.class)
                .sheet("FAQ数据")
                .doWrite(templateRows);
    }

    @PostMapping("/knowledge/import")
    public Result<ImportResult> importKnowledge(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的Excel文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls"))) {
            return Result.error("仅支持 .xlsx 或 .xls 格式的Excel文件");
        }

        ImportResult importResult = new ImportResult();
        List<FaqKnowledgeImportRow> allRows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), FaqKnowledgeImportRow.class, new ReadListener<FaqKnowledgeImportRow>() {
                @Override
                public void invoke(FaqKnowledgeImportRow row, AnalysisContext context) {
                    allRows.add(row);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }

        importResult.setTotalRows(allRows.size());
        for (int i = 0; i < allRows.size(); i++) {
            FaqKnowledgeImportRow row = allRows.get(i);
            int rowNum = i + 2;

            if (row.getQuestion() == null || row.getQuestion().isBlank()) {
                importResult.addError("第" + rowNum + "行：问题不能为空");
                continue;
            }
            if (row.getAnswer() == null || row.getAnswer().isBlank()) {
                importResult.addError("第" + rowNum + "行：答案不能为空");
                continue;
            }

            try {
                FaqKnowledge k = new FaqKnowledge();
                k.setQuestion(row.getQuestion().trim());
                k.setAnswer(row.getAnswer().trim());
                k.setKeywords(row.getKeywords() != null ? row.getKeywords().trim() : null);
                k.setSortOrder(row.getSortOrder() != null ? row.getSortOrder() : 0);
                k.setStatus("禁用".equals(row.getStatus()) ? 0 : 1);

                if (row.getCategoryName() != null && !row.getCategoryName().isBlank()) {
                    FaqCategory cat = categoryMapper.findByCategoryName(row.getCategoryName().trim());
                    if (cat != null) {
                        k.setCategoryId(cat.getId());
                    }
                }

                knowledgeService.create(k);
                importResult.setSuccessCount(importResult.getSuccessCount() + 1);
            } catch (RuntimeException e) {
                importResult.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", importResult);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}
