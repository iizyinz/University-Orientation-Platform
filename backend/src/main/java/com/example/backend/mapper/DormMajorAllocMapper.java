package com.example.backend.mapper;

import com.example.backend.entity.DormMajorAlloc;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DormMajorAllocMapper {

    @Select("SELECT dma.id, dma.major_id, dma.room_id, dma.gender, dma.alloc_bed_count, dma.create_time, dma.update_time, " +
            "sm.major_name, sc.college_name, db.building_name, dr.room_no, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc WHERE sdc.room_id = dma.room_id AND sdc.status = 1) AS used_bed_count " +
            "FROM dorm_major_alloc dma " +
            "LEFT JOIN sys_major sm ON dma.major_id = sm.id " +
            "LEFT JOIN sys_college sc ON sm.college_id = sc.id " +
            "LEFT JOIN dorm_room dr ON dma.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "ORDER BY sc.sort_order, sm.sort_order, dma.gender, db.building_name, dr.room_no")
    List<DormMajorAlloc> findAll();

    @Select("SELECT dma.id, dma.major_id, dma.room_id, dma.gender, dma.alloc_bed_count, dma.create_time, dma.update_time, " +
            "sm.major_name, sc.college_name, db.building_name, dr.room_no, " +
            "(SELECT COUNT(*) FROM stu_dorm_choice sdc WHERE sdc.room_id = dma.room_id AND sdc.status = 1) AS used_bed_count " +
            "FROM dorm_major_alloc dma " +
            "LEFT JOIN sys_major sm ON dma.major_id = sm.id " +
            "LEFT JOIN sys_college sc ON sm.college_id = sc.id " +
            "LEFT JOIN dorm_room dr ON dma.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE dma.id = #{id}")
    DormMajorAlloc findById(Long id);

    @Insert("INSERT INTO dorm_major_alloc (major_id, room_id, gender, alloc_bed_count) " +
            "VALUES (#{majorId}, #{roomId}, #{gender}, #{allocBedCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DormMajorAlloc alloc);

    @Update("UPDATE dorm_major_alloc SET major_id=#{majorId}, room_id=#{roomId}, gender=#{gender}, " +
            "alloc_bed_count=#{allocBedCount} WHERE id=#{id}")
    int update(DormMajorAlloc alloc);

    @Delete("DELETE FROM dorm_major_alloc WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM dorm_major_alloc WHERE major_id = #{majorId} AND room_id = #{roomId} AND gender = #{gender} AND id != #{id}")
    int countByMajorRoomGenderExcludeId(@Param("majorId") Long majorId, @Param("roomId") Long roomId, @Param("gender") Integer gender, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM dorm_major_alloc WHERE major_id = #{majorId} AND room_id = #{roomId} AND gender = #{gender}")
    int countByMajorRoomGender(@Param("majorId") Long majorId, @Param("roomId") Long roomId, @Param("gender") Integer gender);

    @Update("UPDATE dorm_major_alloc SET used_bed_count = #{usedBedCount} WHERE room_id = #{roomId} AND major_id = #{majorId} AND gender = #{gender}")
    int updateUsedBedCount(@Param("roomId") Long roomId, @Param("majorId") Long majorId, @Param("gender") Integer gender, @Param("usedBedCount") Integer usedBedCount);

    @Select("SELECT * FROM dorm_major_alloc WHERE room_id = #{roomId}")
    List<DormMajorAlloc> findByRoomId(Long roomId);

    @Select("SELECT * FROM dorm_major_alloc WHERE room_id = #{roomId} AND gender = #{gender}")
    List<DormMajorAlloc> findByRoomIdAndGender(@Param("roomId") Long roomId, @Param("gender") Integer gender);
}
