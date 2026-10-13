package com.example.backend.mapper;

import com.example.backend.entity.SysCollege;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysCollegeMapper {

    @Select("SELECT * FROM sys_college ORDER BY sort_order, id")
    List<SysCollege> findAll();

    @Select("SELECT * FROM sys_college WHERE id = #{id}")
    SysCollege findById(Long id);

    @Insert("INSERT INTO sys_college (college_name, college_code, description, sort_order, status) VALUES (#{collegeName}, #{collegeCode}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysCollege college);

    @Update("UPDATE sys_college SET college_name=#{collegeName}, college_code=#{collegeCode}, description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(SysCollege college);

    @Delete("DELETE FROM sys_college WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_college WHERE college_code = #{code} AND id != #{id}")
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_college WHERE college_code = #{code}")
    int countByCode(String code);

    @Select("SELECT * FROM sys_college WHERE college_code = #{code} LIMIT 1")
    SysCollege findByCode(String code);
}
