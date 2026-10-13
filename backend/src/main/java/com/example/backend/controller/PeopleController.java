package com.example.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.backend.common.ImportResult;
import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.mapper.SysClassMapper;
import com.example.backend.mapper.SysCollegeMapper;
import com.example.backend.mapper.SysMajorMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.StuInfoService;
import com.example.backend.service.SysCounselorService;
import com.example.backend.service.SysVolunteerService;
import com.example.backend.util.PasswordUtil;
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
@RequestMapping("/api/people")
@CrossOrigin
public class PeopleController {

    @Autowired
    private SysCounselorService counselorService;
    @Autowired
    private SysVolunteerService volunteerService;
    @Autowired
    private StuInfoService stuInfoService;
    @Autowired
    private SysCollegeMapper collegeMapper;
    @Autowired
    private SysMajorMapper majorMapper;
    @Autowired
    private SysClassMapper classMapper;
    @Autowired
    private SysUserMapper userMapper;

    // ==================== Counselor ====================

    @GetMapping("/counselor/list")
    public Result<List<SysCounselor>> counselorList() {
        return Result.success(counselorService.findAll());
    }

    // ==================== Admin ====================

    @GetMapping("/admin/list")
    public Result<List<SysUser>> adminList() {
        List<SysUser> all = userMapper.findAll();
        List<SysUser> admins = all.stream().filter(u -> u.getUserType() != null && u.getUserType() == 3).toList();
        admins.forEach(u -> u.setPassword(null));
        return Result.success(admins);
    }

    @PostMapping("/admin")
    public Result<?> adminCreate(@RequestBody Map<String, Object> body) {
        String username = str(body.get("username"));
        String realName = str(body.get("realName"));
        String password = str(body.get("password"));
        if (username == null || username.isBlank()) return Result.error(400, "用户名不能为空");
        if (realName == null || realName.isBlank()) return Result.error(400, "姓名不能为空");
        if (password == null || password.isBlank()) password = "123456";

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword(PasswordUtil.encode(password));
        user.setUserType(3);
        user.setStatus(1);
        userMapper.insertUser(user);
        return Result.success("创建成功");
    }

    @PutMapping("/admin")
    public Result<?> adminUpdate(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        String realName = str(body.get("realName"));
        String password = str(body.get("password"));
        if (id == null) return Result.error(400, "缺少ID");

        SysUser user = userMapper.findById(id);
        if (user == null) return Result.error(404, "用户不存在");
        if (realName != null) user.setRealName(realName);
        if (password != null && !password.isBlank()) user.setPassword(PasswordUtil.encode(password));
        userMapper.updateProfile(user);
        return Result.success("更新成功");
    }

    @PutMapping("/admin/reset-password")
    public Result<?> adminResetPassword(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        String newPassword = str(body.get("newPassword"));
        if (id == null) return Result.error(400, "缺少ID");
        if (newPassword == null || newPassword.isBlank()) newPassword = "123456";
        userMapper.updatePassword(PasswordUtil.encode(newPassword), id);
        return Result.success("密码已重置为: " + newPassword);
    }

