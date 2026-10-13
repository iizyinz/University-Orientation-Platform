package com.example.backend.mapper;

import com.example.backend.entity.SysClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysClassMapper {

    @Select("SELECT sc.*, sm.major_name, scol.college_name FROM sys_class sc LEFT JOIN sys_major sm ON sc.major_id = sm.id LEFT JOIN sys_college scol ON sc.college_id = scol.id ORDER BY sc.sort_order, sc.id")
    List<SysClass> findAll();

    @Select("SELECT sc.*, sm.major_name, scol.college_name FROM sys_class sc LEFT JOIN sys_major sm ON sc.major_id = sm.id LEFT JOIN sys_college scol ON sc.college_id = scol.id WHERE sc.id = #{id}")
    SysClass findById(Long id);

    @Select("SELECT sc.*, sm.major_name, scol.college_name FROM sys_class sc LEFT JOIN sys_major sm ON sc.major_id = sm.id LEFT JOIN sys_college scol ON sc.college_id = scol.id WHERE sc.major_id = #{majorId} ORDER BY sc.sort_order, sc.id")
    List<SysClass> findByMajorId(Long majorId);

    @Insert("INSERT INTO sys_class (major_id, college_id, class_name, class_code, description, sort_order, status) VALUES (#{majorId}, #{collegeId}, #{className}, #{classCode}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysClass sysClass);

    @Update("UPDATE sys_class SET major_id=#{majorId}, college_id=#{collegeId}, class_name=#{className}, class_code=#{classCode}, description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(SysClass sysClass);

    @Delete("DELETE FROM sys_class WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_class WHERE class_code = #{code} AND id != #{id}")
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_class WHERE class_code = #{code}")
    int countByCode(String code);

    @Select("SELECT * FROM sys_class WHERE class_code = #{code} LIMIT 1")
    SysClass findByCode(String code);
}
