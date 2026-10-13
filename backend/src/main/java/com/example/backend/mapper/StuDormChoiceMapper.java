package com.example.backend.mapper;

import com.example.backend.entity.StuDormChoice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StuDormChoiceMapper {

    @Select("SELECT sdc.*, db.building_name, dr.floor_no, dr.room_no, dr.room_type " +
            "FROM stu_dorm_choice sdc " +
            "LEFT JOIN dorm_room dr ON sdc.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE sdc.student_id = #{studentId} AND sdc.status = 1")
    StuDormChoice findByStudentId(Long studentId);

    @Insert("INSERT INTO stu_dorm_choice (student_id, room_id, bed_no) " +
            "VALUES (#{studentId}, #{roomId}, #{bedNo}) " +
            "ON DUPLICATE KEY UPDATE room_id=#{roomId}, bed_no=#{bedNo}, status=1, choose_time=NOW()")
    int upsert(StuDormChoice choice);

    @Update("UPDATE stu_dorm_choice SET status = 0 WHERE student_id = #{studentId}")
    int cancelByStudentId(Long studentId);

    @Select("SELECT COUNT(*) FROM stu_dorm_choice WHERE room_id = #{roomId} AND bed_no = #{bedNo} AND status = 1")
    int countBedUsed(@Param("roomId") Long roomId, @Param("bedNo") Integer bedNo);

    @Select("SELECT bed_no FROM stu_dorm_choice WHERE room_id = #{roomId} AND status = 1")
    List<Integer> findUsedBedsByRoomId(Long roomId);

    @Select("SELECT COUNT(*) FROM stu_dorm_choice WHERE room_id = #{roomId} AND status = 1")
    int countByRoomId(Long roomId);
}
