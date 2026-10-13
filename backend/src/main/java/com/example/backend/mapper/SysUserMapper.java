package com.example.backend.mapper;

import com.example.backend.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("SELECT id, username, password, real_name, user_type, phone, email, avatar, status, last_login_time, last_login_ip, create_time, update_time FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    @Select("SELECT id, username, password, real_name, user_type, phone, email, avatar, status, last_login_time, last_login_ip, create_time, update_time FROM sys_user WHERE id = #{id}")
    SysUser findById(Long id);

    @Insert("INSERT INTO sys_user (username, password, real_name, user_type, phone, email, avatar, status) " +
            "VALUES (#{username}, #{password}, #{realName}, #{userType}, #{phone}, #{email}, #{avatar}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(SysUser user);

    @Update("UPDATE sys_user SET real_name=#{realName}, phone=#{phone}, email=#{email}, avatar=#{avatar} WHERE id=#{id}")
    int updateProfile(SysUser user);

    @Update("UPDATE sys_user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("password") String password, @Param("id") Long id);

    @Update("UPDATE sys_user SET last_login_time=NOW(), last_login_ip=#{ip} WHERE id=#{id}")
    int updateLoginInfo(@Param("id") Long id, @Param("ip") String ip);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT id, username, real_name, user_type, phone, email, avatar, status, last_login_time, create_time FROM sys_user ORDER BY id")
    List<SysUser> findAll();
}
