package com.example.backend.mapper;

import com.example.backend.entity.ReportFlowConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportFlowConfigMapper {

    @Select("SELECT * FROM report_flow_config ORDER BY flow_step, sort_order, id")
    List<ReportFlowConfig> findAll();

    @Select("SELECT * FROM report_flow_config WHERE id = #{id}")
    ReportFlowConfig findById(Long id);

    @Insert("INSERT INTO report_flow_config (flow_name, flow_step, flow_desc, is_required, sort_order, status) " +
            "VALUES (#{flowName}, #{flowStep}, #{flowDesc}, #{isRequired}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReportFlowConfig config);

    @Update("UPDATE report_flow_config SET flow_name=#{flowName}, flow_step=#{flowStep}, " +
            "flow_desc=#{flowDesc}, is_required=#{isRequired}, sort_order=#{sortOrder}, " +
            "status=#{status} WHERE id=#{id}")
    int update(ReportFlowConfig config);

    @Delete("DELETE FROM report_flow_config WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COALESCE(MAX(flow_step), 0) + 1 FROM report_flow_config")
    int nextStep();
}
