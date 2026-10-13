package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.StuInfo;
import com.example.backend.mapper.KeyDistributionMapper;
import com.example.backend.mapper.UniformDistributionMapper;
import com.example.backend.entity.KeyDistribution;
import com.example.backend.entity.UniformDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteer")
@CrossOrigin
public class VolunteerController {

    @Autowired private KeyDistributionMapper keyMapper;
    @Autowired private UniformDistributionMapper uniformMapper;

    // ===== Key Distribution =====

    @GetMapping("/scan/key")
    public Result<?> scanKey(@RequestParam String idCard) {
        StuInfo student = keyMapper.findStudentByIdCard(idCard);
        if (student == null) return Result.error(404, "未找到该学生信息");
        if (student.getHasKey() != null && student.getHasKey() > 0) {
            return Result.error("该学生已经领取过钥匙");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getId());
        data.put("name", student.getName());
        data.put("idCard", student.getIdCard());
        data.put("phone", student.getPhone());
        data.put("collegeName", student.getCollegeName());
        data.put("majorName", student.getMajorName());
        data.put("className", student.getClassName());
        data.put("buildingName", student.getBuildingName());
        data.put("roomNo", student.getRoomNo());
        data.put("bedNo", student.getBedNo());
        data.put("roomId", student.getRoomId());
        return Result.success(data);
    }

    @PostMapping("/scan/key")
    public Result<?> distributeKey(@RequestBody Map<String, Long> body) {
        Long studentId = body.get("studentId");
        Long roomId = body.get("roomId");
        Long volunteerId = body.get("volunteerId");
        if (studentId == null || roomId == null || volunteerId == null) return Result.error(400, "参数不完整");

        KeyDistribution exists = keyMapper.findByStudentId(studentId);
        if (exists != null) return Result.error("该学生已经领取过钥匙");

        KeyDistribution record = new KeyDistribution();
        record.setStudentId(studentId);
        record.setRoomId(roomId);
        record.setVolunteerId(volunteerId);
        keyMapper.insert(record);
        return Result.success("钥匙发放成功");
    }

    @GetMapping("/stats/key")
    public Result<?> keyStats(@RequestParam Long volunteerId) {
        Map<String, Object> data = new HashMap<>();
        data.put("distributed", keyMapper.countDistributed(volunteerId));
        return Result.success(data);
    }

    // ===== Uniform Distribution =====

    @GetMapping("/scan/uniform")
    public Result<?> scanUniform(@RequestParam String idCard) {
        StuInfo student = uniformMapper.findStudentWithSize(idCard);
        if (student == null) return Result.error(404, "未找到该学生信息");
        if (student.getHasUniform() != null && student.getHasUniform() > 0) {
            return Result.error("该学生已经领取过军训服");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getId());
        data.put("name", student.getName());
        data.put("idCard", student.getIdCard());
        data.put("phone", student.getPhone());
        data.put("collegeName", student.getCollegeName());
        data.put("majorName", student.getMajorName());
        data.put("className", student.getClassName());
        data.put("clothingSize", student.getClothingSize());
        data.put("shoeSize", student.getShoeSize());
        return Result.success(data);
    }

    @PostMapping("/scan/uniform")
    public Result<?> distributeUniform(@RequestBody Map<String, Object> body) {
        Long studentId = toLong(body.get("studentId"));
        Long volunteerId = toLong(body.get("volunteerId"));
        String clothingSize = (String) body.get("clothingSize");
        String shoeSize = (String) body.get("shoeSize");
        if (studentId == null || volunteerId == null) return Result.error(400, "参数不完整");

        UniformDistribution exists = uniformMapper.findByStudentId(studentId);
        if (exists != null) return Result.error("该学生已经领取过军训服");

        UniformDistribution record = new UniformDistribution();
        record.setStudentId(studentId);
        record.setVolunteerId(volunteerId);
        record.setClothingSize(clothingSize);
        record.setShoeSize(shoeSize);
        uniformMapper.insert(record);
        return Result.success("军训服发放成功");
    }

    @GetMapping("/stats/uniform")
    public Result<?> uniformStats(@RequestParam Long volunteerId) {
        Map<String, Object> data = new HashMap<>();
        data.put("distributed", uniformMapper.countDistributed(volunteerId));
        return Result.success(data);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
