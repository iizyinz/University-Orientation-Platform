package com.example.backend.mapper;

import com.example.backend.entity.CmsHandbook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CmsHandbookMapper {
    @Select("SELECT * FROM cms_handbook ORDER BY sort_order, id DESC")
    List<CmsHandbook> findAll();
    @Select("SELECT * FROM cms_handbook WHERE id = #{id}")
    CmsHandbook findById(Long id);
    @Insert("INSERT INTO cms_handbook (title, content, cover_image, sort_order, publish_status) VALUES (#{title}, #{content}, #{coverImage}, #{sortOrder}, #{publishStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CmsHandbook h);
    @Update("UPDATE cms_handbook SET title=#{title}, content=#{content}, cover_image=#{coverImage}, sort_order=#{sortOrder}, publish_status=#{publishStatus} WHERE id=#{id}")
    int update(CmsHandbook h);
    @Delete("DELETE FROM cms_handbook WHERE id = #{id}")
    int deleteById(Long id);
}
