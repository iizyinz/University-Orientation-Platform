package com.example.backend.mapper;

import com.example.backend.entity.StuUniformSize;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StuUniformSizeMapper {

    @Select("SELECT * FROM stu_uniform_size WHERE student_id = #{studentId}")
    StuUniformSize findByStudentId(Long studentId);

    @Insert("INSERT INTO stu_uniform_size (student_id, clothing_size, shoe_size) " +
            "VALUES (#{studentId}, #{clothingSize}, #{shoeSize}) " +
            "ON DUPLICATE KEY UPDATE clothing_size=#{clothingSize}, shoe_size=#{shoeSize}")
    int upsert(StuUniformSize size);

    @Update("UPDATE stu_uniform_size SET clothing_size=#{clothingSize}, shoe_size=#{shoeSize} " +
            "WHERE student_id=#{studentId}")
    int updateByStudentId(StuUniformSize size);
}
