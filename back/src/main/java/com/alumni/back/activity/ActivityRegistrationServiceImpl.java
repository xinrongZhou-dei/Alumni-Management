package com.alumni.back.activity;

import com.alumni.back.Entity.ActivityRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alumni.back.util.DatabaseWebSocketHandler;
import java.util.List;

@Service
public class ActivityRegistrationServiceImpl implements ActivityRegistrationService {
    @Autowired
    private ActivityRegistrationMapper registrationMapper;
    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;

    @Override
    public void saveRegistration(ActivityRegistration registration) {
        registrationMapper.insertRegistration(registration);
        databaseWebSocketHandler.sendUpdateMessage("activity registration changed");
    }

    @Override
    public ActivityRegistration getRegistration(String alumniId, String activityUuid) {
        return registrationMapper.getRegistration(alumniId, activityUuid);
    }

    @Override
    public List<ActivityRegistration> getRegistrationsByActivityUuid(String activityUuid) {
        return registrationMapper.getRegistrationsByActivityUuid(activityUuid);
    }
    
    @Override
    public int countByActivityUuid(String activityUuid) {
        return registrationMapper.countByActivityUuid(activityUuid);
    }
} 