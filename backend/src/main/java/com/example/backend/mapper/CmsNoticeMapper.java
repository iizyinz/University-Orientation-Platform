package com.example.backend.mapper;

import com.example.backend.entity.CmsNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CmsNoticeMapper {
    @Select("SELECT * FROM cms_notice ORDER BY id DESC")
    List<CmsNotice> findAll();
    @Select("SELECT * FROM cms_notice WHERE id = #{id}")
    CmsNotice findById(Long id);
    @Insert("INSERT INTO cms_notice (title, content, notice_type, publish_status, publish_time) VALUES (#{title}, #{content}, #{noticeType}, #{publishStatus}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CmsNotice n);
    @Update("UPDATE cms_notice SET title=#{title}, content=#{content}, notice_type=#{noticeType}, publish_status=#{publishStatus}, publish_time=#{publishTime} WHERE id=#{id}")
    int update(CmsNotice n);
    @Delete("DELETE FROM cms_notice WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT id, title, notice_type, publish_time, view_count FROM cms_notice " +
            "WHERE publish_status = 1 ORDER BY notice_type DESC, publish_time DESC LIMIT #{limit}")
    List<CmsNotice> findPublished(@Param("limit") int limit);
}
