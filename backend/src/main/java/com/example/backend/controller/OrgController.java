package com.example.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.backend.common.ImportResult;
import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.mapper.SysCollegeMapper;
import com.example.backend.mapper.SysMajorMapper;
import com.example.backend.mapper.SysClassMapper;
import com.example.backend.mapper.SysCounselorMapper;
import com.example.backend.service.SysClassService;
import com.example.backend.service.SysCollegeService;
import com.example.backend.service.SysMajorService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org")
@CrossOrigin
public class OrgController {

    @Autowired
    private SysCollegeService collegeService;
    @Autowired
    private SysMajorService majorService;
    @Autowired
    private SysClassService classService;
    @Autowired
    private SysCollegeMapper collegeMapper;
    @Autowired
    private SysMajorMapper majorMapper;
    @Autowired
    private SysClassMapper classMapper;
    @Autowired
    private SysCounselorMapper counselorMapper;

    // ==================== Counselor-Class Assignment ====================

    @GetMapping("/counselor-class/list")
    public Result<?> counselorClassList(@RequestParam(required = false) Long collegeId) {
        List<SysCounselor> counselors = counselorMapper.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysCounselor c : counselors) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("userId", c.getUserId());
            item.put("realName", c.getRealName());
            item.put("workNo", c.getWorkNo());
            item.put("phone", c.getPhone());
            item.put("collegeId", c.getCollegeId());
            item.put("collegeName", c.getCollegeName());
            List<SysCounselorClass> classes = counselorMapper.findClassesDetailByCounselorId(c.getId());
            item.put("classes", classes);
            item.put("classCount", classes.size());
            result.add(item);
        }
        if (collegeId != null) {
            result = result.stream().filter(item -> collegeId.equals(item.get("collegeId"))).collect(java.util.stream.Collectors.toList());
        }
        return Result.success(result);
    }

    @PutMapping("/counselor-class/assign")
    public Result<?> assignClasses(@RequestBody Map<String, Object> body) {
        Long counselorId = Long.valueOf(body.get("counselorId").toString());
        List<Long> classIds = body.get("classIds") != null ?
            ((List<?>) body.get("classIds")).stream().map(o -> Long.valueOf(o.toString())).toList() : List.of();
        counselorMapper.deleteClassesByCounselorId(counselorId);
        if (!classIds.isEmpty()) {
            counselorMapper.insertClasses(counselorId, classIds);
        }
        return Result.success("分配成功");
    }

    @GetMapping("/counselor-class/template")
    public void downloadCounselorClassTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("班级分配导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<CounselorClassImportRow> rows = new ArrayList<>();
        CounselorClassImportRow example = new CounselorClassImportRow();
        example.setWorkNo("T001");
        example.setClassCode("CS202601");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), CounselorClassImportRow.class)
                .sheet("班级分配数据").doWrite(rows);
    }

    @PostMapping("/counselor-class/import")
    public Result<ImportResult> importCounselorClasses(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<CounselorClassImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), CounselorClassImportRow.class, new ReadListener<CounselorClassImportRow>() {
                public void invoke(CounselorClassImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            CounselorClassImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getWorkNo() == null || row.getWorkNo().isBlank()) {
                    result.addError("第" + rowNum + "行：辅导员工号不能为空"); continue;
                }
                if (row.getClassCode() == null || row.getClassCode().isBlank()) {
                    result.addError("第" + rowNum + "行：班级编码不能为空"); continue;
                }

                SysCounselor counselor = counselorMapper.findByWorkNo(row.getWorkNo().trim());
                if (counselor == null) {
                    result.addError("第" + rowNum + "行：辅导员工号不存在「" + row.getWorkNo() + "」"); continue;
                }

                SysClass sysClass = classMapper.findByCode(row.getClassCode().trim());
                if (sysClass == null) {
                    result.addError("第" + rowNum + "行：班级编码不存在「" + row.getClassCode() + "」"); continue;
                }

                List<SysCounselorClass> existing = counselorMapper.findClassesByCounselorId(counselor.getId());
                boolean alreadyAssigned = existing.stream().anyMatch(c -> c.getClassId().equals(sysClass.getId()));
                if (alreadyAssigned) {
                    result.addError("第" + rowNum + "行：该辅导员已分配此班级「" + row.getClassCode() + "」"); continue;
                }

                counselorMapper.insertClasses(counselor.getId(), List.of(sysClass.getId()));
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }

    // ==================== College ====================

    @GetMapping("/college/list")
    public Result<List<SysCollege>> collegeList() {
        return Result.success(collegeService.findAll());
    }

    @GetMapping("/college/{id}")
    public Result<SysCollege> collegeGet(@PathVariable Long id) {
        SysCollege college = collegeService.findById(id);
        if (college == null) return Result.error(404, "学院不存在");
        return Result.success(college);
    }

    @PostMapping("/college")
    public Result<SysCollege> collegeCreate(@RequestBody SysCollege college) {
        try {
            return Result.success(collegeService.create(college));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/college")
    public Result<SysCollege> collegeUpdate(@RequestBody SysCollege college) {
        try {
            return Result.success(collegeService.update(college));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/college/{id}")
    public Result<Void> collegeDelete(@PathVariable Long id) {
        collegeService.delete(id);
        return Result.success(null);
    }

    // ==================== Major ====================

    @GetMapping("/major/list")
    public Result<List<SysMajor>> majorList() {
        return Result.success(majorService.findAll());
    }

    @GetMapping("/major/byCollege/{collegeId}")
    public Result<List<SysMajor>> majorByCollege(@PathVariable Long collegeId) {
        return Result.success(majorService.findByCollegeId(collegeId));
    }

    @GetMapping("/major/{id}")
    public Result<SysMajor> majorGet(@PathVariable Long id) {
        SysMajor major = majorService.findById(id);
        if (major == null) return Result.error(404, "专业不存在");
        return Result.success(major);
    }

    @PostMapping("/major")
    public Result<SysMajor> majorCreate(@RequestBody SysMajor major) {
        try {
            return Result.success(majorService.create(major));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/major")
    public Result<SysMajor> majorUpdate(@RequestBody SysMajor major) {
        try {
            return Result.success(majorService.update(major));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/major/{id}")
    public Result<Void> majorDelete(@PathVariable Long id) {
        majorService.delete(id);
        return Result.success(null);
    }

    // ==================== Class ====================

    @GetMapping("/class/list")
    public Result<List<SysClass>> classList() {
        return Result.success(classService.findAll());
    }

    @GetMapping("/class/byMajor/{majorId}")
    public Result<List<SysClass>> classByMajor(@PathVariable Long majorId) {
        return Result.success(classService.findByMajorId(majorId));
    }

    @GetMapping("/class/{id}")
    public Result<SysClass> classGet(@PathVariable Long id) {
        SysClass sysClass = classService.findById(id);
        if (sysClass == null) return Result.error(404, "班级不存在");
        return Result.success(sysClass);
    }

    @PostMapping("/class")
    public Result<SysClass> classCreate(@RequestBody SysClass sysClass) {
        try {
            return Result.success(classService.create(sysClass));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/class")
    public Result<SysClass> classUpdate(@RequestBody SysClass sysClass) {
        try {
            return Result.success(classService.update(sysClass));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/class/{id}")
    public Result<Void> classDelete(@PathVariable Long id) {
        classService.delete(id);
        return Result.success(null);
    }

    // ==================== Major Excel Import ====================

    @GetMapping("/major/template")
    public void downloadMajorTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("专业导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<MajorImportRow> rows = new ArrayList<>();
        MajorImportRow example = new MajorImportRow();
        example.setCollegeCode("CS");
        example.setMajorName("计算机科学与技术");
        example.setMajorCode("CS01");
        example.setDescription("示例数据，可删除");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), MajorImportRow.class)
                .sheet("专业数据").doWrite(rows);
    }

    @PostMapping("/major/import")
    public Result<ImportResult> importMajors(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<MajorImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), MajorImportRow.class, new ReadListener<MajorImportRow>() {
                public void invoke(MajorImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            MajorImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getMajorName() == null || row.getMajorName().isBlank()) {
                    result.addError("第" + rowNum + "行：专业名称不能为空"); continue;
                }
                if (row.getMajorCode() == null || row.getMajorCode().isBlank()) {
                    result.addError("第" + rowNum + "行：专业编码不能为空"); continue;
                }
                if (row.getCollegeCode() == null || row.getCollegeCode().isBlank()) {
                    result.addError("第" + rowNum + "行：学院编码不能为空"); continue;
                }

                SysCollege college = collegeMapper.findByCode(row.getCollegeCode().trim());
                if (college == null) {
                    result.addError("第" + rowNum + "行：学院编码不存在「" + row.getCollegeCode() + "」"); continue;
                }

                if (majorMapper.countByCode(row.getMajorCode().trim()) > 0) {
                    result.addError("第" + rowNum + "行：专业编码已存在「" + row.getMajorCode() + "」"); continue;
                }

                SysMajor major = new SysMajor();
                major.setCollegeId(college.getId());
                major.setMajorName(row.getMajorName().trim());
                major.setMajorCode(row.getMajorCode().trim());
                major.setDescription(row.getDescription());
                major.setSortOrder(0);
                major.setStatus(1);
                majorMapper.insert(major);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }

    // ==================== Class Excel Import ====================

    @GetMapping("/class/template")
    public void downloadClassTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("班级导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<ClassImportRow> rows = new ArrayList<>();
        ClassImportRow example = new ClassImportRow();
        example.setCollegeCode("CS");
        example.setMajorCode("CS01");
        example.setClassName("2026级计科1班");
        example.setClassCode("CS202601");
        example.setDescription("示例数据，可删除");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), ClassImportRow.class)
                .sheet("班级数据").doWrite(rows);
    }

    @PostMapping("/class/import")
    public Result<ImportResult> importClasses(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<ClassImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), ClassImportRow.class, new ReadListener<ClassImportRow>() {
                public void invoke(ClassImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            ClassImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getClassName() == null || row.getClassName().isBlank()) {
                    result.addError("第" + rowNum + "行：班级名称不能为空"); continue;
                }
                if (row.getClassCode() == null || row.getClassCode().isBlank()) {
                    result.addError("第" + rowNum + "行：班级编码不能为空"); continue;
                }
                if (row.getMajorCode() == null || row.getMajorCode().isBlank()) {
                    result.addError("第" + rowNum + "行：专业编码不能为空"); continue;
                }

                SysMajor major = majorMapper.findByCode(row.getMajorCode().trim());
                if (major == null) {
                    result.addError("第" + rowNum + "行：专业编码不存在「" + row.getMajorCode() + "」"); continue;
                }

                if (classMapper.countByCode(row.getClassCode().trim()) > 0) {
                    result.addError("第" + rowNum + "行：班级编码已存在「" + row.getClassCode() + "」"); continue;
                }

                SysClass sysClass = new SysClass();
                sysClass.setMajorId(major.getId());
                sysClass.setCollegeId(major.getCollegeId());
                sysClass.setClassName(row.getClassName().trim());
                sysClass.setClassCode(row.getClassCode().trim());
                sysClass.setDescription(row.getDescription());
                sysClass.setSortOrder(0);
                sysClass.setStatus(1);
                classMapper.insert(sysClass);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }
}
