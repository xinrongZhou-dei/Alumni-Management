package com.alumni.back.activity;

import com.alumni.back.Entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityMapper {
    // 分页查询活动列表
    List<Activity> selectActivityList(@Param("offset") int offset, @Param("limit") int limit);

    // 查询活动总数
    int countActivity();

    int deleteActivity(@Param("uuid") String uuid);

    Activity selectActivityByUuid(@Param("uuid") String uuid);

    int updateActivity(Activity activity);

    int updateActivityRequiredFields(Activity activity);

    // 查询报名表填写字段是否存在
    int countActivityRequiredFields(@Param("uuid") String uuid);
    // 插入报名表填写字段
    int insertActivityRequiredFields(Activity activity);

    int insertActivity(Activity activity);

    int deleteActivityRequiredFields(String uuid);
    int deleteActivityRegistrations(String uuid);

    // 更新活动已报名人数
    int updateSignupActual(@Param("uuid") String uuid, @Param("signupActual") int signupActual);

    // 乐观锁递减剩余名额
    int decrementRemainIfAvailable(@Param("uuid") String uuid);

    // 乐观锁条件更新报名人数
    int incrementSignupActualIfAvailable(@Param("uuid") String uuid);

    // 动态字段批量查询
    java.util.List<java.util.Map<String, Object>> batchSelectFields(
        @org.apache.ibatis.annotations.Param("uuids") java.util.List<String> uuids,
        @org.apache.ibatis.annotations.Param("fields") java.util.List<String> fields
    );

    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM activity WHERE signup_start <= NOW() AND signup_end >= NOW()")
    int countOngoingSignupActivities();
} 