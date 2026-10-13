package com.example.backend.mapper;

import com.example.backend.entity.DormRoom;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DormRoomMapper {

    @Select("SELECT dr.id, dr.building_id, dr.floor_no, dr.room_no, dr.bed_count, dr.room_type, dr.description, dr.status, dr.create_time, dr.update_time, " +
            "db.building_name, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc WHERE sdc.room_id = dr.id AND sdc.status = 1) AS used_bed_count " +
            "FROM dorm_room dr " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "ORDER BY db.sort_order, dr.building_id, dr.floor_no, dr.room_no")
    List<DormRoom> findAll();

    @Select("SELECT dr.id, dr.building_id, dr.floor_no, dr.room_no, dr.bed_count, dr.room_type, dr.description, dr.status, dr.create_time, dr.update_time, " +
            "db.building_name, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc WHERE sdc.room_id = dr.id AND sdc.status = 1) AS used_bed_count " +
            "FROM dorm_room dr " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE dr.id = #{id}")
    DormRoom findById(Long id);

    @Select("SELECT dr.id, dr.building_id, dr.floor_no, dr.room_no, dr.bed_count, dr.room_type, dr.description, dr.status, dr.create_time, dr.update_time, " +
            "db.building_name, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc WHERE sdc.room_id = dr.id AND sdc.status = 1) AS used_bed_count " +
            "FROM dorm_room dr " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE dr.building_id = #{buildingId} ORDER BY dr.floor_no, dr.room_no")
    List<DormRoom> findByBuildingId(Long buildingId);

    @Insert("INSERT INTO dorm_room (building_id, floor_no, room_no, bed_count, room_type, description, status) " +
            "VALUES (#{buildingId}, #{floorNo}, #{roomNo}, #{bedCount}, #{roomType}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DormRoom room);

    @Update("UPDATE dorm_room SET building_id=#{buildingId}, floor_no=#{floorNo}, room_no=#{roomNo}, " +
            "bed_count=#{bedCount}, room_type=#{roomType}, description=#{description}, status=#{status} WHERE id=#{id}")
    int update(DormRoom room);

    @Delete("DELETE FROM dorm_room WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM dorm_room WHERE building_id = #{buildingId} AND floor_no = #{floorNo} AND room_no = #{roomNo} AND id != #{id}")
    int countByUniqueExcludeId(@Param("buildingId") Long buildingId, @Param("floorNo") Integer floorNo, @Param("roomNo") String roomNo, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM dorm_room WHERE building_id = #{buildingId} AND floor_no = #{floorNo} AND room_no = #{roomNo}")
    int countByUnique(@Param("buildingId") Long buildingId, @Param("floorNo") Integer floorNo, @Param("roomNo") String roomNo);

    @Update("UPDATE dorm_room SET used_bed_count = #{usedBedCount} WHERE id = #{id}")
    int updateUsedBedCount(@Param("id") Long id, @Param("usedBedCount") Integer usedBedCount);
}
