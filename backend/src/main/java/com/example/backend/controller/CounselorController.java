package com.example.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.example.backend.common.Result;
import com.example.backend.entity.SysCounselor;
import com.example.backend.entity.SysCounselorClass;
import com.example.backend.entity.StuInfo;
import com.example.backend.entity.StuDocument;
import com.example.backend.entity.GroupConfig;
import com.example.backend.entity.StudentExportRow;
import com.example.backend.mapper.SysCounselorMapper;
import com.example.backend.mapper.StuInfoMapper;
import com.example.backend.mapper.GroupConfigMapper;
import com.example.backend.mapper.KeyDistributionMapper;
import com.example.backend.mapper.StuDocumentMapper;
import com.example.backend.mapper.StuDormChoiceMapper;
import com.example.backend.service.GroupConfigService;
import com.example.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselor")
@CrossOrigin
public class CounselorController {

    @Autowired private SysCounselorMapper counselorMapper;
    @Autowired private StuInfoMapper stuInfoMapper;
    @Autowired private KeyDistributionMapper keyMapper;
    @Autowired private GroupConfigService groupConfigService;
    @Autowired private GroupConfigMapper groupConfigMapper;
    @Autowired private S3Service s3Service;
    @Autowired private StuDocumentMapper documentMapper;
    @Autowired private StuDormChoiceMapper dormChoiceMapper;

    // ===== Dashboard =====

    @GetMapping("/dashboard/stats")
    public Result<?> dashboardStats(@RequestParam Long userId) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return Result.error(404, "辅导员信息不存在");

        List<SysCounselorClass> classes = counselorMapper.findClassesDetailByCounselorId(counselor.getId());
        int totalStudents = 0;
        int reportedStudents = 0;
        for (SysCounselorClass cc : classes) {
            totalStudents += cc.getStudentCount() != null ? cc.getStudentCount() : 0;
            reportedStudents += cc.getReportedCount() != null ? cc.getReportedCount() : 0;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("reportedStudents", reportedStudents);
        data.put("unreportedStudents", totalStudents - reportedStudents);
        data.put("reportRate", totalStudents > 0 ? Math.round(reportedStudents * 100.0 / totalStudents) : 0);
        data.put("totalClasses", classes.size());
        data.put("counselorName", counselor.getRealName());
        data.put("collegeName", counselor.getCollegeName());
        return Result.success(data);
    }

    // ===== Classes =====

