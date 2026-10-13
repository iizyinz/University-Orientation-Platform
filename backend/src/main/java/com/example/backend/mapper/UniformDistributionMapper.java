package com.example.backend.mapper;

import com.example.backend.entity.UniformDistribution;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UniformDistributionMapper {

    @Select("SELECT s.id, s.name, s.id_card, s.phone, c.college_name, m.major_name, " +
            "us.clothing_size, us.shoe_size, " +
            "(SELECT COUNT(*) FROM uniform_distribution WHERE student_id = s.id AND status = 1) AS has_uniform " +
            "FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN stu_uniform_size us ON s.id = us.student_id " +
            "WHERE s.id_card = #{idCard}")
    com.example.backend.entity.StuInfo findStudentWithSize(@Param("idCard") String idCard);

    @Select("SELECT * FROM uniform_distribution WHERE student_id = #{studentId} AND status = 1")
    UniformDistribution findByStudentId(@Param("studentId") Long studentId);

    @Insert("INSERT INTO uniform_distribution (student_id, volunteer_id, clothing_size, shoe_size) " +
            "VALUES (#{studentId}, #{volunteerId}, #{clothingSize}, #{shoeSize})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UniformDistribution record);

    @Select("SELECT ud.*, s.name AS student_name, s.id_card " +
            "FROM uniform_distribution ud " +
            "LEFT JOIN stu_info s ON ud.student_id = s.id " +
            "WHERE ud.volunteer_id = #{volunteerId} " +
            "ORDER BY ud.distribute_time DESC")
    List<UniformDistribution> findByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Select("SELECT COUNT(*) FROM uniform_distribution WHERE volunteer_id = #{volunteerId} AND status = 1")
    int countDistributed(@Param("volunteerId") Long volunteerId);

    @Select("SELECT COUNT(*) FROM uniform_distribution WHERE student_id = #{studentId} AND status = 1")
    int countByStudentId(@Param("studentId") Long studentId);
}
