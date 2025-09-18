package com.alumni.back.activity;

import com.alumni.back.vo.ActivityVO;
import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alumni.back.Entity.Activity;
import com.alumni.back.util.JwtUtil;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.alumni.back.util.QRCodeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping("/list")
    public Map<String, Object> getActivityList(@RequestAttribute("Authorization") String token, 
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }

        try {
            // 解析token获取用户信息
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String userRole = String.valueOf(tokenData.get("role"));
            
            // 权限检查：校友可以查看活动列表，管理员需要验证活动管理权限
            if ("alumni".equals(userRole)) {
                // 校友可以查看活动列表，无需额外权限验证
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证活动管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 1);
                if (!permissionResult.isSuccess()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", permissionResult.getCode());
                    result.put("msg", permissionResult.getMessage());
                    return result;
                }
            } else {
                // 其他角色无权限
                Map<String, Object> result = new HashMap<>();
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }
            
            List<ActivityVO> list = activityService.getActivityList(page, size);
            int total = activityService.getActivityCount();
            Map<String, Object> res = new HashMap<>();
            res.put("code", 0);
            res.put("data", list);
            res.put("total", total);
            return res;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "服务器内部错误");
            return result;
        }
    }

    @DeleteMapping("/delete/{uuid}")
    public Map<String, Object> deleteActivity(@PathVariable String uuid, @RequestAttribute("Authorization") String token) {
        // 验证活动管理权限（需要读写权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        boolean success = activityService.deleteActivity(uuid);
        Map<String, Object> res = new HashMap<>();
        res.put("code", success ? 0 : 1);
        res.put("msg", success ? "删除成功" : "删除失败");
        return res;
    }

    @GetMapping("/detail/{uuid}")
    public Map<String, Object> getActivityDetail(@PathVariable String uuid, @RequestAttribute("Authorization") String token) {
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }

        try {
            // 解析token获取用户信息
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String userRole = String.valueOf(tokenData.get("role"));
            
            // 权限检查：校友可以查看活动详情，管理员需要验证活动管理权限
            if ("alumni".equals(userRole)) {
                // 校友可以查看活动详情，无需额外权限验证
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证活动管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 1);
                if (!permissionResult.isSuccess()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", permissionResult.getCode());
                    result.put("msg", permissionResult.getMessage());
                    return result;
                }
            } else {
                // 其他角色无权限
                Map<String, Object> result = new HashMap<>();
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }
            
            Activity activity = activityService.getActivityByUuid(uuid);
            Map<String, Object> res = new HashMap<>();
            if (activity != null) {
                ActivityVO vo = new ActivityVO();
                vo.setUuid(activity.getUuid());
                vo.setName(activity.getName());
                vo.setActivityDate(activity.getActivityDate());
                vo.setSignupStart(activity.getSignupStart());
                vo.setSignupEnd(activity.getSignupEnd());
                vo.setCoverUrl(activity.getCoverUrl());
                vo.setSignupTotal(activity.getSignupTotal());
                vo.setSignupActual(activity.getSignupActual());
                // 计算报名状态，考虑人数限制
                String status = calcSignupStatus(activity.getSignupStart(), activity.getSignupEnd());
                if (activity.getSignupActual() >= activity.getSignupTotal()) {
                    status = "报名人数已满";
                }
                vo.setSignupStatus(status);
                vo.setDetail(activity.getDetail());
                vo.setVenue(activity.getVenue());
                vo.setWechat_id_required(activity.getWechatIdRequired());
                vo.setAddress_required(activity.getAddressRequired());
                vo.setCurrent_location_required(activity.getCurrentLocationRequired());
                vo.setCompanions_required(activity.getCompanionsRequired());
                vo.setPayment_proof_required(activity.getPaymentProofRequired());
                vo.setLink(activity.getLink());
                vo.setQrcodeUrl("/api/activity/qrcode/" + activity.getUuid());
                res.put("code", 0);
                res.put("data", vo);
            } else {
                res.put("code", 1);
                res.put("msg", "未找到活动");
            }
            return res;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "服务器内部错误");
            return result;
        }
    }

    @PutMapping("/edit/{uuid}")
    public Map<String, Object> editActivity(@PathVariable String uuid, @RequestBody Activity activity, @RequestAttribute("Authorization") String token) {
        // 验证活动管理权限（需要读写权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        activity.setUuid(uuid);
        boolean success = activityService.updateActivity(activity);
        Map<String, Object> res = new HashMap<>();
        res.put("code", success ? 0 : 1);
        res.put("msg", success ? "保存成功" : "保存失败");
        return res;
    }

    @PostMapping("/add")
    public Map<String, Object> addActivity(@RequestBody Activity activity, @RequestAttribute("Authorization") String token) {
        // 验证活动管理权限（需要读写权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateActivityManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        Map<String, Object> res = new HashMap<>();
        try {
            String uuid = activityService.addActivity(activity);
            res.put("code", 0);
            res.put("msg", "添加成功");
            res.put("data", Map.of("uuid", uuid));
        } catch (Exception e) {
            res.put("code", 1);
            res.put("msg", "添加失败: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("/qrcode/{uuid}")
    public void getActivityQRCode(@PathVariable String uuid, HttpServletResponse response) throws IOException {
        Activity activity = activityService.getActivityByUuid(uuid);
        if (activity == null || activity.getLink() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(activity.getLink(), 300, 300);
            response.setContentType("image/png");
            ImageIO.write(qrImage, "png", response.getOutputStream());
        } catch (WriterException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ongoing-signup-count")
    public Map<String, Object> getOngoingSignupActivityCount(@RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }
        result.put("code", 0);
        result.put("data", activityService.getOngoingSignupActivityCount());
        return result;
    }

    // 计算报名状态（与ServiceImpl一致）
    private String calcSignupStatus(java.time.LocalDate start, java.time.LocalDate end) {
        long now = System.currentTimeMillis();
        long soonMillis = 2 * 24 * 60 * 60 * 1000L; // 2天
        long startMillis = start.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        long endMillis = end.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        if (now < startMillis) return "未开始";
        if (now > endMillis) return "已结束";
        if (endMillis - now <= soonMillis) return "即将结束";
        return "进行中";
    }
} 