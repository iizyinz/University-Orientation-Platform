package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardMapper dashboardMapper;
    @Autowired
    private StuInfoMapper stuInfoMapper;
    @Autowired
    private DormBuildingMapper buildingMapper;
    @Autowired
    private DormRoomMapper roomMapper;
    @Autowired
    private SysMajorMapper majorMapper;

    @GetMapping("/stats")
    public Result<?> getStats() {
        Map<String, Object> raw = dashboardMapper.getStats();
        int totalStudents = toInt(raw.get("totalStudents"));
        int reportedStudents = toInt(raw.get("reportedStudents"));
        int totalBeds = toInt(raw.get("totalBeds"));
        int usedBeds = toInt(raw.get("usedBeds"));

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("reportedStudents", reportedStudents);
        data.put("unreportedStudents", totalStudents - reportedStudents);
        data.put("reportRate", totalStudents > 0 ? Math.round(reportedStudents * 100.0 / totalStudents) : 0);
        data.put("totalBeds", totalBeds);
        data.put("usedBeds", usedBeds);
        data.put("dormRate", totalBeds > 0 ? Math.round(usedBeds * 100.0 / totalBeds) : 0);
        data.put("totalColleges", toInt(raw.get("totalColleges")));
        data.put("totalCounselors", toInt(raw.get("totalCounselors")));
        data.put("totalVolunteers", toInt(raw.get("totalVolunteers")));
        data.put("totalFaq", toInt(raw.get("totalFaq")));
        return Result.success(data);
    }

    @GetMapping("/chart/college-distribution")
    public Result<?> collegeDistribution() {
        List<Map<String, Object>> result = stuInfoMapper.countByCollege();
        return Result.success(result);
    }

    @GetMapping("/chart/gender-distribution")
    public Result<?> genderDistribution() {
        List<Map<String, Object>> result = stuInfoMapper.countByGender();
        return Result.success(result);
    }

    @GetMapping("/chart/dorm-occupancy")
    public Result<?> dormOccupancy() {
        List<Map<String, Object>> result = buildingMapper.findOccupancyStats();
        return Result.success(result);
    }

    @GetMapping("/chart/report-trend")
    public Result<?> reportTrend() {
        List<Map<String, Object>> result = majorMapper.countReportedByMajor();
        return Result.success(result);
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }
}
