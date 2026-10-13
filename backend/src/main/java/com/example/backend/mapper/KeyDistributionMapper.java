package com.example.backend.mapper;

import com.example.backend.entity.KeyDistribution;
import com.example.backend.entity.StuInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KeyDistributionMapper {

    @Select("SELECT s.id, s.user_id, s.name, s.id_card, s.phone, s.college_id, s.major_id, s.class_id, " +
            "s.report_status, c.college_name, m.major_name, cl.class_name, " +
            "sd.room_id, dr.room_no, db.building_name, " +
            "sd.bed_no, " +
            "IFNULL((SELECT COUNT(*) FROM key_distribution WHERE student_id = s.id AND status = 1), 0) AS has_key, " +
            "IFNULL((SELECT COUNT(*) FROM uniform_distribution WHERE student_id = s.id AND status = 1), 0) AS has_uniform " +
            "FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "LEFT JOIN stu_dorm_choice sd ON s.id = sd.student_id AND sd.status = 1 " +
            "LEFT JOIN dorm_room dr ON sd.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE s.id_card = #{idCard}")
    StuInfo findStudentByIdCard(@Param("idCard") String idCard);

    @Select("SELECT * FROM key_distribution WHERE student_id = #{studentId} AND status = 1")
    KeyDistribution findByStudentId(@Param("studentId") Long studentId);

    @Insert("INSERT INTO key_distribution (student_id, room_id, volunteer_id) VALUES (#{studentId}, #{roomId}, #{volunteerId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KeyDistribution record);

    @Select("SELECT kd.*, s.name AS student_name, s.id_card, " +
            "dr.room_no, db.building_name " +
            "FROM key_distribution kd " +
            "LEFT JOIN stu_info s ON kd.student_id = s.id " +
            "LEFT JOIN dorm_room dr ON kd.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE kd.volunteer_id = #{volunteerId} " +
            "ORDER BY kd.distribute_time DESC")
    List<KeyDistribution> findByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Select("SELECT COUNT(*) FROM key_distribution WHERE volunteer_id = #{volunteerId} AND status = 1")
    int countDistributed(@Param("volunteerId") Long volunteerId);

    @Select("SELECT COUNT(*) FROM key_distribution WHERE student_id = #{studentId} AND status = 1")
    int countByStudentId(@Param("studentId") Long studentId);
}
