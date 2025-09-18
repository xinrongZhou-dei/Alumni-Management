package com.alumni.back.chapter;

import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alumni.back.util.JwtUtil;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ChapterApplicationService chapterApplicationService;

    @Autowired
    private PermissionUtil permissionUtil;

    @GetMapping
    public ResponseEntity<?> getChapters() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterService.getChapters());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createChapter(@RequestBody Map<String, String> request) {
        String alumniId = request.get("alumniId");
        String branchName = request.get("branchName");

        if (alumniId == null || branchName == null) {
            return ResponseEntity.badRequest().body("参数不完整");
        }

        boolean success = chapterService.createChapter(alumniId, branchName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "创建成功" : "创建失败");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyChapters(@RequestParam String alumniId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterService.getMyChapters(alumniId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/review-list")
    public ResponseEntity<?> getReviewList(@RequestAttribute("Authorization") String token) {
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateChapterReviewPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return ResponseEntity.status(permissionResult.getCode()).body(result);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterService.getReviewList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review-status")
    public ResponseEntity<?> updateReviewStatus(@RequestBody Map<String, Object> req, @RequestAttribute("Authorization") String token) {
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateChapterReviewPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return ResponseEntity.status(permissionResult.getCode()).body(result);
        }
        chapterService.updateReviewStatus((List<Integer>) req.get("tagIds"), (String) req.get("status"));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinChapter(@RequestBody Map<String, Object> req) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("msg", "禁止直接加入分会，请通过申请和审核流程");
        return ResponseEntity.status(403).body(response);
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveChapter(@RequestBody Map<String, Object> req) {
        String alumniId = String.valueOf(req.get("alumniId"));
        Integer tagId = null;
        Object tagIdObj = req.get("tagId");
        if (tagIdObj instanceof Integer) {
            tagId = (Integer) tagIdObj;
        } else if (tagIdObj instanceof Number) {
            tagId = ((Number) tagIdObj).intValue();
        } else if (tagIdObj != null) {
            tagId = Integer.valueOf(tagIdObj.toString());
        }
        Map<String, Object> response = new HashMap<>();
        if (alumniId == null || tagId == null) {
            response.put("success", false);
            response.put("msg", "参数不完整");
            return ResponseEntity.badRequest().body(response);
        }
        boolean success = chapterService.leaveChapter(alumniId, tagId);
        response.put("success", success);
        response.put("msg", success ? "退出成功" : "退出失败");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-with-joined")
    public ResponseEntity<?> getChaptersWithJoined(@RequestParam String alumniId) {
        Map<String, Object> response = new HashMap<>();
        // 查询所有已审核通过的分会
        List<Map<String, Object>> chapters = chapterService.getChaptersWithMap();
        // 查询当前校友已加入的分会id
        String tagIds = chapterService.getAlumnusTagIds(alumniId);
        List<String> joinedTagIds = new java.util.ArrayList<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            for (String id : tagIds.split(",")) {
                if (!id.trim().isEmpty()) joinedTagIds.add(id.trim());
            }
        }
        // 查询申请状态
        List<Integer> allTagIds = chapters.stream()
            .map(chapter -> (Integer) chapter.get("tag_id"))
            .collect(java.util.stream.Collectors.toList());
        Map<String, String> applicationStatus = chapterService.getApplicationStatus(alumniId, allTagIds);
        
        response.put("success", true);
        response.put("data", chapters);
        response.put("joinedTagIds", joinedTagIds);
        response.put("applicationStatus", applicationStatus);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot-chapters")
    public ResponseEntity<?> getHotChapters(@RequestParam String alumniId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取热门分会（前4个）
            List<Map<String, Object>> hotChapters = chapterService.getHotChapters();
            // 查询当前校友已加入的分会id
            String tagIds = chapterService.getAlumnusTagIds(alumniId);
            List<String> joinedTagIds = new java.util.ArrayList<>();
            if (tagIds != null && !tagIds.isEmpty()) {
                for (String id : tagIds.split(",")) {
                    if (!id.trim().isEmpty()) joinedTagIds.add(id.trim());
                }
            }
            // 查询申请状态
            List<Integer> allTagIds = hotChapters.stream()
                .map(chapter -> (Integer) chapter.get("tag_id"))
                .collect(java.util.stream.Collectors.toList());
            Map<String, String> applicationStatus = chapterService.getApplicationStatus(alumniId, allTagIds);
            response.put("code", 0);
            response.put("data", hotChapters);
            response.put("joinedTagIds", joinedTagIds);
            response.put("applicationStatus", applicationStatus);
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "获取热门分会失败：" + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 申请加入分会
    @PostMapping("/apply")
    public ResponseEntity<?> applyForChapter(@RequestBody Map<String, Object> req) {
        String alumniId = String.valueOf(req.get("alumniId"));
        Integer tagId = null;
        try {
            tagId = Integer.valueOf(String.valueOf(req.get("tagId")));
        } catch (Exception e) {
            tagId = null;
        }
        Map<String, Object> response = new HashMap<>();
        if (alumniId == null || alumniId.equals("null") || alumniId.isEmpty() || tagId == null) {
            response.put("code", 1);
            response.put("msg", "参数不完整");
            return ResponseEntity.badRequest().body(response);
        }
        boolean success = chapterApplicationService.applyForChapter(alumniId, tagId);
        response.put("code", success ? 0 : 1);
        response.put("msg", success ? "申请成功" : "申请失败");
        return ResponseEntity.ok(response);
    }

    // 获取所有申请列表（管理员端）
    @GetMapping("/applications")
    public ResponseEntity<?> getAllApplications(@RequestAttribute("Authorization") String token) {
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateChapterReviewPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return ResponseEntity.status(permissionResult.getCode()).body(result);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterApplicationService.getAllApplications());
        return ResponseEntity.ok(response);
    }

    // 获取我的申请列表（校友端）
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(@RequestParam String alumniId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterApplicationService.getMyApplications(alumniId));
        return ResponseEntity.ok(response);
    }

    // 获取分会申请列表（分会负责人）
    @GetMapping("/chapter-applications")
    public ResponseEntity<?> getChapterApplications(@RequestParam Integer tagId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chapterApplicationService.getChapterApplications(tagId));
        return ResponseEntity.ok(response);
    }

    // 审核申请
    @PostMapping("/review-application")
    public ResponseEntity<?> reviewApplication(@RequestBody Map<String, Object> req, @RequestAttribute("Authorization") String token) {
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateChapterReviewPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return ResponseEntity.status(permissionResult.getCode()).body(result);
        }
        String applicationId = String.valueOf(req.get("applicationId"));
        String status = String.valueOf(req.get("status"));
        Map<String, Object> response = new HashMap<>();
        if (applicationId == null || status == null) {
            response.put("success", false);
            response.put("msg", "参数不完整");
            return ResponseEntity.badRequest().body(response);
        }
        boolean success = chapterApplicationService.reviewApplication(applicationId, status);
        response.put("success", success);
        response.put("msg", success ? "审核成功" : "审核失败");
        return ResponseEntity.ok(response);
    }

    // 解散分会
    @PostMapping("/dissolve")
    public ResponseEntity<?> dissolveChapter(@RequestBody Map<String, Object> req) {
        String alumniId = String.valueOf(req.get("alumniId"));
        Integer tagId = null;
        try {
            tagId = Integer.valueOf(String.valueOf(req.get("tagId")));
        } catch (Exception e) {
            tagId = null;
        }
        Map<String, Object> response = new HashMap<>();
        if (alumniId == null || alumniId.equals("null") || alumniId.isEmpty() || tagId == null) {
            response.put("success", false);
            response.put("msg", "参数不完整");
            return ResponseEntity.badRequest().body(response);
        }
        // 校验是否为负责人
        boolean isOwner = chapterService.isChapterOwner(alumniId, tagId);
        if (!isOwner) {
            response.put("success", false);
            response.put("msg", "只有分会负责人才能解散分会");
            return ResponseEntity.status(403).body(response);
        }
        boolean success = chapterService.dissolveChapter(tagId);
        response.put("success", success);
        response.put("msg", success ? "解散成功" : "解散失败");
        return ResponseEntity.ok(response);
    }

    // 分会申请批量审核（前端直接传tagId, applicationIds, status）
    @PostMapping("/batch-review")
    public ResponseEntity<?> batchReview(@RequestBody Map<String, Object> req) {
        Integer tagId = Integer.valueOf(String.valueOf(req.get("tagId")));
        @SuppressWarnings("unchecked")
        List<String> applicationIds = (List<String>) req.get("applicationIds");
        String status = String.valueOf(req.get("status"));

        Map<String, Object> response = new HashMap<>();
        boolean allSuccess = true;
        for (String applicationId : applicationIds) {
            boolean success = chapterApplicationService.reviewApplication(applicationId, status);
            if (!success) allSuccess = false;
        }
        response.put("success", allSuccess);
        response.put("msg", allSuccess ? "审核成功" : "部分审核失败");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-chapter-count")
    public Map<String, Object> getPendingChapterCount(@RequestAttribute("Authorization") String token) {
        // 分会审核权限校验（只读权限即可）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateChapterReviewPermission(token, 1);
        Map<String, Object> result = new HashMap<>();
        if (!permissionResult.isSuccess()) {
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        int count = chapterService.getPendingChapterCount();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", count);
        return result;
    }

    @GetMapping("/my-pending-application-count")
    public Map<String, Object> getMyPendingApplicationCount(@RequestAttribute("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }
        // 获取当前用户id
        io.jsonwebtoken.Claims tokenData = JwtUtil.parseToken(token);
        String alumniId = String.valueOf(tokenData.get("alumni_id"));
        
        // 获取当前用户管理的分会tagId列表
        List<com.alumni.back.Entity.Chapter> myChapters = chapterService.getMyChapters(alumniId);
        
        List<Integer> tagIds = new java.util.ArrayList<>();
        for (com.alumni.back.Entity.Chapter chapter : myChapters) {
            if (chapter.getTagId() != null) {
                tagIds.add(chapter.getTagId());
            }
        }
        
        int count = chapterApplicationService.countPendingApplicationsByTagIds(tagIds);
        
        result.put("code", 0);
        result.put("data", count);
        return result;
    }
} 