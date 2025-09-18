package com.alumni.back.alumnisearch;

import com.alumni.back.admin.AdminMapper;
import com.alumni.back.dto.AlumniSearchRequest;
import com.alumni.back.register.RegisterMapper;
import com.alumni.back.vo.AlumniSearchResultVO;
import com.alumni.back.util.JwtUtil;
import com.alumni.back.util.PermissionUtil;
import com.alumni.back.Entity.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AlumniSearchController {
    private static final Logger logger = LoggerFactory.getLogger(AlumniSearchController.class);

    @Autowired
    private AlumniSearchService alumniSearchService;

    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private PermissionUtil permissionUtil;

    @Autowired
    private AdminMapper adminMapper;

    @PostMapping("/api/alumni/search")
    public Map<String, Object> searchAlumni(@RequestBody AlumniSearchRequest req, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证token有效性
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 401);
                result.put("msg", "未登录或token无效");
                return result;
            }

            // 解析token获取用户信息
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String userRole = String.valueOf(tokenData.get("role"));
            
            // 设置用户角色，用于Service层判断是否需要脱敏
            req.setUserRole(userRole);
            
            // 根据用户角色设置权限参数
            if ("alumni".equals(userRole)) {
                // 校友可以查看所有校友，不设置权限限制
                req.setAlumniInfoManagementPermission(null);
                req.setPermissionSchool(null);
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证校友信息管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 1);
                if (!permissionResult.isSuccess()) {
                    result.put("code", permissionResult.getCode());
                    result.put("msg", permissionResult.getMessage());
                    return result;
                }
                
                // 设置管理员的权限参数
                Admin admin = permissionResult.getAdmin();
                req.setAlumniInfoManagementPermission(admin.getAlumniInfoManagementPermission());
                req.setPermissionSchool(admin.getAccessibleSchools());
            } else {
                // 其他角色无权限
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }

            // 设置默认值
            if (req.getPageNum() == null) req.setPageNum(1);
            if (req.getPageSize() == null) req.setPageSize(10);
            
            // 计算分页偏移量
            req.setOffset((req.getPageNum() - 1) * req.getPageSize());
            
            List<AlumniSearchResultVO> list = alumniSearchService.searchAlumni(req);
            int total = alumniSearchService.countAlumni(req);
            
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("搜索校友失败", e);
            result.put("code", 1);
            result.put("msg", "搜索失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/api/alumni/search-by-chapter")
    public Map<String, Object> searchAlumniByChapter(@RequestBody AlumniSearchRequest req, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证token有效性
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 401);
                result.put("msg", "未登录或token无效");
                return result;
            }

            // 解析token获取用户信息
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String userRole = String.valueOf(tokenData.get("role"));
            
            // 设置用户角色，用于Service层判断是否需要脱敏
            req.setUserRole(userRole);
            
            // 根据用户角色设置权限参数
            if ("alumni".equals(userRole)) {
                // 校友可以查看所有校友，不设置权限限制
                req.setAlumniInfoManagementPermission(null);
                req.setPermissionSchool(null);
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证校友信息管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 1);
                if (!permissionResult.isSuccess()) {
                    result.put("code", permissionResult.getCode());
                    result.put("msg", permissionResult.getMessage());
                    return result;
                }
                
                // 设置管理员的权限参数
                Admin admin = permissionResult.getAdmin();
                req.setAlumniInfoManagementPermission(admin.getAlumniInfoManagementPermission());
                req.setPermissionSchool(admin.getAccessibleSchools());
            } else {
                // 其他角色无权限
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }

            // 设置默认值
            if (req.getPageNum() == null) req.setPageNum(1);
            if (req.getPageSize() == null) req.setPageSize(10);
            
            // 计算分页偏移量
            req.setOffset((req.getPageNum() - 1) * req.getPageSize());
            
            List<AlumniSearchResultVO> list = alumniSearchService.searchAlumniByChapter(req);
            int total = alumniSearchService.countAlumniByChapter(req);
            
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("查询分会成员失败", e);
            result.put("code", 1);
            result.put("msg", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/api/user/info")
    public Map<String, Object> getUserInfo(@RequestParam("alumniId") String alumniId, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证token有效性
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 401);
                result.put("msg", "未登录或token无效");
                return result;
            }

            // 解析token获取用户信息
            Map<String, Object> tokenData = JwtUtil.parseToken(token);
            String userRole = String.valueOf(tokenData.get("role"));
            
            // 权限检查：校友可以查看所有校友信息，管理员需要验证权限
            if ("alumni".equals(userRole)) {
                // 校友可以查看所有校友信息，无需额外权限验证
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证校友信息管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 1);
                if (!permissionResult.isSuccess()) {
                    result.put("code", permissionResult.getCode());
                    result.put("msg", permissionResult.getMessage());
                    return result;
                }
            } else {
                // 其他角色无权限
                result.put("code", 403);
                result.put("msg", "无权限访问");
                return result;
            }

            com.alumni.back.Entity.Alumni alumni = registerMapper.findByAlumniId(alumniId);
            if (alumni != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("salutation", alumni.getSalutation());
                data.put("firstName", alumni.getFirstName());
                data.put("lastName", alumni.getLastName());
                data.put("chineseName", alumni.getChineseName());
                data.put("email", alumni.getEmail());
                data.put("contactNumber", alumni.getContactNumber());
                data.put("wechatId", alumni.getWechatId());
                data.put("correspondenceAddress", alumni.getCorrespondenceAddress());
                data.put("currentLocation", alumni.getCurrentLocation());
                result.put("code", 0);
                result.put("data", data);
            } else {
                result.put("code", 1);
                result.put("msg", "未找到校友信息");
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            result.put("code", 1);
            result.put("msg", "获取用户信息失败：" + e.getMessage());
        }
        return result;
    }
} 