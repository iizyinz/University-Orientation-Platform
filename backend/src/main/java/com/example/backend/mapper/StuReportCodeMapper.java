package com.example.backend.mapper;

import com.example.backend.entity.StuReportCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StuReportCodeMapper {

    @Select("SELECT * FROM stu_report_code WHERE student_id = #{studentId}")
    StuReportCode findByStudentId(Long studentId);

    @Insert("INSERT INTO stu_report_code (student_id, code_content, expire_time) " +
            "VALUES (#{studentId}, #{codeContent}, #{expireTime}) " +
            "ON DUPLICATE KEY UPDATE code_content=#{codeContent}, expire_time=#{expireTime}")
    int upsert(StuReportCode code);

    @Update("UPDATE stu_report_code SET is_used = #{isUsed} WHERE student_id = #{studentId}")
    int updateUsed(@Param("studentId") Long studentId, @Param("isUsed") int isUsed);
}
