package com.alumni.back.visitrecord;

import com.alumni.back.Entity.VisitRecord;
import com.alumni.back.Entity.VisitRecord.VisitStatus;
import com.alumni.back.util.PermissionUtil;
import com.alumni.back.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

@RestController
@RequestMapping("/api/visitrecord")
public class VisitRecordController {
    @Autowired
    private VisitRecordService visitRecordService;

    @Autowired
    private PermissionUtil permissionUtil;

    // 校友端创建预约记录
    @PostMapping("/alumni/visit-records")
    public ResponseEntity<Map<String, Object>> createAlumniVisitRecord(@RequestBody VisitRecord visitRecord, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友可以创建自己的预约记录，无需额外权限验证
            // 确保预约记录属于当前校友
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            
            
            if (!alumniId.equals(visitRecord.getAlumniId())) {
                result.put("code", 403);
                result.put("msg", "只能创建自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
        
        visitRecordService.createVisitRecord(visitRecord);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        
        return ResponseEntity.ok(response);
    }

    // 校友端更新预约记录
    @PutMapping("/alumni/visit-records/{visitUUID}")
    public ResponseEntity<Map<String, Object>> updateAlumniVisitRecord(
            @PathVariable String visitUUID,
            @RequestBody VisitRecord visitRecord,
            @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友只能更新自己的预约记录
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            VisitRecord existingRecord = visitRecordService.getVisitRecordDetail(visitUUID);
            if (existingRecord == null || !existingRecord.getAlumniId().equals(alumniId)) {
                result.put("code", 403);
                result.put("msg", "只能更新自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
            
            // 确保更新的记录仍然属于当前校友
            if (!alumniId.equals(visitRecord.getAlumniId())) {
                result.put("code", 403);
                result.put("msg", "只能更新自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
        
        visitRecord.setVisitUUID(visitUUID);
        visitRecordService.updateVisitRecord(visitRecord);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        
        return ResponseEntity.ok(response);
    }

    // 管理员端创建预约记录
    @PostMapping("/admin/visit-records")
    public ResponseEntity<Map<String, Object>> createVisitRecord(@RequestBody VisitRecord visitRecord, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友可以创建自己的预约记录，无需额外权限验证
            // 确保预约记录属于当前校友
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            if (!alumniId.equals(visitRecord.getAlumniId())) {
                result.put("code", 403);
                result.put("msg", "只能创建自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
        } else if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 读写权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 2);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
        
        visitRecordService.createVisitRecord(visitRecord);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        
        return ResponseEntity.ok(response);
    }

    // 获取预约记录列表
    @GetMapping("/admin/visit-records")
    public ResponseEntity<Map<String, Object>> getVisitRecordList(
            @RequestParam(required = false) String visitSchool,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visitDay,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友只能查看自己的预约记录
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            List<VisitRecord> records = visitRecordService.getVisitRecordList(null, null, null, 1, 1000);
            records = records.stream()
                    .filter(record -> record.getAlumniId().equals(alumniId))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", records);
            response.put("total", records.size());
            
            return ResponseEntity.ok(response);
        } else if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 只读权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 1);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
            
            // 获取管理员的可访问学校范围
            String accessibleSchools = permissionResult.getAdmin().getAccessibleSchools();
            
            List<VisitRecord> records;
            int total;
            
            if (accessibleSchools == null || accessibleSchools.trim().isEmpty()) {
                // 如果管理员没有设置可访问学校，使用普通查询方法
                records = visitRecordService.getVisitRecordList(visitSchool, status, visitDay, pageNum, pageSize);
                total = visitRecordService.getVisitRecordCount(visitSchool, status, visitDay);
            } else {
                // 如果管理员设置了可访问学校，使用权限过滤查询方法
                records = visitRecordService.getVisitRecordListByAdminPermission(visitSchool, status, visitDay, pageNum, pageSize, accessibleSchools);
                total = visitRecordService.getVisitRecordCountByAdminPermission(visitSchool, status, visitDay, accessibleSchools);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", records);
            response.put("total", total);
            
            return ResponseEntity.ok(response);
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
    }

    // 获取管理员可访问的学校列表
    @GetMapping("/admin/visit-records/accessible-schools")
    public ResponseEntity<Map<String, Object>> getAccessibleSchools(@RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 只读权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 1);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
            
            // 获取管理员的可访问学校范围
            String accessibleSchools = permissionResult.getAdmin().getAccessibleSchools();
            List<String> schools;
            
            if (accessibleSchools == null || accessibleSchools.trim().isEmpty()) {
                // 如果管理员没有设置可访问学校，返回所有可能的学校代码
                schools = Arrays.asList(
                    "YCIS_HK", "YCIS_BJ", "YCIS_CQ", "YCIS_QD", "YCIS_SH",
                    "YWIES_BJ", "YWIES_GZ", "YWIES_SH_GB", "YWIES_SH_LG", "YWIES_TX", "YWIES_YT"
                );
            } else {
                // 使用服务方法转换学校名称到代码
                schools = visitRecordService.getAccessibleSchools(accessibleSchools);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", schools);
            
            return ResponseEntity.ok(response);
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
    }

    // 获取预约记录详情
    @GetMapping("/admin/visit-records/{visitUUID}")
    public ResponseEntity<Map<String, Object>> getVisitRecordDetail(@PathVariable String visitUUID, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友只能查看自己的预约记录详情
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            VisitRecord record = visitRecordService.getVisitRecordDetail(visitUUID);
            if (record == null || !record.getAlumniId().equals(alumniId)) {
                result.put("code", 403);
                result.put("msg", "无权限查看此预约记录");
                return ResponseEntity.status(403).body(result);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } else if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 只读权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 1);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
            
            VisitRecord record = visitRecordService.getVisitRecordDetail(visitUUID);
            if (record == null) {
                result.put("code", 404);
                result.put("msg", "预约记录不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
    }

    // 更新预约状态
    @PutMapping("/admin/visit-records/status")
    public ResponseEntity<Map<String, Object>> updateVisitStatus(@RequestBody Map<String, String> params, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 读写权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 2);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
            
            String visitUUID = params.get("visitUUID");
            String status = params.get("status");
            String remark = params.getOrDefault("remark", "");
            
            if (visitUUID == null || status == null) {
                result.put("code", 400);
                result.put("msg", "参数不完整");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 直接调用Service的updateVisitStatus方法
            visitRecordService.updateVisitStatus(visitUUID, VisitRecord.VisitStatus.valueOf(status), remark);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "success");
            
            return ResponseEntity.ok(response);
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
    }

    // 更新预约记录
    @PutMapping("/admin/visit-records/{visitUUID}")
    public ResponseEntity<Map<String, Object>> updateVisitRecord(
            @PathVariable String visitUUID,
            @RequestBody VisitRecord visitRecord,
            @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友只能更新自己的预约记录
            String alumniId = String.valueOf(tokenData.get("alumni_id"));
            VisitRecord existingRecord = visitRecordService.getVisitRecordDetail(visitUUID);
            if (existingRecord == null || !existingRecord.getAlumniId().equals(alumniId)) {
                result.put("code", 403);
                result.put("msg", "只能更新自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
            
            // 确保更新的记录仍然属于当前校友
            if (!alumniId.equals(visitRecord.getAlumniId())) {
                result.put("code", 403);
                result.put("msg", "只能更新自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
        } else if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 读写权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 2);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
        
        visitRecord.setVisitUUID(visitUUID);
        visitRecordService.updateVisitRecord(visitRecord);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        
        return ResponseEntity.ok(response);
    }

    // 批量更新预约状态
    @PutMapping("/admin/visit-records/batch-status")
    public ResponseEntity<Map<String, Object>> batchUpdateVisitStatus(@RequestBody Map<String, Object> params, @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 读写权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 2);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
            
            @SuppressWarnings("unchecked")
            List<String> visitUUIDs = (List<String>) params.get("visitUUIDs");
            String status = String.valueOf(params.get("status"));
            
            if (visitUUIDs == null || visitUUIDs.isEmpty() || status == null) {
                result.put("code", 400);
                result.put("msg", "参数不完整");
                return ResponseEntity.badRequest().body(result);
            }
            
            for (String visitUUID : visitUUIDs) {
                VisitRecord record = visitRecordService.getVisitRecordDetail(visitUUID);
                if (record != null) {
                    record.setStatus(VisitStatus.valueOf(status));
                    visitRecordService.updateVisitRecord(record);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "success");
            
            return ResponseEntity.ok(response);
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
    }

    // 获取校友的预约记录列表
    @GetMapping("/admin/visit-records/alumni")
    public ResponseEntity<Map<String, Object>> getAlumniVisitRecordList(
            @RequestParam String alumniId,
            @RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证token有效性
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return ResponseEntity.status(401).body(result);
        }

        // 解析token获取用户信息
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String userRole = String.valueOf(tokenData.get("role"));
        
        if ("alumni".equals(userRole)) {
            // 校友只能查看自己的预约记录
            String tokenAlumniId = String.valueOf(tokenData.get("alumni_id"));
            if (!tokenAlumniId.equals(alumniId)) {
                result.put("code", 403);
                result.put("msg", "只能查看自己的预约记录");
                return ResponseEntity.status(403).body(result);
            }
        } else if ("admin".equals(userRole)) {
            // 管理员需要验证校园参观记录权限 - 只读权限
            PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 1);
            if (!permissionResult.isSuccess()) {
                result.put("code", permissionResult.getCode());
                result.put("msg", permissionResult.getMessage());
                return ResponseEntity.status(permissionResult.getCode()).body(result);
            }
        } else {
            // 其他角色无权限
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }
        
        List<VisitRecord> records = visitRecordService.getVisitRecordList(null, null, null, 1, 1000);
        records = records.stream()
                .filter(record -> record.getAlumniId().equals(alumniId))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", records);
        response.put("total", records.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-visit-record-count")
    public Map<String, Object> getPendingVisitRecordCount(@RequestAttribute("Authorization") String token) {
        // 校园参观预约管理权限校验（只读权限即可）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateCampusVisitRecordPermission(token, 1);
        Map<String, Object> result = new HashMap<>();
        if (!permissionResult.isSuccess()) {
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        int count = visitRecordService.getPendingVisitRecordCount();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", count);
        return result;
    }
} 