package com.example.backend.mapper;

import com.example.backend.entity.FaqCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FaqCategoryMapper {
    @Select("SELECT fc.*, " +
            "(SELECT COUNT(*) FROM faq_knowledge WHERE category_id = fc.id) AS question_count " +
            "FROM faq_category fc ORDER BY fc.sort_order, fc.id")
    List<FaqCategory> findAll();

    @Select("SELECT * FROM faq_category WHERE id = #{id}")
    FaqCategory findById(Long id);

    @Select("SELECT * FROM faq_category WHERE category_name = #{categoryName}")
    FaqCategory findByCategoryName(String categoryName);

    @Insert("INSERT INTO faq_category (category_name, sort_order, status) VALUES (#{categoryName}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FaqCategory c);

    @Update("UPDATE faq_category SET category_name=#{categoryName}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(FaqCategory c);

    @Delete("DELETE FROM faq_category WHERE id = #{id}")
    int deleteById(Long id);
}
