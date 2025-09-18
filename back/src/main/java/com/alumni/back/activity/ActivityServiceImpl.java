package com.alumni.back.activity;

import com.alumni.back.Entity.Activity;
import com.alumni.back.vo.ActivityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alumni.back.util.DatabaseWebSocketHandler;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;

    @Override
    public List<ActivityVO> getActivityList(int page, int size) {
        int offset = (page - 1) * size;
        List<Activity> activities = activityMapper.selectActivityList(offset, size);
        List<ActivityVO> voList = new ArrayList<>();
        for (Activity act : activities) {
            ActivityVO vo = new ActivityVO();
            vo.setUuid(act.getUuid());
            vo.setName(act.getName());
            vo.setActivityDate(act.getActivityDate());
            vo.setSignupStart(act.getSignupStart());
            vo.setSignupEnd(act.getSignupEnd());
            vo.setCoverUrl(act.getCoverUrl());
            vo.setSignupTotal(act.getSignupTotal());
            // 实时统计报名人数
            int signupActual = activityRegistrationMapper.countByActivityUuid(act.getUuid());
            vo.setSignupActual(signupActual);
            // 计算报名状态，考虑人数限制
            String status = calcSignupStatus(act.getSignupStart(), act.getSignupEnd());
            if (signupActual >= act.getSignupTotal()) {
                status = "报名人数已满";
            }
            vo.setSignupStatus(status);
            vo.setVenue(act.getVenue());
            vo.setDetail(act.getDetail());
            vo.setLink(act.getLink());
            vo.setQrcodeUrl(act.getQrcodeUrl());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public int getActivityCount() {
        return activityMapper.countActivity();
    }

    @Override
    public int getOngoingSignupActivityCount() {
        return activityMapper.countOngoingSignupActivities();
    }

    // 计算报名状态
    private String calcSignupStatus(LocalDate start, LocalDate end) {
        long now = System.currentTimeMillis();
        long soonMillis = 2 * 24 * 60 * 60 * 1000L; // 2天
        long startMillis = start.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        long endMillis = end.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        if (now < startMillis) return "未开始";
        if (now > endMillis) return "已结束";
        if (endMillis - now <= soonMillis) return "即将结束";
        return "进行中";
    }

    @Override
    public boolean deleteActivity(String uuid) {
        // 先删必填字段表
        activityMapper.deleteActivityRequiredFields(uuid);
        // 再删报名表
        activityMapper.deleteActivityRegistrations(uuid);
        // 最后删活动表
        boolean result = activityMapper.deleteActivity(uuid) > 0;
        if (result) {
            databaseWebSocketHandler.sendUpdateMessage("activity changed");
        }
        return result;
    }

    @Override
    public Activity getActivityByUuid(String uuid) {
        Activity activity = activityMapper.selectActivityByUuid(uuid);
        if (activity != null) {
            int signupActual = activityRegistrationMapper.countByActivityUuid(uuid);
            activity.setSignupActual(signupActual);
        }
        return activity;
    }

    @Override
    public boolean updateActivity(Activity activity) {
        boolean mainUpdated = activityMapper.updateActivity(activity) > 0;
        int count = activityMapper.countActivityRequiredFields(activity.getUuid());
        boolean fieldsUpdated;
        if (count > 0) {
            fieldsUpdated = activityMapper.updateActivityRequiredFields(activity) > 0;
        } else {
            fieldsUpdated = activityMapper.insertActivityRequiredFields(activity) > 0;
        }
        boolean result = mainUpdated || fieldsUpdated;
        if (result) {
            databaseWebSocketHandler.sendUpdateMessage("activity changed");
        }
        return result;
    }

    @Override
    public String addActivity(Activity activity) {
        // 生成UUID
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        activity.setUuid(uuid);
        activity.setLink("https://7122a86e.r15.cpolar.top/alumni/activity-detail/" + uuid);
        // 若signupActual为null，强制设为0
        if (activity.getSignupActual() == null) {
            activity.setSignupActual(0);
        }
        activityMapper.insertActivity(activity);
        // 插入必填项
        activityMapper.insertActivityRequiredFields(activity);
        databaseWebSocketHandler.sendUpdateMessage("activity changed");
        return uuid;
    }
    
    @Override
    public void updateSignupActual(String activityUuid) {
        // 实时统计报名人数并更新活动表
        int signupActual = activityRegistrationMapper.countByActivityUuid(activityUuid);
        activityMapper.updateSignupActual(activityUuid, signupActual);
        // 发送WebSocket通知
        databaseWebSocketHandler.sendUpdateMessage("activity changed");
    }

    @Override
    public boolean trySignupActivity(String activityUuid) {
        // 乐观锁：只在剩余名额大于0时递减
        int affected = activityMapper.decrementRemainIfAvailable(activityUuid);
        return affected > 0;
    }

    @Override
    public boolean incrementSignupActualIfAvailable(String activityUuid) {
        int affected = activityMapper.incrementSignupActualIfAvailable(activityUuid);
        if (affected > 0) {
            databaseWebSocketHandler.sendUpdateMessage("activity changed");
            return true;
        }
        return false;
    }
} 