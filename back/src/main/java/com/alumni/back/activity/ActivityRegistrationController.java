package com.alumni.back.activity;

import com.alumni.back.Entity.ActivityRegistration;
import com.alumni.back.util.JwtUtil;
import com.alumni.back.util.PermissionUtil;
import com.alumni.back.wechat.WeChatMessageService;
import com.alumni.back.Entity.Activity;
import com.alumni.back.Entity.Alumni;
import com.alumni.back.register.RegisterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/activity")
public class ActivityRegistrationController {
    
    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private PermissionUtil permissionUtil;

    @Autowired
    private WeChatMessageService weChatMessageService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RegisterService registerService;

    @Value("${wechat.template.activity-signup}")
    private String activitySignupTemplateId;
    
    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody ActivityRegistration registration) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 1. 获取活动信息
            Activity activity = activityService.getActivityByUuid(registration.getActivityUuid());
            if (activity == null) {
                res.put("code", -1);
                res.put("msg", "活动不存在");
                return res;
            }

            // 2. 检查报名时间
            LocalDate today = LocalDate.now();
            if (today.isAfter(activity.getSignupEnd())) {
                res.put("code", -1);
                res.put("msg", "报名已结束");
                return res;
            }

            // 3. 检查是否重复报名
            ActivityRegistration existing = registrationService.getRegistration(
                registration.getAlumniId(), registration.getActivityUuid());
            if (existing != null) {
                res.put("code", -1);
                res.put("msg", "您已报名此活动");
                return res;
            }

            // 4. 乐观锁条件更新报名人数
            boolean success = activityService.incrementSignupActualIfAvailable(registration.getActivityUuid());
            if (!success) {
                res.put("code", -1);
                res.put("msg", "报名人数已满，无法报名");
                return res;
            }

            // 5. 保存报名记录
            registrationService.saveRegistration(registration);

            // 6. 发送微信通知
            Alumni alumni = registerService.findByAlumniId(registration.getAlumniId());
            String openid = alumni != null ? alumni.getOpenid() : null;
            String templateId = activitySignupTemplateId;
            String timeStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            java.util.Map<String, Object> data = java.util.Map.of(
                "thing2", java.util.Map.of("value", activity.getName()),
                "time3", java.util.Map.of("value", timeStr),
                "thing6", java.util.Map.of("value", activity.getVenue() != null ? activity.getVenue() : ""),
                "thing15", java.util.Map.of("value", registration.getChineseName() != null ? registration.getChineseName() : "")
            );
            boolean sendSuccess = false;
            try {
                if (openid != null && !openid.isEmpty()) {
                    sendSuccess = weChatMessageService.sendTemplateMessage(openid, templateId, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            res.put("code", 0);
            res.put("msg", "报名成功");
        } catch (Exception e) {
            res.put("code", -1);
            res.put("msg", "报名失败: " + e.getMessage());
        }
        return res;
    }
    
    @GetMapping("/registration")
    public Map<String, Object> getRegistration(@RequestParam String alumniId, @RequestParam String activityUuid) {
        ActivityRegistration registration = registrationService.getRegistration(alumniId, activityUuid);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("msg", "查询成功");
        res.put("data", registration);
        return res;
    }
    
    @GetMapping("/registrations")
    public Map<String, Object> getRegistrationsByActivityUuid(@RequestParam String activityUuid, @RequestAttribute("Authorization") String token) {
        // 验证活动管理权限（至少需要只读权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        List<ActivityRegistration> registrations = registrationService.getRegistrationsByActivityUuid(activityUuid);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("msg", "查询成功");
        res.put("data", registrations);
        return res;
    }
} 