package com.example.backend.mapper;

import com.example.backend.entity.StuInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface StuInfoMapper {

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "ORDER BY s.id")
    List<StuInfo> findAll();

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "WHERE s.id = #{id}")
    StuInfo findById(Long id);

    @Insert("INSERT INTO stu_info (user_id, student_no, exam_no, id_card, name, gender, " +
            "birthday, nation, political_status, province, city, district, health_status, " +
            "phone, email, hobby, emergency_contact_name, emergency_contact_relation, " +
            "emergency_contact_phone, college_id, major_id, class_id) " +
            "VALUES (#{userId}, #{studentNo}, #{examNo}, #{idCard}, #{name}, #{gender}, " +
            "#{birthday}, #{nation}, #{politicalStatus}, #{province}, #{city}, #{district}, " +
            "#{healthStatus}, #{phone}, #{email}, #{hobby}, #{emergencyContactName}, " +
            "#{emergencyContactRelation}, #{emergencyContactPhone}, #{collegeId}, #{majorId}, #{classId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StuInfo stuInfo);

    @Update("UPDATE stu_info SET student_no=#{studentNo}, exam_no=#{examNo}, id_card=#{idCard}, " +
            "name=#{name}, gender=#{gender}, birthday=#{birthday}, nation=#{nation}, " +
            "political_status=#{politicalStatus}, province=#{province}, city=#{city}, " +
            "district=#{district}, health_status=#{healthStatus}, phone=#{phone}, email=#{email}, " +
            "hobby=#{hobby}, emergency_contact_name=#{emergencyContactName}, " +
            "emergency_contact_relation=#{emergencyContactRelation}, " +
            "emergency_contact_phone=#{emergencyContactPhone}, college_id=#{collegeId}, " +
            "major_id=#{majorId}, class_id=#{classId} WHERE id=#{id}")
    int update(StuInfo stuInfo);

    @Delete("DELETE FROM stu_info WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM stu_info WHERE id_card = #{idCard}")
    int countByIdCard(String idCard);

    @Select("SELECT COUNT(*) FROM stu_info WHERE student_no = #{studentNo}")
    int countByStudentNo(String studentNo);

    @Select("SELECT COUNT(*) FROM stu_info")
    int countAll();

    @Select("SELECT COUNT(*) FROM stu_info WHERE report_status = 1")
    int countReported();

    @Select("SELECT COUNT(*) FROM stu_info WHERE class_id = #{classId}")
    int countByClassId(Long classId);

    @Select("SELECT COUNT(*) FROM stu_info WHERE class_id = #{classId} AND report_status = 1")
    int countReportedByClassId(Long classId);

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "WHERE s.class_id = #{classId} ORDER BY s.id")
    List<StuInfo> findByClassId(Long classId);

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "WHERE s.id_card = #{idCard}")
    StuInfo findByIdCard(String idCard);

    @Update("UPDATE stu_info SET report_status = #{status}, report_time = NOW() WHERE id = #{id}")
    int updateReportStatus(@Param("id") Long id, @Param("status") int status);

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "WHERE s.user_id = #{userId}")
    StuInfo findByUserId(Long userId);

    @Update("UPDATE stu_info SET info_completed = #{status} WHERE id = #{id}")
    int updateInfoCompleted(@Param("id") Long id, @Param("status") int status);

    @Select("SELECT c.college_name AS name, COUNT(*) AS value FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id GROUP BY c.college_name ORDER BY value DESC")
    List<Map<String, Object>> countByCollege();

    @Select("SELECT CASE WHEN gender = 1 THEN '男' ELSE '女' END AS name, COUNT(*) AS value " +
            "FROM stu_info GROUP BY gender")
    List<Map<String, Object>> countByGender();

    @Select("SELECT s.*, c.college_name, m.major_name, cl.class_name, " +
            "db.building_name, dr.room_no, sdc.bed_no " +
            "FROM stu_info s " +
            "LEFT JOIN sys_college c ON s.college_id = c.id " +
            "LEFT JOIN sys_major m ON s.major_id = m.id " +
            "LEFT JOIN sys_class cl ON s.class_id = cl.id " +
            "LEFT JOIN stu_dorm_choice sdc ON sdc.student_id = s.id AND sdc.status = 1 " +
            "LEFT JOIN dorm_room dr ON sdc.room_id = dr.id " +
            "LEFT JOIN dorm_building db ON dr.building_id = db.id " +
            "WHERE s.class_id = #{classId} ORDER BY s.id")
    List<StuInfo> findByClassIdWithDorm(Long classId);
}
