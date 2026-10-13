package com.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM stu_info) AS totalStudents, " +
            "(SELECT COUNT(*) FROM stu_info WHERE report_status = 1) AS reportedStudents, " +
            "(SELECT COALESCE(SUM(bed_count), 0) FROM dorm_room) AS totalBeds, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice WHERE status = 1) AS usedBeds, " +
            "(SELECT COUNT(*) FROM sys_college) AS totalColleges, " +
            "(SELECT COUNT(*) FROM sys_counselor) AS totalCounselors, " +
            "(SELECT COUNT(*) FROM sys_volunteer) AS totalVolunteers, " +
            "(SELECT COUNT(*) FROM faq_knowledge) AS totalFaq")
    Map<String, Object> getStats();
}
