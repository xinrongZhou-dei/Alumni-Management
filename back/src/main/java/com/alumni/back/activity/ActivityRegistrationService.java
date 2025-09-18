package com.alumni.back.activity;

import java.util.List;

import com.alumni.back.Entity.ActivityRegistration;

public interface ActivityRegistrationService {
    void saveRegistration(ActivityRegistration registration);
    
    ActivityRegistration getRegistration(String alumniId, String activityUuid);

    List<ActivityRegistration> getRegistrationsByActivityUuid(String activityUuid);
    
    // 统计活动报名人数
    int countByActivityUuid(String activityUuid);
} 