package com.example.backend.mapper;

import com.example.backend.entity.SysCounselor;
import com.example.backend.entity.SysCounselorClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysCounselorMapper {

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "ORDER BY sc.id")
    List<SysCounselor> findAll();

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "WHERE sc.id = #{id}")
    SysCounselor findById(Long id);

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "WHERE sc.college_id = #{collegeId} ORDER BY sc.id")
    List<SysCounselor> findByCollegeId(Long collegeId);

    @Insert("INSERT INTO sys_counselor (user_id, college_id, work_no, phone, email) " +
            "VALUES (#{userId}, #{collegeId}, #{workNo}, #{phone}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysCounselor counselor);

    @Update("UPDATE sys_counselor SET college_id=#{collegeId}, work_no=#{workNo}, " +
            "phone=#{phone}, email=#{email} WHERE id=#{id}")
    int update(SysCounselor counselor);

    @Delete("DELETE FROM sys_counselor WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_counselor WHERE user_id = #{userId} AND id != #{id}")
    int countByUserIdExcludeId(@Param("userId") Long userId, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_counselor WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    // Counselor-Class relation (basic)
    @Select("SELECT cc.*, sc.class_name FROM sys_counselor_class cc " +
            "LEFT JOIN sys_class sc ON cc.class_id = sc.id " +
            "WHERE cc.counselor_id = #{counselorId}")
    List<SysCounselorClass> findClassesByCounselorId(Long counselorId);

    // Counselor-Class with full info (major, college, student counts)
    @Select("SELECT cc.id, cc.counselor_id, cc.class_id, cl.class_name, " +
            "m.id AS major_id, m.major_name, c.id AS college_id, c.college_name, " +
            "(SELECT COUNT(*) FROM stu_info si WHERE si.class_id = cc.class_id) AS student_count, " +
            "(SELECT COUNT(*) FROM stu_info si WHERE si.class_id = cc.class_id AND si.report_status = 1) AS reported_count " +
            "FROM sys_counselor_class cc " +
            "LEFT JOIN sys_class cl ON cc.class_id = cl.id " +
            "LEFT JOIN sys_major m ON cl.major_id = m.id " +
            "LEFT JOIN sys_college c ON cl.college_id = c.id " +
            "WHERE cc.counselor_id = #{counselorId} " +
            "ORDER BY c.college_name, m.major_name, cl.class_name")
    List<SysCounselorClass> findClassesDetailByCounselorId(Long counselorId);

    @Insert("<script>INSERT INTO sys_counselor_class (counselor_id, class_id) VALUES " +
            "<foreach collection='classIds' item='classId' separator=','>" +
            "(#{counselorId}, #{classId})</foreach></script>")
    int insertClasses(@Param("counselorId") Long counselorId, @Param("classIds") List<Long> classIds);

    @Delete("DELETE FROM sys_counselor_class WHERE counselor_id = #{counselorId}")
    int deleteClassesByCounselorId(Long counselorId);

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "WHERE sc.user_id = #{userId}")
    SysCounselor findByUserId(Long userId);

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "WHERE sc.work_no = #{workNo}")
    SysCounselor findByWorkNo(String workNo);

    @Select("SELECT sc.*, u.real_name, c.college_name FROM sys_counselor sc " +
            "LEFT JOIN sys_user u ON sc.user_id = u.id " +
            "LEFT JOIN sys_college c ON sc.college_id = c.id " +
            "WHERE sc.id IN (SELECT counselor_id FROM sys_counselor_class WHERE class_id = #{classId}) LIMIT 1")
    SysCounselor findByClassId(Long classId);
}
