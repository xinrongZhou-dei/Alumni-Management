package com.alumni.back.admin;

import com.alumni.back.Entity.Admin;
import com.alumni.back.util.JwtUtil;
import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping("/permissions")
    public Map<String, Object> getAdminPermissions(@RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }

        try {
            // 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }

            // 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                result.put("code", 403);
                result.put("msg", "管理员信息不存在");
                return result;
            }

            // 返回权限信息
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("alumni_info_management_permission", admin.getAlumniInfoManagementPermission());
            permissions.put("alumni_info_review_permission", admin.getAlumniInfoReviewPermission());
            permissions.put("chapter_review_permission", admin.getChapterReviewPermission());
            permissions.put("activity_management_permission", admin.getActivityManagementPermission());
            permissions.put("tag_management_permission", admin.getTagManagementPermission());
            permissions.put("email_template_permission", admin.getEmailTemplatePermission());
            permissions.put("data_analysis_permission", admin.getDataAnalysisPermission());
            permissions.put("permission_management_permission", admin.getPermissionManagementPermission());
            permissions.put("campus_visit_record_permission", admin.getCampusVisitRecordPermission());
            permissions.put("accessible_schools", admin.getAccessibleSchools());

            result.put("code", 0);
            result.put("msg", "获取权限信息成功");
            result.put("data", permissions);
        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "token解析失败");
        }
        
        return result;
    }
} 