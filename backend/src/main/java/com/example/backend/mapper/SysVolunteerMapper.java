package com.example.backend.mapper;

import com.example.backend.entity.SysVolunteer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysVolunteerMapper {

    @Select("SELECT * FROM sys_volunteer ORDER BY id")
    List<SysVolunteer> findAll();

    @Select("SELECT * FROM sys_volunteer WHERE id = #{id}")
    SysVolunteer findById(Long id);

    @Select("SELECT * FROM sys_volunteer WHERE volunteer_type = #{type} ORDER BY id")
    List<SysVolunteer> findByType(Integer type);

    @Insert("INSERT INTO sys_volunteer (user_id, volunteer_type, real_name, phone, status) " +
            "VALUES (#{userId}, #{volunteerType}, #{realName}, #{phone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysVolunteer volunteer);

    @Update("UPDATE sys_volunteer SET volunteer_type=#{volunteerType}, real_name=#{realName}, " +
            "phone=#{phone}, status=#{status} WHERE id=#{id}")
    int update(SysVolunteer volunteer);

    @Delete("DELETE FROM sys_volunteer WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_volunteer WHERE user_id = #{userId} AND id != #{id}")
    int countByUserIdExcludeId(@Param("userId") Long userId, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_volunteer WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
