package com.alumni.back.util;

import com.alumni.back.Entity.Admin;
import com.alumni.back.admin.AdminMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 权限验证工具类
 * 用于统一处理管理员权限验证逻辑
 */
@Component
public class PermissionUtil {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 验证校友信息管理权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateAlumniInfoManagementPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }

        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }

            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }

            // 4. 验证权限级别
            if (admin.getAlumniInfoManagementPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "校友信息管理权限不足");
            }

            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证活动管理权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateActivityManagementPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }

        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }

            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }

            // 4. 验证权限级别
            if (admin.getActivityManagementPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "活动管理权限不足");
            }

            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证标签管理权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateTagManagementPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }

        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }

            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }

            // 4. 验证权限级别
            if (admin.getTagManagementPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "标签管理权限不足");
            }

            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证分会审核权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateChapterReviewPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }
        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }
            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }
            // 4. 验证权限级别
            if (admin.getChapterReviewPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "分会审核权限不足");
            }
            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证数据分析权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateDataAnalysisPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }
        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }
            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }
            // 4. 验证权限级别
            if (admin.getDataAnalysisPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "数据分析权限不足");
            }
            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证权限管理权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validatePermissionManagementPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }
        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }
            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }
            // 4. 验证权限级别
            if (admin.getPermissionManagementPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "权限管理权限不足");
            }
            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 验证校园参观记录权限
     * @param token JWT token
     * @param requiredLevel 需要的权限级别 (0=无权限, 1=只读, 2=读写)
     * @return 验证结果
     */
    public PermissionResult validateCampusVisitRecordPermission(String token, int requiredLevel) {
        // 1. 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            return new PermissionResult(false, 401, "未登录或token无效");
        }
        try {
            // 2. 验证用户角色
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String role = String.valueOf(tokenData.get("role"));
            if (!"admin".equals(role)) {
                return new PermissionResult(false, 403, "无权限访问");
            }
            // 3. 获取管理员权限信息
            String adminId = String.valueOf(tokenData.get("alumni_id"));
            Admin admin = adminMapper.findById(adminId);
            if (admin == null) {
                return new PermissionResult(false, 403, "管理员信息不存在");
            }
            // 4. 验证权限级别
            if (admin.getCampusVisitRecordPermission() < requiredLevel) {
                return new PermissionResult(false, 403, "校园参观记录权限不足");
            }
            // 5. 返回成功结果，包含权限信息
            return new PermissionResult(true, 200, "权限验证通过", admin);
        } catch (Exception e) {
            return new PermissionResult(false, 401, "token解析失败");
        }
    }

    /**
     * 权限验证结果类
     */
    public static class PermissionResult {
        private boolean success;
        private int code;
        private String message;
        private Admin admin;

        public PermissionResult(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
        }

        public PermissionResult(boolean success, int code, String message, Admin admin) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.admin = admin;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Admin getAdmin() {
            return admin;
        }
    }
} 