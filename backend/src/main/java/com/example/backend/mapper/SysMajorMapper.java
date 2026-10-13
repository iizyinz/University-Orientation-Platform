package com.example.backend.mapper;

import com.example.backend.entity.SysMajor;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysMajorMapper {

    @Select("SELECT m.*, c.college_name FROM sys_major m LEFT JOIN sys_college c ON m.college_id = c.id ORDER BY m.sort_order, m.id")
    List<SysMajor> findAll();

    @Select("SELECT m.*, c.college_name FROM sys_major m LEFT JOIN sys_college c ON m.college_id = c.id WHERE m.id = #{id}")
    SysMajor findById(Long id);

    @Select("SELECT m.*, c.college_name FROM sys_major m LEFT JOIN sys_college c ON m.college_id = c.id WHERE m.college_id = #{collegeId} ORDER BY m.sort_order, m.id")
    List<SysMajor> findByCollegeId(Long collegeId);

    @Insert("INSERT INTO sys_major (college_id, major_name, major_code, description, sort_order, status) VALUES (#{collegeId}, #{majorName}, #{majorCode}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMajor major);

    @Update("UPDATE sys_major SET college_id=#{collegeId}, major_name=#{majorName}, major_code=#{majorCode}, description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(SysMajor major);

    @Delete("DELETE FROM sys_major WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_major WHERE major_code = #{code} AND id != #{id}")
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_major WHERE major_code = #{code}")
    int countByCode(String code);

    @Select("SELECT * FROM sys_major WHERE major_code = #{code} LIMIT 1")
    SysMajor findByCode(String code);

    @Select("SELECT m.major_name AS name, " +
            "SUM(CASE WHEN s.report_status = 1 THEN 1 ELSE 0 END) AS reported, " +
            "COUNT(*) AS total " +
            "FROM stu_info s LEFT JOIN sys_major m ON s.major_id = m.id " +
            "GROUP BY m.major_name ORDER BY total DESC")
    List<Map<String, Object>> countReportedByMajor();
}
