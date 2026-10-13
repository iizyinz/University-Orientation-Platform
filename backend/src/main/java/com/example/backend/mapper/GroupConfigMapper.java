package com.example.backend.mapper;

import com.example.backend.entity.GroupConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupConfigMapper {

    @Select("SELECT g.*, " +
            "CASE g.group_type " +
            "  WHEN 2 THEN (SELECT college_name FROM sys_college WHERE id = g.target_id) " +
            "  WHEN 3 THEN (SELECT class_name FROM sys_class WHERE id = g.target_id) " +
            "  ELSE g.description " +
            "END AS target_name " +
            "FROM group_config g ORDER BY g.group_type, g.sort_order, g.id")
    List<GroupConfig> findAll();

    @Select("SELECT g.*, " +
            "CASE g.group_type " +
            "  WHEN 2 THEN (SELECT college_name FROM sys_college WHERE id = g.target_id) " +
            "  WHEN 3 THEN (SELECT class_name FROM sys_class WHERE id = g.target_id) " +
            "  ELSE g.description " +
            "END AS target_name " +
            "FROM group_config g WHERE g.id = #{id}")
    GroupConfig findById(Long id);

    @Insert("INSERT INTO group_config (group_type, group_name, group_no, group_qr_image, target_id, province, description, sort_order, status) " +
            "VALUES (#{groupType}, #{groupName}, #{groupNo}, #{groupQrImage}, #{targetId}, #{province}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GroupConfig g);

    @Update("UPDATE group_config SET group_type=#{groupType}, group_name=#{groupName}, group_no=#{groupNo}, " +
            "group_qr_image=#{groupQrImage}, target_id=#{targetId}, province=#{province}, description=#{description}, " +
            "sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(GroupConfig g);

    @Delete("DELETE FROM group_config WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT g.*, " +
            "CASE g.group_type " +
            "  WHEN 2 THEN (SELECT college_name FROM sys_college WHERE id = g.target_id) " +
            "  WHEN 3 THEN (SELECT class_name FROM sys_class WHERE id = g.target_id) " +
            "  ELSE g.description " +
            "END AS target_name " +
            "FROM group_config g " +
            "WHERE g.group_type = 3 AND g.target_id IN " +
            "(SELECT class_id FROM sys_counselor_class WHERE counselor_id = #{counselorId}) " +
            "ORDER BY g.sort_order, g.id")
    List<GroupConfig> findByCounselorId(Long counselorId);

    @Select("SELECT COUNT(*) FROM group_config WHERE group_type = #{groupType} AND target_id = #{targetId}")
    int countByTargetTypeAndId(@Param("groupType") int groupType, @Param("targetId") Long targetId);
}