    @GetMapping("/classes")
    public Result<?> listClasses(@RequestParam Long userId) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return Result.error(404, "辅导员信息不存在");
        List<SysCounselorClass> classes = counselorMapper.findClassesDetailByCounselorId(counselor.getId());
        return Result.success(classes);
    }

    // ===== Students =====

    @GetMapping("/students")
    public Result<?> listStudents(@RequestParam Long userId, @RequestParam(required = false) Long classId,
                                   @RequestParam(required = false) String keyword) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return Result.error(404, "辅导员信息不存在");

        List<SysCounselorClass> counselorClasses = counselorMapper.findClassesByCounselorId(counselor.getId());

        List<StuInfo> allStudents = new java.util.ArrayList<>();
        for (SysCounselorClass cc : counselorClasses) {
            List<StuInfo> students = stuInfoMapper.findByClassIdWithDorm(cc.getClassId());
            allStudents.addAll(students);
        }

        if (classId != null) {
            allStudents = allStudents.stream().filter(s -> classId.equals(s.getClassId())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            allStudents = allStudents.stream().filter(s ->
                (s.getName() != null && s.getName().toLowerCase().contains(kw)) ||
                (s.getStudentNo() != null && s.getStudentNo().toLowerCase().contains(kw)) ||
                (s.getIdCard() != null && s.getIdCard().contains(kw))
            ).toList();
        }

        return Result.success(allStudents);
    }

    @GetMapping("/students/export")
    public void exportStudents(@RequestParam Long userId,
                            @RequestParam(required = false) Long classId,
                            @RequestParam(required = false) String keyword,
                            HttpServletResponse response) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return;

        List<SysCounselorClass> counselorClasses = counselorMapper.findClassesByCounselorId(counselor.getId());

        List<StuInfo> allStudents = new ArrayList<>();
        for (SysCounselorClass cc : counselorClasses) {
            List<StuInfo> students = stuInfoMapper.findByClassIdWithDorm(cc.getClassId());
            allStudents.addAll(students);
        }

        if (classId != null) {
            allStudents = allStudents.stream().filter(s -> classId.equals(s.getClassId())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            allStudents = allStudents.stream().filter(s ->
                (s.getName() != null && s.getName().toLowerCase().contains(kw)) ||
                (s.getStudentNo() != null && s.getStudentNo().toLowerCase().contains(kw)) ||
                (s.getIdCard() != null && s.getIdCard().contains(kw))
            ).toList();
        }

        List<StudentExportRow> rows = new ArrayList<>();
        for (StuInfo s : allStudents) {
            StudentExportRow row = new StudentExportRow();
            row.setName(s.getName());
            row.setStudentNo(s.getStudentNo());
            row.setIdCard(s.getIdCard());
            row.setGender(s.getGender() != null ? (s.getGender() == 1 ? "男" : "女") : "");
            row.setNation(s.getNation());
            row.setPhone(s.getPhone());
            row.setEmail(s.getEmail());
            row.setCollegeName(s.getCollegeName());
            row.setMajorName(s.getMajorName());
            row.setClassName(s.getClassName());

            String hometown = "";
            if (s.getProvince() != null) hometown += s.getProvince();
            if (s.getCity() != null) hometown += s.getCity();
            if (s.getDistrict() != null) hometown += s.getDistrict();
            row.setHometown(hometown);

            row.setReportStatus(s.getReportStatus() != null && s.getReportStatus() == 1 ? "已报到" : "未报到");

            String dormInfo = "";
            if (s.getBuildingName() != null) dormInfo += s.getBuildingName();
            if (s.getRoomNo() != null) dormInfo += "-" + s.getRoomNo();
            if (s.getBedNo() != null) dormInfo += "-" + s.getBedNo() + "床";
            row.setDormInfo(dormInfo);

            row.setEmergencyContactName(s.getEmergencyContactName());
            row.setEmergencyContactPhone(s.getEmergencyContactPhone());

            rows.add(row);
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = java.net.URLEncoder.encode("学生信息", java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), StudentExportRow.class).sheet("学生信息").doWrite(rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/student/documents")
    public Result<?> getStudentDocuments(@RequestParam Long studentId) {
        List<StuDocument> docs = documentMapper.findByStudentId(studentId);
        return Result.success(docs);
    }

    // ===== Check-in =====

    @GetMapping("/scan/student")
    public Result<?> scanStudent(@RequestParam String idCard) {
        StuInfo student = stuInfoMapper.findByIdCard(idCard);
        if (student == null) return Result.error(404, "未找到该学生信息");

        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getId());
        data.put("name", student.getName());
        data.put("studentNo", student.getStudentNo());
        data.put("idCard", student.getIdCard());
        data.put("phone", student.getPhone());
        data.put("collegeName", student.getCollegeName());
        data.put("majorName", student.getMajorName());
        data.put("className", student.getClassName());
        data.put("reportStatus", student.getReportStatus());

        // 查询证件照
        List<StuDocument> docs = documentMapper.findByStudentId(student.getId());
        for (StuDocument doc : docs) {
            String key = switch (doc.getDocType()) {
                case 1 -> "idCardFront";
                case 2 -> "idCardBack";
                case 3 -> "photo";
                case 4 -> "otherDoc";
                default -> "doc_" + doc.getDocType();
            };
            data.put(key, doc.getFileUrl());
        }

        return Result.success(data);
    }

    @PostMapping("/scan/checkin")
    public Result<?> checkin(@RequestBody Map<String, Long> body) {
        Long studentId = body.get("studentId");
        if (studentId == null) return Result.error(400, "缺少学生ID");

        StuInfo student = stuInfoMapper.findById(studentId);
        if (student == null) return Result.error(404, "学生不存在");

        if (student.getReportStatus() != null && student.getReportStatus() == 1) {
            return Result.error("该学生已完成报到");
        }

        stuInfoMapper.updateReportStatus(studentId, 1);
        return Result.success("报到签到成功");
    }

    // ===== Group Management =====

    @GetMapping("/groups")
    public Result<?> listGroups(@RequestParam Long userId) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return Result.error(404, "辅导员信息不存在");
        List<GroupConfig> groups = groupConfigService.findByCounselorId(counselor.getId());
        return Result.success(groups);
    }

    @PostMapping("/groups/create")
    public Result<?> createGroup(@RequestBody GroupConfig group, @RequestParam Long userId) {
        SysCounselor counselor = counselorMapper.findByUserId(userId);
        if (counselor == null) return Result.error(404, "辅导员信息不存在");

        if (group.getTargetId() == null) return Result.error(400, "请选择班级");

        // 验证该辅导员是否管理这个班级
        List<SysCounselorClass> managedClasses = counselorMapper.findClassesByCounselorId(counselor.getId());
        boolean isManaged = managedClasses.stream().anyMatch(c -> c.getClassId().equals(group.getTargetId()));
        if (!isManaged) return Result.error(403, "您没有该班级的管理权限");

        // 检查该班级是否已有群
        int exists = groupConfigMapper.countByTargetTypeAndId(3, group.getTargetId());
        if (exists > 0) return Result.error("该班级已创建群，请勿重复添加");

        group.setGroupType(3);
        if (group.getGroupName() == null || group.getGroupName().isBlank()) {
            group.setGroupName(managedClasses.stream()
                .filter(c -> c.getClassId().equals(group.getTargetId()))
                .map(SysCounselorClass::getClassName)
                .findFirst().orElse("") + "班级群");
        }
        if (group.getStatus() == null) group.setStatus(1);
        if (group.getSortOrder() == null) group.setSortOrder(0);
        groupConfigService.create(group);
        return Result.success("创建成功", group);
    }

    @PutMapping("/groups/update")
    public Result<?> updateGroup(@RequestBody GroupConfig group) {
        if (group.getId() == null) return Result.error(400, "缺少群ID");
        GroupConfig existing = groupConfigService.findById(group.getId());
        if (existing == null) return Result.error(404, "群配置不存在");
        groupConfigService.update(group);
        return Result.success("保存成功");
    }

    @PostMapping("/groups/upload-qr")
    public Result<?> uploadGroupQr(@RequestParam("file") MultipartFile file, @RequestParam Long groupId) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.error("仅支持图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }
        try {
            String url = s3Service.upload(file, "group-qr");
            GroupConfig existing = groupConfigService.findById(groupId);
            if (existing == null) return Result.error(404, "群配置不存在");
            existing.setGroupQrImage(url);
            groupConfigService.update(existing);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
