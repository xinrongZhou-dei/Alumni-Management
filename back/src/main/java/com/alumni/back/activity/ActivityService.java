package com.alumni.back.activity;

import com.alumni.back.vo.ActivityVO;
import java.util.List;
import com.alumni.back.Entity.Activity;

public interface ActivityService {
    List<ActivityVO> getActivityList(int page, int size);
    int getActivityCount();
    boolean deleteActivity(String uuid);
    Activity getActivityByUuid(String uuid);
    boolean updateActivity(Activity activity);
    String addActivity(Activity activity);
    
    // 更新活动已报名人数
    void updateSignupActual(String activityUuid);
    // 乐观锁报名，原子性递减剩余名额
    boolean trySignupActivity(String activityUuid);
    int getOngoingSignupActivityCount();
    // 乐观锁条件更新报名人数
    boolean incrementSignupActualIfAvailable(String activityUuid);
} 