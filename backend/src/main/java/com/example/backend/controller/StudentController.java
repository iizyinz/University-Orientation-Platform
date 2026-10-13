package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    @Autowired private StuInfoMapper stuInfoMapper;
    @Autowired private StuUniformSizeMapper uniformSizeMapper;
    @Autowired private StuReportCodeMapper reportCodeMapper;
    @Autowired private StuDormChoiceMapper dormChoiceMapper;
    @Autowired private SysCounselorMapper counselorMapper;
    @Autowired private DormChooseConfigMapper configMapper;
    @Autowired private DormMajorAllocMapper allocMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private KeyDistributionMapper keyMapper;
    @Autowired private UniformDistributionMapper uniformDistMapper;
    @Autowired private CmsNoticeMapper noticeMapper;
    @Autowired private CmsHandbookMapper handbookMapper;
    @Autowired private CmsCampusMapMapper campusMapMapper;
    @Autowired private GroupConfigMapper groupConfigMapper;
    @Autowired private FaqCategoryMapper faqCategoryMapper;
    @Autowired private FaqKnowledgeMapper faqKnowledgeMapper;
    @Autowired private FeeItemMapper feeItemMapper;
    @Autowired private FeeMajorConfigMapper feeMajorConfigMapper;
    @Autowired private StuDocumentMapper documentMapper;
    @Autowired private S3Service s3Service;

    @GetMapping("/info")
    public Result<?> getStudentInfo(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        Map<String, Object> data = new HashMap<>();
        // 基础信息（只读）
        data.put("id", student.getId());
        data.put("name", student.getName());
        data.put("studentNo", student.getStudentNo());
        data.put("idCard", student.getIdCard());
        data.put("gender", student.getGender());
        data.put("birthday", student.getBirthday());
        data.put("collegeName", student.getCollegeName());
        data.put("majorName", student.getMajorName());
        data.put("className", student.getClassName());
        data.put("reportStatus", student.getReportStatus());
        data.put("infoCompleted", student.getInfoCompleted());
        // 可编辑字段
        data.put("nation", student.getNation());
        data.put("politicalStatus", student.getPoliticalStatus());
        data.put("phone", student.getPhone());
        data.put("email", student.getEmail());
        data.put("province", student.getProvince());
        data.put("city", student.getCity());
        data.put("district", student.getDistrict());
        data.put("healthStatus", student.getHealthStatus());
        data.put("hobby", student.getHobby());
        data.put("emergencyContactName", student.getEmergencyContactName());
        data.put("emergencyContactRelation", student.getEmergencyContactRelation());
        data.put("emergencyContactPhone", student.getEmergencyContactPhone());

        // 军训服尺码
        StuUniformSize uniformSize = uniformSizeMapper.findByStudentId(student.getId());
        if (uniformSize != null) {
            data.put("clothingSize", uniformSize.getClothingSize());
            data.put("shoeSize", uniformSize.getShoeSize());
        } else {
            data.put("clothingSize", "");
            data.put("shoeSize", "");
        }

        return Result.success(data);
    }

    @PutMapping("/info")
    public Result<?> updateStudentInfo(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        if (userId == null) return Result.error(400, "缺少用户ID");

        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        // 只允许更新可编辑字段
        if (body.containsKey("nation")) student.setNation(str(body.get("nation")));
        if (body.containsKey("politicalStatus")) student.setPoliticalStatus(str(body.get("politicalStatus")));
        if (body.containsKey("phone")) student.setPhone(str(body.get("phone")));
        if (body.containsKey("email")) student.setEmail(str(body.get("email")));
        if (body.containsKey("province")) student.setProvince(str(body.get("province")));
        if (body.containsKey("city")) student.setCity(str(body.get("city")));
        if (body.containsKey("district")) student.setDistrict(str(body.get("district")));
        if (body.containsKey("healthStatus")) student.setHealthStatus(str(body.get("healthStatus")));
        if (body.containsKey("hobby")) student.setHobby(str(body.get("hobby")));
        if (body.containsKey("emergencyContactName")) student.setEmergencyContactName(str(body.get("emergencyContactName")));
        if (body.containsKey("emergencyContactRelation")) student.setEmergencyContactRelation(str(body.get("emergencyContactRelation")));
        if (body.containsKey("emergencyContactPhone")) student.setEmergencyContactPhone(str(body.get("emergencyContactPhone")));

        stuInfoMapper.update(student);

        // 更新信息完善状态
        boolean completed = isInfoCompleted(student);
        if (completed && (student.getInfoCompleted() == null || student.getInfoCompleted() == 0)) {
            stuInfoMapper.updateInfoCompleted(student.getId(), 1);
        }

        return Result.success("保存成功");
    }

    @PutMapping("/uniform-size")
    public Result<?> updateUniformSize(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        if (userId == null) return Result.error(400, "缺少用户ID");

        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        String clothingSize = body.get("clothingSize") != null ? body.get("clothingSize").toString() : null;
        String shoeSize = body.get("shoeSize") != null ? body.get("shoeSize").toString() : null;

        if (clothingSize == null || clothingSize.isBlank()) return Result.error("请选择衣服尺码");
        if (shoeSize == null || shoeSize.isBlank()) return Result.error("请输入鞋码");

        int shoeNum;
        try {
            shoeNum = Integer.parseInt(shoeSize);
        } catch (NumberFormatException e) {
            return Result.error("鞋码格式不正确");
        }
        if (shoeNum < 35 || shoeNum > 46) return Result.error("鞋码范围为35-46");

        StuUniformSize size = new StuUniformSize();
        size.setStudentId(student.getId());
        size.setClothingSize(clothingSize);
        size.setShoeSize(shoeSize);
        uniformSizeMapper.upsert(size);

        return Result.success("尺码保存成功");
    }

    // ===== Report Code =====

    @GetMapping("/report-code")
    public Result<?> getReportCode(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        StuReportCode reportCode = reportCodeMapper.findByStudentId(student.getId());
        if (reportCode == null) {
            // 自动生成报到码（使用身份证号作为二维码内容）
            reportCode = new StuReportCode();
            reportCode.setStudentId(student.getId());
            reportCode.setCodeContent(student.getIdCard());
            reportCode.setExpireTime(LocalDateTime.now().plusDays(30));
            reportCodeMapper.upsert(reportCode);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("codeContent", reportCode.getCodeContent());
        data.put("isUsed", reportCode.getIsUsed());
        data.put("expireTime", reportCode.getExpireTime());
        data.put("reportStatus", student.getReportStatus());
        return Result.success(data);
    }

    // ===== Flow Status =====

    @GetMapping("/flow-status")
    public Result<?> getFlowStatus(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        // 判断各步骤完成状态（从实际分布表查询）
        boolean step1Done = student.getInfoCompleted() != null && student.getInfoCompleted() == 1;
        StuDormChoice dormChoice = dormChoiceMapper.findByStudentId(student.getId());
        boolean step2Done = dormChoice != null;
        boolean step3Done = student.getReportStatus() != null && student.getReportStatus() == 1;
        boolean step4Done = keyMapper.countByStudentId(student.getId()) > 0;
        boolean step5Done = uniformDistMapper.countByStudentId(student.getId()) > 0;

        Map<String, Object> data = new HashMap<>();
        data.put("reportStatus", student.getReportStatus());
        data.put("steps", new Object[]{
            Map.of("step", 1, "name", "在线填报信息", "done", step1Done),
            Map.of("step", 2, "name", "在线选择宿舍", "done", step2Done),
            Map.of("step", 3, "name", "现场扫码报到", "done", step3Done),
            Map.of("step", 4, "name", "领取宿舍钥匙", "done", step4Done),
            Map.of("step", 5, "name", "领取军训服装", "done", step5Done),
        });
        return Result.success(data);
    }

    // ===== Notices =====

    @GetMapping("/notices")
    public Result<?> getNotices(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(noticeMapper.findPublished(limit));
    }

    @GetMapping("/notices/{id}")
    public Result<?> getNoticeDetail(@PathVariable Long id) {
        CmsNotice n = noticeMapper.findById(id);
        if (n == null) return Result.error(404, "公告不存在");
        return Result.success(n);
    }

    // ===== Handbook =====

    @GetMapping("/handbook/list")
    public Result<?> getHandbookList() {
        return Result.success(handbookMapper.findAll().stream()
            .filter(h -> h.getPublishStatus() != null && h.getPublishStatus() == 1)
            .toList());
    }

    @GetMapping("/handbook/{id}")
    public Result<?> getHandbookDetail(@PathVariable Long id) {
        CmsHandbook h = handbookMapper.findById(id);
        if (h == null) return Result.error(404, "手册不存在");
        return Result.success(h);
    }

    // ===== Campus Map =====

    @GetMapping("/campus-map/list")
    public Result<?> getCampusMapList() {
        return Result.success(campusMapMapper.findAll().stream()
            .filter(m -> m.getStatus() != null && m.getStatus() == 1)
            .toList());
    }

    // ===== Student Groups =====

    @GetMapping("/groups")
    public Result<?> getMyGroups(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        List<GroupConfig> allGroups = groupConfigMapper.findAll();
        List<Map<String, Object>> myGroups = new ArrayList<>();

        for (GroupConfig g : allGroups) {
            if (g.getStatus() != null && g.getStatus() != 1) continue;

            boolean match = false;
            // 班级群：匹配学生的班级ID
            if (g.getGroupType() != null && g.getGroupType() == 3) {
                match = student.getClassId() != null && student.getClassId().equals(g.getTargetId());
            }
            // 学院群：匹配学生的学院ID
            if (g.getGroupType() != null && g.getGroupType() == 2) {
                match = student.getCollegeId() != null && student.getCollegeId().equals(g.getTargetId());
            }
            // 生源地群：匹配学生的省份
            if (g.getGroupType() != null && g.getGroupType() == 1) {
                String stuProvince = student.getProvince();
                String groupProvince = g.getProvince();
                match = stuProvince != null && !stuProvince.isBlank() 
                    && groupProvince != null && !groupProvince.isBlank()
                    && stuProvince.equals(groupProvince);
            }
            if (!match) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("groupType", g.getGroupType());
            item.put("groupName", g.getGroupName());
            item.put("groupNo", g.getGroupNo());
            item.put("groupQrImage", g.getGroupQrImage());
            item.put("targetId", g.getTargetId());
            item.put("targetName", g.getTargetName());

            // 如果是班级群，查询该班级的辅导员信息
            if (g.getGroupType() != null && g.getGroupType() == 3 && g.getTargetId() != null) {
                SysCounselor counselor = counselorMapper.findByClassId(g.getTargetId());
                if (counselor != null) {
                    item.put("counselorName", counselor.getRealName());
                    item.put("counselorPhone", counselor.getPhone());
                    item.put("counselorEmail", counselor.getEmail());
                }
            }

            myGroups.add(item);
        }
        return Result.success(myGroups);
    }

    // ===== FAQ =====

    @GetMapping("/faq/categories")
    public Result<?> getFaqCategories() {
        return Result.success(faqCategoryMapper.findAll().stream()
            .filter(c -> c.getStatus() != null && c.getStatus() == 1)
            .toList());
    }

    @GetMapping("/faq/search")
    public Result<?> searchFaq(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) return Result.success(List.of());
        String kw = keyword.trim().toLowerCase();
        List<FaqKnowledge> all = faqKnowledgeMapper.findAll();
        return Result.success(all.stream()
            .filter(f -> f.getStatus() != null && f.getStatus() == 1)
            .filter(f -> {
                if (f.getQuestion() != null && f.getQuestion().toLowerCase().contains(kw)) return true;
                if (f.getKeywords() != null && f.getKeywords().toLowerCase().contains(kw)) return true;
                if (f.getCategoryName() != null && f.getCategoryName().toLowerCase().contains(kw)) return true;
                return false;
            })
            .toList());
    }

    @GetMapping("/faq/by-category/{categoryId}")
    public Result<?> getFaqByCategory(@PathVariable Long categoryId) {
        return Result.success(faqKnowledgeMapper.findByCategoryId(categoryId).stream()
            .filter(f -> f.getStatus() != null && f.getStatus() == 1)
            .toList());
    }

    @PostMapping("/faq/hit/{id}")
    public Result<?> hitFaq(@PathVariable Long id) {
        faqKnowledgeMapper.incrementHitCount(id);
        return Result.success(null);
    }

    // ===== Fee =====

    @GetMapping("/fee")
    public Result<?> getMyFee(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");
        if (student.getMajorId() == null) return Result.success(List.of());

        List<FeeMajorConfig> configs = feeMajorConfigMapper.findByMajorId(student.getMajorId());
        return Result.success(configs);
    }

    // ===== Document Upload =====

    @GetMapping("/documents")
    public Result<?> getDocuments(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");
        return Result.success(documentMapper.findByStudentId(student.getId()));
    }

    @PostMapping("/documents/upload")
    public Result<?> uploadDocument(@RequestParam Long userId,
                                     @RequestParam Integer docType,
                                     @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.error("仅支持图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        try {
            String url = s3Service.upload(file, "stu-documents");

            // 如果该类型已有图片，先删除旧的
            documentMapper.deleteByStudentIdAndType(student.getId(), docType);

            StuDocument doc = new StuDocument();
            doc.setStudentId(student.getId());
            doc.setDocType(docType);
            doc.setFileName(file.getOriginalFilename());
            doc.setFileUrl(url);
            doc.setFileSize(file.getSize());
            documentMapper.insert(doc);

            Map<String, Object> data = new HashMap<>();
            data.put("id", doc.getId());
            data.put("url", url);
            data.put("docType", docType);
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/documents/{id}")
    public Result<?> deleteDocument(@PathVariable Long id) {
        documentMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ===== Dorm Selection =====

    @GetMapping("/dorm/config")
    public Result<?> getDormConfig() {
        List<DormChooseConfig> configs = configMapper.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (DormChooseConfig c : configs) {
            if (c.getStartTime() != null && c.getEndTime() != null) {
                if (now.isBefore(c.getStartTime())) {
                    return Result.success(Map.of("status", "not_started", "configName", c.getConfigName(),
                        "startTime", c.getStartTime(), "endTime", c.getEndTime()));
                } else if (now.isAfter(c.getStartTime()) && now.isBefore(c.getEndTime())) {
                    return Result.success(Map.of("status", "in_progress", "configName", c.getConfigName(),
                        "startTime", c.getStartTime(), "endTime", c.getEndTime()));
                }
            }
        }
        // 检查是否已结束（最后一个配置的结束时间已过）
        if (!configs.isEmpty()) {
            DormChooseConfig last = configs.get(0);
            if (last.getEndTime() != null && now.isAfter(last.getEndTime())) {
                return Result.success(Map.of("status", "ended", "configName", last.getConfigName()));
            }
        }
        return Result.success(Map.of("status", "not_started"));
    }

    @GetMapping("/dorm/alloc")
    public Result<?> getDormAlloc(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");
        if (student.getMajorId() == null || student.getGender() == null) return Result.success(Map.of("buildings", List.of(), "rooms", List.of()));

        List<DormMajorAlloc> allocs = allocMapper.findAll();
        // 筛选该专业+性别分配的宿舍
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> buildingIds = new HashSet<>();
        for (DormMajorAlloc a : allocs) {
            if (a.getMajorId().equals(student.getMajorId()) && a.getGender() != null && a.getGender().equals(student.getGender())) {
                int available = a.getAllocBedCount() - (a.getUsedBedCount() != null ? a.getUsedBedCount() : 0);
                if (available > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("roomId", a.getRoomId());
                    item.put("buildingName", a.getBuildingName());
                    item.put("roomNo", a.getRoomNo());
                    item.put("allocBedCount", a.getAllocBedCount());
                    item.put("usedBedCount", a.getUsedBedCount() != null ? a.getUsedBedCount() : 0);
                    item.put("available", available);
                    result.add(item);
                    if (a.getRoomId() != null) {
                        DormRoom room = roomMapper.findById(a.getRoomId());
                        if (room != null) buildingIds.add(room.getBuildingId());
                    }
                }
            }
        }

        // 按楼栋分组统计
        List<Map<String, Object>> buildings = new ArrayList<>();
        for (Long bid : buildingIds) {
            DormBuilding b = buildingMapper.findById(bid);
            if (b == null) continue;
            int totalAvail = 0;
            for (Map<String, Object> item : result) {
                DormRoom room = roomMapper.findById((Long) item.get("roomId"));
                if (room != null && room.getBuildingId().equals(bid)) {
                    totalAvail += (int) item.get("available");
                }
            }
            if (totalAvail > 0) {
                buildings.add(Map.of("id", bid, "name", b.getBuildingName(),
                    "floorCount", b.getFloorCount(), "available", totalAvail));
            }
        }

        return Result.success(Map.of("buildings", buildings, "rooms", result));
    }

    @GetMapping("/dorm/rooms")
    public Result<?> getDormRooms(@RequestParam Long buildingId, @RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        List<DormMajorAlloc> allocs = allocMapper.findAll();
        // 获取该专业在该楼栋下分配的房间
        List<Long> allowedRoomIds = allocs.stream()
            .filter(a -> a.getMajorId().equals(student.getMajorId()))
            .map(DormMajorAlloc::getRoomId)
            .collect(Collectors.toList());

        List<DormRoom> allRooms = roomMapper.findByBuildingId(buildingId);
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (DormRoom r : allRooms) {
            if (!allowedRoomIds.contains(r.getId())) continue;
            // 查找该房间的分配信息
            for (DormMajorAlloc a : allocs) {
                if (a.getRoomId().equals(r.getId()) && a.getMajorId().equals(student.getMajorId())) {
                    int available = a.getAllocBedCount() - (a.getUsedBedCount() != null ? a.getUsedBedCount() : 0);
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", r.getId());
                    item.put("roomNo", r.getRoomNo());
                    item.put("floorNo", r.getFloorNo());
                    item.put("roomType", r.getRoomType());
                    item.put("bedCount", r.getBedCount());
                    item.put("available", available);
                    // 获取已选床位号
                    List<Integer> usedBeds = getUsedBeds(r.getId());
                    item.put("usedBeds", usedBeds);
                    rooms.add(item);
                    break;
                }
            }
        }
        return Result.success(rooms);
    }

    @PostMapping("/dorm/choose")
    public Result<?> chooseDorm(@RequestBody Map<String, Object> body) {
        Long userId = toLong(body.get("userId"));
        Long roomId = toLong(body.get("roomId"));
        Object bedNoObj = body.get("bedNo");
        Integer bedNo = bedNoObj != null ? Integer.parseInt(bedNoObj.toString()) : null;

        if (userId == null || roomId == null || bedNo == null) {
            return Result.error(400, "参数不完整");
        }

        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        // 检查是否已选过
        StuDormChoice existing = dormChoiceMapper.findByStudentId(student.getId());
        if (existing != null) {
            return Result.error("您已选择过宿舍，如需更改请联系管理员");
        }

        // 检查床位是否已被占用
        int bedUsed = dormChoiceMapper.countBedUsed(roomId, bedNo);
        if (bedUsed > 0) {
            return Result.error("该床位已被其他同学选择");
        }

        // 检查该房间是否在该专业+性别的分配范围内
        List<DormMajorAlloc> allocs = allocMapper.findAll();
        boolean allowed = allocs.stream().anyMatch(a ->
            a.getRoomId().equals(roomId) && a.getMajorId().equals(student.getMajorId())
            && a.getGender() != null && a.getGender().equals(student.getGender()));
        if (!allowed) {
            return Result.error("该房间不在您专业性别的可选范围内");
        }

        // 保存选择
        StuDormChoice choice = new StuDormChoice();
        choice.setStudentId(student.getId());
        choice.setRoomId(roomId);
        choice.setBedNo(bedNo);
        dormChoiceMapper.upsert(choice);

        // 更新 dorm_room.used_bed_count
        int usedCount = dormChoiceMapper.countByRoomId(roomId);
        roomMapper.updateUsedBedCount(roomId, usedCount);

        // 更新 dorm_major_alloc.used_bed_count（该房间该专业该性别的记录）
        List<DormMajorAlloc> roomAllocs = allocMapper.findByRoomId(roomId);
        for (DormMajorAlloc ra : roomAllocs) {
            if (ra.getMajorId().equals(student.getMajorId()) && ra.getGender() != null && ra.getGender().equals(student.getGender())) {
                allocMapper.updateUsedBedCount(roomId, ra.getMajorId(), ra.getGender(), usedCount);
            }
        }

        return Result.success("宿舍选择成功");
    }

    @GetMapping("/dorm/my")
    public Result<?> getMyDorm(@RequestParam Long userId) {
        StuInfo student = findStudentByUserId(userId);
        if (student == null) return Result.error(404, "学生信息不存在");

        StuDormChoice choice = dormChoiceMapper.findByStudentId(student.getId());
        if (choice == null) {
            return Result.success(null);
        }
        return Result.success(choice);
    }

    private List<Integer> getUsedBeds(Long roomId) {
        return dormChoiceMapper.findUsedBedsByRoomId(roomId);
    }

    private StuInfo findStudentByUserId(Long userId) {
        // 通过 userId 查找学生，需要先从 sys_user 关联 stu_info
        // stu_info 表有 user_id 字段
        return stuInfoMapper.findByUserId(userId);
    }

    private boolean isInfoCompleted(StuInfo s) {
        return s.getNation() != null && !s.getNation().isBlank()
                && s.getPhone() != null && !s.getPhone().isBlank()
                && s.getEmergencyContactName() != null && !s.getEmergencyContactName().isBlank()
                && s.getEmergencyContactPhone() != null && !s.getEmergencyContactPhone().isBlank();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}