    @DeleteMapping("/admin/{id}")
    public Result<?> adminDelete(@PathVariable Long id) {
        SysUser user = userMapper.findById(id);
        if (user == null) return Result.error(404, "用户不存在");
        if (user.getUserType() != 3) return Result.error("只能删除管理员账号");
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/counselor/byCollege/{collegeId}")
    public Result<List<SysCounselor>> counselorByCollege(@PathVariable Long collegeId) {
        return Result.success(counselorService.findByCollegeId(collegeId));
    }

    @GetMapping("/counselor/{id}")
    public Result<SysCounselor> counselorGet(@PathVariable Long id) {
        SysCounselor c = counselorService.findById(id);
        if (c == null) return Result.error(404, "辅导员不存在");
        return Result.success(c);
    }

    @PostMapping("/counselor")
    public Result<SysCounselor> counselorCreate(@RequestBody Map<String, Object> body) {
        try {
            SysCounselor counselor = new SysCounselor();
            counselor.setCollegeId(toLong(body.get("collegeId")));
            counselor.setWorkNo((String) body.get("workNo"));
            counselor.setRealName((String) body.get("realName"));
            counselor.setPhone((String) body.get("phone"));
            counselor.setEmail((String) body.get("email"));

            @SuppressWarnings("unchecked")
            List<Long> classIds = body.get("classIds") != null ?
                ((List<?>) body.get("classIds")).stream().map(this::toLong).toList() : List.of();

            return Result.success(counselorService.create(counselor, classIds));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/counselor")
    public Result<SysCounselor> counselorUpdate(@RequestBody Map<String, Object> body) {
        try {
            SysCounselor counselor = new SysCounselor();
            counselor.setId(toLong(body.get("id")));
            counselor.setCollegeId(toLong(body.get("collegeId")));
            counselor.setWorkNo((String) body.get("workNo"));
            counselor.setRealName((String) body.get("realName"));
            counselor.setPhone((String) body.get("phone"));
            counselor.setEmail((String) body.get("email"));

            @SuppressWarnings("unchecked")
            List<Long> classIds = body.get("classIds") != null ?
                ((List<?>) body.get("classIds")).stream().map(this::toLong).toList() : List.of();

            return Result.success(counselorService.update(counselor, classIds));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

@DeleteMapping("/counselor/{id}")
    public Result<Void> counselorDelete(@PathVariable Long id) {
        counselorService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/counselor/reset-password")
    public Result<?> counselorResetPassword(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        if (id == null) return Result.error(400, "缺少ID");
        SysCounselor c = counselorService.findById(id);
        if (c == null) return Result.error(404, "辅导员不存在");
        if (c.getUserId() == null) return Result.error("辅导员未绑定账号，无法重置密码");
        String newPassword = str(body.get("newPassword"));
        if (newPassword == null || newPassword.isBlank()) newPassword = "123456";
        userMapper.updatePassword(PasswordUtil.encode(newPassword), c.getUserId());
        return Result.success("密码已重置为: " + newPassword);
    }

    // ==================== Volunteer ====================

    @GetMapping("/volunteer/list")
    public Result<List<SysVolunteer>> volunteerList() {
        return Result.success(volunteerService.findAll());
    }

    @GetMapping("/volunteer/byType/{type}")
    public Result<List<SysVolunteer>> volunteerByType(@PathVariable Integer type) {
        return Result.success(volunteerService.findByType(type));
    }

    @GetMapping("/volunteer/{id}")
    public Result<SysVolunteer> volunteerGet(@PathVariable Long id) {
        SysVolunteer v = volunteerService.findById(id);
        if (v == null) return Result.error(404, "志愿者不存在");
        return Result.success(v);
    }

    @PostMapping("/volunteer")
    public Result<SysVolunteer> volunteerCreate(@RequestBody SysVolunteer volunteer) {
        try {
            return Result.success(volunteerService.create(volunteer));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/volunteer")
    public Result<SysVolunteer> volunteerUpdate(@RequestBody SysVolunteer volunteer) {
        try {
            return Result.success(volunteerService.update(volunteer));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/volunteer/{id}")
    public Result<Void> volunteerDelete(@PathVariable Long id) {
        volunteerService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/volunteer/reset-password")
    public Result<?> volunteerResetPassword(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        if (id == null) return Result.error(400, "缺少ID");
        SysVolunteer v = volunteerService.findById(id);
        if (v == null) return Result.error(404, "志愿者不存在");
        if (v.getUserId() == null) return Result.error("志愿者未绑定账号，无法重置密码");
        String newPassword = str(body.get("newPassword"));
        if (newPassword == null || newPassword.isBlank()) newPassword = "123456";
        userMapper.updatePassword(PasswordUtil.encode(newPassword), v.getUserId());
        return Result.success("密码已重置为: " + newPassword);
    }

    // ==================== Counselor Excel Import ====================

    @GetMapping("/counselor/template")
    public void downloadCounselorTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("辅导员导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<CounselorImportRow> rows = new ArrayList<>();
        CounselorImportRow example = new CounselorImportRow();
        example.setRealName("张老师");
        example.setWorkNo("T2026001");
        example.setCollegeCode("CS");
        example.setPhone("13800138000");
        example.setEmail("zhang@example.com");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), CounselorImportRow.class)
                .sheet("辅导员数据").doWrite(rows);
    }

    @PostMapping("/counselor/import")
    public Result<ImportResult> importCounselors(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<CounselorImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), CounselorImportRow.class, new ReadListener<CounselorImportRow>() {
                public void invoke(CounselorImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            CounselorImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getRealName() == null || row.getRealName().isBlank()) {
                    result.addError("第" + rowNum + "行：姓名不能为空"); continue;
                }
                if (row.getWorkNo() == null || row.getWorkNo().isBlank()) {
                    result.addError("第" + rowNum + "行：工号不能为空"); continue;
                }
                if (row.getCollegeCode() == null || row.getCollegeCode().isBlank()) {
                    result.addError("第" + rowNum + "行：学院编码不能为空"); continue;
                }

                SysCollege college = collegeMapper.findByCode(row.getCollegeCode().trim());
                if (college == null) {
                    result.addError("第" + rowNum + "行：学院编码不存在「" + row.getCollegeCode() + "」"); continue;
                }

                SysCounselor counselor = new SysCounselor();
                counselor.setCollegeId(college.getId());
                counselor.setRealName(row.getRealName().trim());
                counselor.setWorkNo(row.getWorkNo().trim());
                counselor.setPhone(row.getPhone());
                counselor.setEmail(row.getEmail());
                counselorService.create(counselor, List.of());
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }

    // ==================== Volunteer Excel Import ====================

    @GetMapping("/volunteer/template")
    public void downloadVolunteerTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("志愿者导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<VolunteerImportRow> rows = new ArrayList<>();
        VolunteerImportRow example = new VolunteerImportRow();
        example.setRealName("李同学");
        example.setPhone("13900139000");
        example.setVolunteerType("钥匙发放");
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), VolunteerImportRow.class)
                .sheet("志愿者数据").doWrite(rows);
    }

    @PostMapping("/volunteer/import")
    public Result<ImportResult> importVolunteers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<VolunteerImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), VolunteerImportRow.class, new ReadListener<VolunteerImportRow>() {
                public void invoke(VolunteerImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            VolunteerImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getRealName() == null || row.getRealName().isBlank()) {
                    result.addError("第" + rowNum + "行：姓名不能为空"); continue;
                }
                if (row.getPhone() == null || row.getPhone().isBlank()) {
                    result.addError("第" + rowNum + "行：联系电话不能为空"); continue;
                }

                Integer type = parseVolunteerType(row.getVolunteerType());
                if (type == null) {
                    result.addError("第" + rowNum + "行：志愿者类型无效，请填写「钥匙发放」或「军训服发放」"); continue;
                }

                SysVolunteer volunteer = new SysVolunteer();
                volunteer.setRealName(row.getRealName().trim());
                volunteer.setPhone(row.getPhone().trim());
                volunteer.setVolunteerType(type);
                volunteer.setStatus(1);
                volunteerService.create(volunteer);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }

    private Integer parseVolunteerType(String type) {
        if (type == null) return null;
        String t = type.trim();
        if (t.equals("钥匙发放") || t.equals("1")) return 1;
        if (t.equals("军训服发放") || t.equals("2")) return 2;
        return null;
    }

    // ==================== Student Info ====================

    @GetMapping("/student/list")
    public Result<List<StuInfo>> studentList() {
        return Result.success(stuInfoService.findAll());
    }

    @GetMapping("/student/stats")
    public Result<Map<String, Object>> studentStats() {
        return Result.success(stuInfoService.getStats());
    }

    @GetMapping("/student/{id}")
    public Result<StuInfo> studentGet(@PathVariable Long id) {
        StuInfo s = stuInfoService.findById(id);
        if (s == null) return Result.error(404, "学生不存在");
        return Result.success(s);
    }

    @PostMapping("/student")
    public Result<StuInfo> studentCreate(@RequestBody StuInfo stuInfo) {
        try {
            return Result.success(stuInfoService.create(stuInfo));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/student")
    public Result<StuInfo> studentUpdate(@RequestBody StuInfo stuInfo) {
        try {
            return Result.success(stuInfoService.update(stuInfo));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/student/{id}")
    public Result<Void> studentDelete(@PathVariable Long id) {
        stuInfoService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/student/reset-password")
    public Result<?> studentResetPassword(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        if (id == null) return Result.error(400, "缺少ID");
        StuInfo stu = stuInfoService.findById(id);
        if (stu == null) return Result.error(404, "学生不存在");
        if (stu.getUserId() == null) return Result.error("学生未绑定账号，无法重置密码");
        String newPassword = str(body.get("newPassword"));
        if (newPassword == null || newPassword.isBlank()) {
            if (stu.getIdCard() == null || stu.getIdCard().length() < 6) {
                return Result.error("身份证号无效，无法生成默认密码");
            }
            newPassword = stu.getIdCard().substring(stu.getIdCard().length() - 6);
        }
        userMapper.updatePassword(PasswordUtil.encode(newPassword), stu.getUserId());
        return Result.success("密码已重置为: " + newPassword);
    }

    // ==================== Excel Import ====================

    @GetMapping("/student/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("新生导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<StuImportRow> templateRows = new ArrayList<>();
        StuImportRow example1 = new StuImportRow();
        example1.setName("张三");
        example1.setStudentNo("2026001");
        example1.setExamNo("26420100100001");
        example1.setIdCard("420100200601011234");
        example1.setGender("男");
        example1.setPhone("13800138000");
        example1.setEmail("zhangsan@example.com");
        example1.setCollegeCode("CS");
        example1.setMajorCode("CS01");
        example1.setClassCode("CS202601");
        templateRows.add(example1);

        EasyExcel.write(response.getOutputStream(), StuImportRow.class)
                .sheet("新生数据")
                .doWrite(templateRows);
    }

    @PostMapping("/student/import")
    public Result<ImportResult> importStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的Excel文件");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls"))) {
            return Result.error("仅支持 .xlsx 或 .xls 格式的Excel文件");
        }

        ImportResult importResult = new ImportResult();
        List<StuImportRow> allRows = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), StuImportRow.class, new ReadListener<StuImportRow>() {
                @Override
                public void invoke(StuImportRow row, AnalysisContext context) {
                    allRows.add(row);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // processed after read
                }
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }

        importResult.setTotalRows(allRows.size());

        for (int i = 0; i < allRows.size(); i++) {
            StuImportRow row = allRows.get(i);
            int rowNum = i + 2; // Excel row (header is row 1)

            // Validate required fields
            if (row.getName() == null || row.getName().isBlank()) {
                importResult.addError("第" + rowNum + "行：姓名不能为空");
                continue;
            }
            if (row.getIdCard() == null || row.getIdCard().isBlank()) {
                importResult.addError("第" + rowNum + "行：身份证号不能为空");
                continue;
            }
            if (row.getIdCard().trim().length() != 18) {
                importResult.addError("第" + rowNum + "行：身份证号格式不正确（需18位）");
                continue;
            }

            try {
                StuInfo stuInfo = new StuInfo();
                stuInfo.setName(row.getName().trim());
                stuInfo.setStudentNo(row.getStudentNo() != null ? row.getStudentNo().trim() : null);
                stuInfo.setExamNo(row.getExamNo() != null ? row.getExamNo().trim() : null);
                stuInfo.setIdCard(row.getIdCard().trim());
                stuInfo.setGender("男".equals(row.getGender()) || "1".equals(row.getGender()) ? 1 : 2);
                stuInfo.setPhone(row.getPhone() != null ? row.getPhone().trim() : null);
                stuInfo.setEmail(row.getEmail() != null ? row.getEmail().trim() : null);

                // Resolve college by code
                if (row.getCollegeCode() != null && !row.getCollegeCode().isBlank()) {
                    SysCollege college = collegeMapper.findByCode(row.getCollegeCode().trim());
                    if (college == null) {
                        importResult.addError("第" + rowNum + "行：学院编码不存在「" + row.getCollegeCode() + "」");
                        continue;
                    }
                    stuInfo.setCollegeId(college.getId());
                }

                // Resolve major by code
                if (row.getMajorCode() != null && !row.getMajorCode().isBlank()) {
                    SysMajor major = majorMapper.findByCode(row.getMajorCode().trim());
                    if (major == null) {
                        importResult.addError("第" + rowNum + "行：专业编码不存在「" + row.getMajorCode() + "」");
                        continue;
                    }
                    stuInfo.setMajorId(major.getId());
                }

                // Resolve class by code
                if (row.getClassCode() != null && !row.getClassCode().isBlank()) {
                    SysClass cls = classMapper.findByCode(row.getClassCode().trim());
                    if (cls == null) {
                        importResult.addError("第" + rowNum + "行：班级编码不存在「" + row.getClassCode() + "」");
                        continue;
                    }
                    stuInfo.setClassId(cls.getId());
                }

                stuInfoService.create(stuInfo);
                importResult.setSuccessCount(importResult.getSuccessCount() + 1);

            } catch (RuntimeException e) {
                importResult.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }

        return Result.success("导入完成", importResult);
    }

    // ==================== Helpers ====================

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}
