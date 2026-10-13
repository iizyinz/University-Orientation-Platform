package com.example.backend.controller;

import com.example.backend.common.ImportResult;
import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.service.*;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/dorm")
@CrossOrigin
public class DormController {

    @Autowired private DormBuildingService buildingService;
    @Autowired private DormRoomService roomService;
    @Autowired private DormMajorAllocService allocService;
    @Autowired private DormChooseConfigService configService;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormMajorAllocMapper allocMapper;
    @Autowired private SysMajorMapper majorMapper;

    // ==================== Building ====================

    @GetMapping("/building/list")
    public Result<List<DormBuilding>> buildingList() { return Result.success(buildingService.findAll()); }

    @GetMapping("/building/{id}")
    public Result<DormBuilding> buildingGet(@PathVariable Long id) {
        DormBuilding b = buildingService.findById(id);
        if (b == null) return Result.error(404, "楼栋不存在");
        return Result.success(b);
    }

    @PostMapping("/building")
    public Result<DormBuilding> buildingCreate(@RequestBody DormBuilding b) {
        try { return Result.success(buildingService.create(b)); }
        catch (RuntimeException e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/building")
    public Result<DormBuilding> buildingUpdate(@RequestBody DormBuilding b) {
        try { return Result.success(buildingService.update(b)); }
        catch (RuntimeException e) { return Result.error(e.getMessage()); }
    }

    @DeleteMapping("/building/{id}")
    public Result<Void> buildingDelete(@PathVariable Long id) { buildingService.delete(id); return Result.success(null); }

    // ==================== Room ====================

    @GetMapping("/room/list")
    public Result<List<DormRoom>> roomList() { return Result.success(roomService.findAll()); }

    @GetMapping("/room/byBuilding/{buildingId}")
    public Result<List<DormRoom>> roomByBuilding(@PathVariable Long buildingId) { return Result.success(roomService.findByBuildingId(buildingId)); }

    @GetMapping("/room/{id}")
    public Result<DormRoom> roomGet(@PathVariable Long id) {
        DormRoom r = roomService.findById(id);
        if (r == null) return Result.error(404, "房间不存在");
        return Result.success(r);
    }

    @PostMapping("/room")
    public Result<DormRoom> roomCreate(@RequestBody DormRoom r) {
        try { return Result.success(roomService.create(r)); }
        catch (RuntimeException e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/room")
    public Result<DormRoom> roomUpdate(@RequestBody DormRoom r) {
        try { return Result.success(roomService.update(r)); }
        catch (RuntimeException e) { return Result.error(e.getMessage()); }
    }

    @DeleteMapping("/room/{id}")
    public Result<Void> roomDelete(@PathVariable Long id) { roomService.delete(id); return Result.success(null); }

    @PostMapping("/room/batch")
    public Result<?> roomBatchCreate(@RequestBody Map<String, Object> body) {
        Long buildingId = toLong(body.get("buildingId"));
        Integer floorNo = toInt(body.get("floorNo"));
        Integer count = toInt(body.get("count"));
        Integer bedCount = toInt(body.get("bedCount"));
        String roomType = str(body.get("roomType"));

        if (buildingId == null || floorNo == null || count == null || count < 1) {
            return Result.error(400, "参数不完整");
        }
        if (bedCount == null || bedCount < 1) bedCount = 4;
        if (roomType == null || roomType.isBlank()) roomType = "四人间";

        DormBuilding building = buildingMapper.findById(buildingId);
        if (building == null) return Result.error(404, "楼栋不存在");

        // 楼栋编号：从 buildingCode 或 buildingName 提取数字
        String prefix = building.getBuildingCode();
        if (prefix == null || prefix.isBlank()) {
            // 从 buildingName 提取数字，如 "1号楼" -> "1"
            String name = building.getBuildingName();
            prefix = name.replaceAll("[^0-9]", "");
            if (prefix.isEmpty()) prefix = "1";
        }

        int created = 0;
        int skipped = 0;
        for (int i = 1; i <= count; i++) {
            // 房间号格式：楼栋编号 + 楼层 + 序号（3位补零）
            String roomNo = prefix + "-" + floorNo + String.format("%03d", i);
            // 检查是否已存在
            if (roomMapper.countByUnique(buildingId, floorNo, roomNo) > 0) {
                skipped++;
                continue;
            }
            DormRoom room = new DormRoom();
            room.setBuildingId(buildingId);
            room.setFloorNo(floorNo);
            room.setRoomNo(roomNo);
            room.setBedCount(bedCount);
            room.setRoomType(roomType);
            room.setStatus(1);
            roomMapper.insert(room);
            created++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", created);
        result.put("skipped", skipped);
        result.put("total", count);
        return Result.success("批量创建完成", result);
    }

    // ==================== Alloc ====================

    @GetMapping("/alloc/list")
    public Result<List<DormMajorAlloc>> allocList() { return Result.success(allocService.findAll()); }

    @PostMapping("/alloc")
    public Result<DormMajorAlloc> allocCreate(@RequestBody Map<String, Object> body) {
        try {
            DormMajorAlloc a = new DormMajorAlloc();
            a.setMajorId(toLong(body.get("majorId")));
            a.setRoomId(toLong(body.get("roomId")));
            a.setGender(toInt(body.get("gender")));
            a.setAllocBedCount(toInt(body.get("allocBedCount")));
            if (a.getMajorId() == null || a.getRoomId() == null || a.getGender() == null) {
                return Result.error(400, "专业、房间和性别不能为空");
            }
            return Result.success(allocService.create(a));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/alloc")
    public Result<DormMajorAlloc> allocUpdate(@RequestBody Map<String, Object> body) {
        try {
            DormMajorAlloc a = new DormMajorAlloc();
            a.setId(toLong(body.get("id")));
            a.setMajorId(toLong(body.get("majorId")));
            a.setRoomId(toLong(body.get("roomId")));
            a.setGender(toInt(body.get("gender")));
            a.setAllocBedCount(toInt(body.get("allocBedCount")));
            return Result.success(allocService.update(a));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/alloc/{id}")
    public Result<Void> allocDelete(@PathVariable Long id) { allocService.delete(id); return Result.success(null); }

    // ==================== Alloc Excel Import ====================

    @GetMapping("/alloc/template")
    public void downloadAllocTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("宿舍分配导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        List<DormAllocImportRow> rows = new ArrayList<>();
        DormAllocImportRow example = new DormAllocImportRow();
        example.setMajorCode("CS01");
        example.setGender("男");
        example.setBuildingName("1号楼");
        example.setRoomNo("1-1001");
        example.setAllocBedCount(4);
        rows.add(example);

        EasyExcel.write(response.getOutputStream(), DormAllocImportRow.class)
                .sheet("宿舍分配数据").doWrite(rows);
    }

    @PostMapping("/alloc/import")
    public Result<ImportResult> importAlloc(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            return Result.error("仅支持 .xlsx 或 .xls 格式");

        ImportResult result = new ImportResult();
        List<DormAllocImportRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), DormAllocImportRow.class, new ReadListener<DormAllocImportRow>() {
                public void invoke(DormAllocImportRow row, AnalysisContext ctx) { rows.add(row); }
                public void doAfterAllAnalysed(AnalysisContext ctx) {}
            }).sheet().doRead();
        } catch (IOException e) {
            return Result.error("文件读取失败：" + e.getMessage());
        }
        result.setTotalRows(rows.size());

        // 获取所有专业和房间用于查找
        List<SysMajor> allMajors = majorMapper.findAll();
        List<DormRoom> allRooms = roomMapper.findAll();

        for (int i = 0; i < rows.size(); i++) {
            DormAllocImportRow row = rows.get(i);
            int rowNum = i + 2;
            try {
                if (row.getMajorCode() == null || row.getMajorCode().isBlank()) {
                    result.addError("第" + rowNum + "行：专业编码不能为空"); continue;
                }
                if (row.getGender() == null || row.getGender().isBlank()) {
                    result.addError("第" + rowNum + "行：性别不能为空"); continue;
                }
                if (row.getRoomNo() == null || row.getRoomNo().isBlank()) {
                    result.addError("第" + rowNum + "行：房间号不能为空"); continue;
                }

                // 查找专业
                SysMajor major = majorMapper.findByCode(row.getMajorCode().trim());
                if (major == null) {
                    result.addError("第" + rowNum + "行：专业编码不存在「" + row.getMajorCode() + "」"); continue;
                }

                // 解析性别
                Integer gender = parseGender(row.getGender());
                if (gender == null) {
                    result.addError("第" + rowNum + "行：性别无效，请填写「男」或「女」"); continue;
                }

                // 查找房间
                String roomNo = row.getRoomNo().trim();
                DormRoom matchedRoom = null;
                for (DormRoom r : allRooms) {
                    if (r.getRoomNo() != null && r.getRoomNo().equals(roomNo)) {
                        matchedRoom = r;
                        break;
                    }
                }
                if (matchedRoom == null) {
                    result.addError("第" + rowNum + "行：房间号不存在「" + roomNo + "」"); continue;
                }

                // 检查是否已分配
                if (allocMapper.countByMajorRoomGender(major.getId(), matchedRoom.getId(), gender) > 0) {
                    result.addError("第" + rowNum + "行：该专业此性别已分配此房间"); continue;
                }

                // 创建分配
                int bedCount = row.getAllocBedCount() != null ? row.getAllocBedCount() : 4;
                DormMajorAlloc alloc = new DormMajorAlloc();
                alloc.setMajorId(major.getId());
                alloc.setRoomId(matchedRoom.getId());
                alloc.setGender(gender);
                alloc.setAllocBedCount(bedCount);
                allocService.create(alloc);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + rowNum + "行：" + e.getMessage());
            }
        }
        return Result.success("导入完成", result);
    }

    private Integer parseGender(String gender) {
        if (gender == null) return null;
        String g = gender.trim();
        if (g.equals("男") || g.equals("1")) return 1;
        if (g.equals("女") || g.equals("2")) return 2;
        return null;
    }

    // ==================== Choose Config ====================

    @GetMapping("/config/list")
    public Result<List<DormChooseConfig>> configList() { return Result.success(configService.findAll()); }

    @PostMapping("/config")
    public Result<DormChooseConfig> configCreate(@RequestBody Map<String, Object> body) {
        try {
            DormChooseConfig c = new DormChooseConfig();
            c.setConfigName(str(body.get("configName")));
            c.setStartTime(parseDateTime(str(body.get("startTime"))));
            c.setEndTime(parseDateTime(str(body.get("endTime"))));
            c.setStatus(body.get("status") != null ? toInt(body.get("status")) : 0);
            c.setDescription(str(body.get("description")));
            return Result.success(configService.create(c));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/config")
    public Result<DormChooseConfig> configUpdate(@RequestBody Map<String, Object> body) {
        try {
            DormChooseConfig c = new DormChooseConfig();
            c.setId(toLong(body.get("id")));
            c.setConfigName(str(body.get("configName")));
            c.setStartTime(parseDateTime(str(body.get("startTime"))));
            c.setEndTime(parseDateTime(str(body.get("endTime"))));
            c.setStatus(body.get("status") != null ? toInt(body.get("status")) : 0);
            c.setDescription(str(body.get("description")));
            return Result.success(configService.update(c));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/config/{id}")
    public Result<Void> configDelete(@PathVariable Long id) { configService.delete(id); return Result.success(null); }

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

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            return LocalDateTime.parse(s);
        }
    }
}
