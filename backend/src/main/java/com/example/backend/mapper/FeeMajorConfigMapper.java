package com.example.backend.mapper;

import com.example.backend.entity.FeeMajorConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeeMajorConfigMapper {

    @Select("SELECT fmc.*, sm.major_name, sc.college_name, fi.item_name FROM fee_major_config fmc " +
            "LEFT JOIN sys_major sm ON fmc.major_id = sm.id " +
            "LEFT JOIN sys_college sc ON sm.college_id = sc.id " +
            "LEFT JOIN fee_item fi ON fmc.fee_item_id = fi.id " +
            "ORDER BY sc.sort_order, sm.sort_order, fi.sort_order")
    List<FeeMajorConfig> findAll();

    @Select("SELECT fmc.*, sm.major_name, sc.college_name, fi.item_name FROM fee_major_config fmc " +
            "LEFT JOIN sys_major sm ON fmc.major_id = sm.id " +
            "LEFT JOIN sys_college sc ON sm.college_id = sc.id " +
            "LEFT JOIN fee_item fi ON fmc.fee_item_id = fi.id " +
            "WHERE fmc.major_id = #{majorId}")
    List<FeeMajorConfig> findByMajorId(Long majorId);

    @Select("SELECT fmc.*, sm.major_name, sc.college_name, fi.item_name FROM fee_major_config fmc " +
            "LEFT JOIN sys_major sm ON fmc.major_id = sm.id " +
            "LEFT JOIN sys_college sc ON sm.college_id = sc.id " +
            "LEFT JOIN fee_item fi ON fmc.fee_item_id = fi.id " +
            "WHERE fmc.id = #{id}")
    FeeMajorConfig findById(Long id);

    @Insert("INSERT INTO fee_major_config (major_id, fee_item_id, amount, pay_url, remark) " +
            "VALUES (#{majorId}, #{feeItemId}, #{amount}, #{payUrl}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeeMajorConfig config);

    @Update("UPDATE fee_major_config SET major_id=#{majorId}, fee_item_id=#{feeItemId}, " +
            "amount=#{amount}, pay_url=#{payUrl}, remark=#{remark} WHERE id=#{id}")
    int update(FeeMajorConfig config);

    @Delete("DELETE FROM fee_major_config WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM fee_major_config WHERE major_id = #{majorId} AND fee_item_id = #{feeItemId} AND id != #{id}")
    int countByMajorAndItemExcludeId(@Param("majorId") Long majorId, @Param("feeItemId") Long feeItemId, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM fee_major_config WHERE major_id = #{majorId} AND fee_item_id = #{feeItemId}")
    int countByMajorAndItem(@Param("majorId") Long majorId, @Param("feeItemId") Long feeItemId);
}
