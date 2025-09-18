package com.alumni.back.permission;

import com.alumni.back.common.Result;
import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alumni.back.Entity.Admin;
import com.alumni.back.dto.NewAdminRequestDTO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping("/admins")
    public Result<Map<String, Object>> getAdminList(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestAttribute("Authorization") String token) {
        
        // 验证权限管理权限 - 只读权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validatePermissionManagementPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return Result.error(permissionResult.getCode(), permissionResult.getMessage());
        }
        
        Map<String, Object> data = permissionService.getAdminList(query, pageNum, pageSize);
        return Result.success(data);
    }

    @PostMapping("/admins")
    public Result<?> addAdmin(@RequestBody NewAdminRequestDTO adminRequest, @RequestAttribute("Authorization") String token) {
        // 验证权限管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validatePermissionManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            return Result.error(permissionResult.getCode(), permissionResult.getMessage());
        }
        
        permissionService.addAdmin(adminRequest);
        return Result.success(null);
    }

    @PutMapping("/admins/{id}")
    public Result<?> updateAdmin(@PathVariable String id, @RequestBody Admin admin, @RequestAttribute("Authorization") String token) {
        // 验证权限管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validatePermissionManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            return Result.error(permissionResult.getCode(), permissionResult.getMessage());
        }
        
        permissionService.updateAdmin(id, admin);
        return Result.success(null);
    }

    @DeleteMapping("/admins/{id}")
    public Result<?> deleteAdmin(@PathVariable String id, @RequestAttribute("Authorization") String token) {
        // 验证权限管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validatePermissionManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            return Result.error(permissionResult.getCode(), permissionResult.getMessage());
        }
        
        permissionService.deleteAdmin(id);
        return Result.success(null);
    }
} 