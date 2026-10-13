package com.example.backend.mapper;

import com.example.backend.entity.StuDocument;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StuDocumentMapper {

    @Select("SELECT * FROM stu_document WHERE student_id = #{studentId} ORDER BY doc_type, id")
    List<StuDocument> findByStudentId(Long studentId);

    @Select("SELECT * FROM stu_document WHERE student_id = #{studentId} AND doc_type = #{docType} LIMIT 1")
    StuDocument findByStudentIdAndType(@Param("studentId") Long studentId, @Param("docType") Integer docType);

    @Insert("INSERT INTO stu_document (student_id, doc_type, file_name, file_key, file_url, file_size) " +
            "VALUES (#{studentId}, #{docType}, #{fileName}, #{fileUrl}, #{fileUrl}, #{fileSize})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StuDocument doc);

    @Delete("DELETE FROM stu_document WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM stu_document WHERE student_id = #{studentId} AND doc_type = #{docType}")
    int deleteByStudentIdAndType(@Param("studentId") Long studentId, @Param("docType") Integer docType);
}
