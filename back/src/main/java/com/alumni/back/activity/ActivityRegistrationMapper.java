package com.alumni.back.activity;

import com.alumni.back.Entity.ActivityRegistration;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface ActivityRegistrationMapper {
    int insertRegistration(ActivityRegistration registration);
    
    ActivityRegistration getRegistration(@Param("alumniId") String alumniId, @Param("activityUuid") String activityUuid);

    int countByActivityUuid(@Param("activityUuid") String activityUuid);

    @Select("SELECT COUNT(*) FROM activity_registration WHERE YEAR(created_at) = #{year}")
    int countRegistrationsByYear(@Param("year") int year);

    // 动态字段批量查询
    java.util.List<java.util.Map<String, Object>> batchSelectFields(
        @org.apache.ibatis.annotations.Param("alumniIds") java.util.List<String> alumniIds,
        @org.apache.ibatis.annotations.Param("fields") java.util.List<String> fields
    );

    List<ActivityRegistration> getRegistrationsByActivityUuid(@Param("activityUuid") String activityUuid);
    
    // 根据校友ID删除活动报名记录
    @Delete("DELETE FROM activity_registration WHERE alumni_id = #{alumniId}")
    int deleteByAlumniId(@Param("alumniId") String alumniId);
} 