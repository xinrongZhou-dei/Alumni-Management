package com.alumni.back.visitrecord;

import com.alumni.back.Entity.VisitRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface VisitRecordMapper {
    // 创建预约记录
    @Insert("INSERT INTO visit_record (visit_UUID, alumni_id, chinese_name, current_location, contact_number, " +
            "visit_school, visit_day, visit_time, status, remark, create_time, update_time) " +
            "VALUES (#{visitUUID}, #{alumniId}, #{chineseName}, #{currentLocation}, #{contactNumber}, " +
            "#{visitSchool}, #{visitDay}, #{visitTime}, #{status}, #{remark}, NOW(), NOW())")
    void createVisitRecord(VisitRecord visitRecord);

    // 获取预约记录列表
    @Select("<script>" +
            "SELECT * FROM visit_record " +
            "WHERE 1=1 " +
            "<if test='visitSchool != null and visitSchool != \"\"'> AND visit_school = #{visitSchool} </if> " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "<if test='visitDay != null and visitDay != \"\"'> AND visit_day = #{visitDay} </if> " +
            "ORDER BY " +
            "CASE status " +
            "  WHEN 'PENDING' THEN 1 " +
            "  WHEN 'APPROVED' THEN 2 " +
            "  WHEN 'REJECTED' THEN 3 " +
            "END, " +
            "visit_day DESC, " +
            "create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<VisitRecord> getVisitRecordList(@Param("visitSchool") String visitSchool,
                                        @Param("status") String status,
                                        @Param("visitDay") String visitDay,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    // 根据管理员权限获取预约记录列表
    @Select("<script>" +
            "SELECT * FROM visit_record " +
            "WHERE 1=1 " +
            "<if test='visitSchool != null and visitSchool != \"\"'> AND visit_school = #{visitSchool} </if> " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "<if test='visitDay != null and visitDay != \"\"'> AND visit_day = #{visitDay} </if> " +
            "<if test='schools != null and schools.size() > 0'> AND visit_school IN " +
            "<foreach collection='schools' item='school' open='(' separator=',' close=')'>" +
            "#{school}" +
            "</foreach>" +
            "</if> " +
            "ORDER BY " +
            "CASE status " +
            "  WHEN 'PENDING' THEN 1 " +
            "  WHEN 'APPROVED' THEN 2 " +
            "  WHEN 'REJECTED' THEN 3 " +
            "END, " +
            "visit_day DESC, " +
            "create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<VisitRecord> getVisitRecordListByAdminPermission(@Param("visitSchool") String visitSchool,
                                                         @Param("status") String status,
                                                         @Param("visitDay") String visitDay,
                                                         @Param("offset") int offset,
                                                         @Param("pageSize") int pageSize,
                                                         @Param("schools") List<String> schools);

    // 获取总记录数
    @Select("<script>" +
            "SELECT COUNT(*) FROM visit_record " +
            "WHERE 1=1 " +
            "<if test='visitSchool != null and visitSchool != \"\"'> AND visit_school = #{visitSchool} </if> " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "<if test='visitDay != null and visitDay != \"\"'> AND visit_day = #{visitDay} </if>" +
            "</script>")
    int getVisitRecordCount(@Param("visitSchool") String visitSchool,
                           @Param("status") String status,
                           @Param("visitDay") String visitDay);

    // 根据管理员权限获取总记录数
    @Select("<script>" +
            "SELECT COUNT(*) FROM visit_record " +
            "WHERE 1=1 " +
            "<if test='visitSchool != null and visitSchool != \"\"'> AND visit_school = #{visitSchool} </if> " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "<if test='visitDay != null and visitDay != \"\"'> AND visit_day = #{visitDay} </if> " +
            "<if test='schools != null and schools.size() > 0'> AND visit_school IN " +
            "<foreach collection='schools' item='school' open='(' separator=',' close=')'>" +
            "#{school}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    int getVisitRecordCountByAdminPermission(@Param("visitSchool") String visitSchool,
                                            @Param("status") String status,
                                            @Param("visitDay") String visitDay,
                                            @Param("schools") List<String> schools);

    // 获取预约记录详情
    @Select("SELECT * FROM visit_record WHERE visit_uuid = #{visitUUID}")
    VisitRecord getVisitRecordDetail(@Param("visitUUID") String visitUUID);

    // 更新预约状态
    @Update("UPDATE visit_record SET status = #{status}, " +
            "remark = CASE " +
            "WHEN #{remark} IS NOT NULL AND #{remark} != '' THEN CONCAT(remark, '\n审核备注：', #{remark}) " +
            "ELSE remark END, " +
            "update_time = NOW() WHERE visit_uuid = #{visitUUID}")
    void updateVisitStatus(@Param("visitUUID") String visitUUID,
                          @Param("status") String status,
                          @Param("remark") String remark);

    // 批量更新预约状态
    @Update("<script>" +
            "UPDATE visit_record SET status = #{status}, remark = #{remark}, update_time = NOW() " +
            "WHERE visit_uuid IN " +
            "<foreach collection='visitUUIDs' item='visitUUID' open='(' separator=',' close=')'>" +
            "#{visitUUID}" +
            "</foreach>" +
            "</script>")
    void batchUpdateVisitStatus(@Param("visitUUIDs") List<String> visitUUIDs,
                               @Param("status") String status,
                               @Param("remark") String remark);

    // 更新预约记录
    @Update("UPDATE visit_record SET " +
            "visit_school = #{visitSchool}, " +
            "visit_day = #{visitDay}, " +
            "visit_time = #{visitTime}, " +
            "remark = #{remark}, " +
            "update_time = NOW() " +
            "WHERE visit_uuid = #{visitUUID}")
    void updateVisitRecord(VisitRecord visitRecord);
    
    // 根据校友ID删除校园参观预约记录
    @Delete("DELETE FROM visit_record WHERE alumni_id = #{alumniId}")
    int deleteByAlumniId(@Param("alumniId") String alumniId);

    @Select("SELECT COUNT(*) FROM visit_record WHERE status = 'PENDING'")
    int countPendingVisitRecords();
}