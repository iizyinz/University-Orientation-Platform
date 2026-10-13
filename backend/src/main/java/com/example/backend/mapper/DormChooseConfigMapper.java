package com.example.backend.mapper;

import com.example.backend.entity.DormChooseConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DormChooseConfigMapper {

    @Select("SELECT * FROM dorm_choose_config ORDER BY id DESC")
    List<DormChooseConfig> findAll();

    @Select("SELECT * FROM dorm_choose_config WHERE id = #{id}")
    DormChooseConfig findById(Long id);

    @Insert("INSERT INTO dorm_choose_config (config_name, start_time, end_time, status, description) " +
            "VALUES (#{configName}, #{startTime}, #{endTime}, #{status}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DormChooseConfig config);

    @Update("UPDATE dorm_choose_config SET config_name=#{configName}, start_time=#{startTime}, " +
            "end_time=#{endTime}, status=#{status}, description=#{description} WHERE id=#{id}")
    int update(DormChooseConfig config);

    @Delete("DELETE FROM dorm_choose_config WHERE id = #{id}")
    int deleteById(Long id);
}
