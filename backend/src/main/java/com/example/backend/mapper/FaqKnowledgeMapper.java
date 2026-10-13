package com.example.backend.mapper;

import com.example.backend.entity.FaqKnowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FaqKnowledgeMapper {
    @Select("SELECT fk.*, fc.category_name FROM faq_knowledge fk " +
            "LEFT JOIN faq_category fc ON fk.category_id = fc.id " +
            "ORDER BY fk.category_id, fk.sort_order, fk.id")
    List<FaqKnowledge> findAll();

    @Select("SELECT fk.*, fc.category_name FROM faq_knowledge fk " +
            "LEFT JOIN faq_category fc ON fk.category_id = fc.id " +
            "WHERE fk.id = #{id}")
    FaqKnowledge findById(Long id);

    @Select("SELECT fk.*, fc.category_name FROM faq_knowledge fk " +
            "LEFT JOIN faq_category fc ON fk.category_id = fc.id " +
            "WHERE fk.category_id = #{categoryId} ORDER BY fk.sort_order, fk.id")
    List<FaqKnowledge> findByCategoryId(Long categoryId);

    @Insert("INSERT INTO faq_knowledge (category_id, question, answer, keywords, sort_order, status) " +
            "VALUES (#{categoryId}, #{question}, #{answer}, #{keywords}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FaqKnowledge k);

    @Update("UPDATE faq_knowledge SET category_id=#{categoryId}, question=#{question}, answer=#{answer}, " +
            "keywords=#{keywords}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(FaqKnowledge k);

    @Delete("DELETE FROM faq_knowledge WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE faq_knowledge SET hit_count = hit_count + 1 WHERE id = #{id}")
    int incrementHitCount(Long id);
}
