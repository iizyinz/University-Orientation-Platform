package com.example.backend.mapper;

import com.example.backend.entity.DormBuilding;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DormBuildingMapper {

    @Select("SELECT db.*, " +
            "(SELECT COUNT(*) FROM dorm_room WHERE building_id = db.id) AS roomCount, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc " +
            "  JOIN dorm_room dr ON sdc.room_id = dr.id " +
            "  WHERE dr.building_id = db.id AND sdc.status = 1) AS usedBedCount, " +
            "(SELECT COALESCE(SUM(bed_count), 0) FROM dorm_room WHERE building_id = db.id) AS totalBedCount " +
            "FROM dorm_building db ORDER BY db.sort_order, db.id")
    List<DormBuilding> findAll();

    @Select("SELECT * FROM dorm_building WHERE id = #{id}")
    DormBuilding findById(Long id);

    @Insert("INSERT INTO dorm_building (building_name, building_code, floor_count, description, sort_order, status) " +
            "VALUES (#{buildingName}, #{buildingCode}, #{floorCount}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DormBuilding building);

    @Update("UPDATE dorm_building SET building_name=#{buildingName}, building_code=#{buildingCode}, " +
            "floor_count=#{floorCount}, description=#{description}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(DormBuilding building);

    @Delete("DELETE FROM dorm_building WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM dorm_building WHERE building_code = #{code} AND id != #{id}")
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM dorm_building WHERE building_code = #{code}")
    int countByCode(String code);

    @Select("SELECT db.building_name AS name, " +
            "(SELECT COALESCE(SUM(bed_count), 0) FROM dorm_room WHERE building_id = db.id) AS total, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc " +
            "  JOIN dorm_room dr ON sdc.room_id = dr.id " +
            "  WHERE dr.building_id = db.id AND sdc.status = 1) AS used " +
            "FROM dorm_building db WHERE db.status = 1 ORDER BY db.sort_order")
    List<Map<String, Object>> findOccupancyStats();
}
