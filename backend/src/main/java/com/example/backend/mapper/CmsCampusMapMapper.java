package com.example.backend.mapper;

import com.example.backend.entity.CmsCampusMap;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CmsCampusMapMapper {
    @Select("SELECT * FROM cms_campus_map ORDER BY sort_order, id")
    List<CmsCampusMap> findAll();
    @Select("SELECT * FROM cms_campus_map WHERE id = #{id}")
    CmsCampusMap findById(Long id);
    @Insert("INSERT INTO cms_campus_map (map_name, map_image, description, sort_order, status) VALUES (#{mapName}, #{mapImage}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CmsCampusMap m);
    @Update("UPDATE cms_campus_map SET map_name=#{mapName}, map_image=#{mapImage}, description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(CmsCampusMap m);
    @Delete("DELETE FROM cms_campus_map WHERE id = #{id}")
    int deleteById(Long id);
}
