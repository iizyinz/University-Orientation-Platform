package com.example.backend.mapper;

import com.example.backend.entity.FeeItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeeItemMapper {

    @Select("SELECT * FROM fee_item ORDER BY sort_order, id")
    List<FeeItem> findAll();

    @Select("SELECT * FROM fee_item WHERE id = #{id}")
    FeeItem findById(Long id);

    @Insert("INSERT INTO fee_item (item_name, item_code, description, sort_order, status) " +
            "VALUES (#{itemName}, #{itemCode}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeeItem item);

    @Update("UPDATE fee_item SET item_name=#{itemName}, item_code=#{itemCode}, " +
            "description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(FeeItem item);

    @Delete("DELETE FROM fee_item WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM fee_item WHERE item_code = #{code} AND id != #{id}")
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM fee_item WHERE item_code = #{code}")
    int countByCode(String code);

    @Select("SELECT * FROM fee_item WHERE item_code = #{code} LIMIT 1")
    FeeItem findByItemCode(String code);
}
