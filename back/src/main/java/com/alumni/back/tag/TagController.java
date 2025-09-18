package com.alumni.back.tag;

import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam int page, @RequestParam int pageSize, @RequestAttribute("Authorization") String token) {
        // 验证标签管理权限 - 只读权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateTagManagementPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        return tagService.tagList(page, pageSize);
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, String> req, @RequestAttribute("Authorization") String token) {
        // 验证标签管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateTagManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        return tagService.tagAdd(req);
    }

    @DeleteMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, Integer> req, @RequestAttribute("Authorization") String token) {
        // 验证标签管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateTagManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        return tagService.tagDelete(req);
    }

    @PostMapping("/update")
    public Map<String, Object> updateTag(@RequestBody Map<String, Object> req, @RequestAttribute("Authorization") String token) {
        // 验证标签管理权限 - 读写权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateTagManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        return tagService.updateTag(req);
    }
} 