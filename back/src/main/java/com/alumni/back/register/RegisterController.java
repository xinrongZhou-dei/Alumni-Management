package com.alumni.back.register;

import com.alumni.back.admin.AdminMapper;
import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.TertiaryInformation;
import com.alumni.back.Entity.CareerInformation;
import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.alumni.back.util.JwtUtil;
import com.alumni.back.Entity.Admin;

import java.util.ArrayList;
import com.alumni.back.dto.MatchGmiRequest;
import com.alumni.back.common.Result;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    @Autowired
    private RegisterService registerService;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private PermissionUtil permissionUtil;

    @PostMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return registerService.verifyEmail(email);
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyCode(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        String verifyCode = request.get("verify_code");
        return registerService.verifyCode(token, verifyCode);
    }

    @PostMapping("/set-password")
    public Map<String, Object> setPassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        return registerService.setPassword(token, password);
    }

    @PutMapping("/alumni-detail")
    public Map<String, Object> setAlumniInfo(@RequestAttribute("Authorization") String token, @RequestBody Map<String, Object> request) {
        return registerService.setAlumniInfo(token, request);
    }

    @GetMapping("/get-alumni-id")
    public Map<String, Object> getAlumniId(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        Alumni alumni = registerService.findByEmail(email);
        if (alumni != null) {
            result.put("code", 0);
            result.put("msg", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("alumni_id", alumni.getAlumniId());
            result.put("data", data);
        } else {
            result.put("code", 1);
            result.put("msg", "未找到校友信息");
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/verify-alumni-id")
    public Map<String, Object> verifyAlumniId(@RequestParam String alumniId) {
        Map<String, Object> result = new HashMap<>();
        // 验证学号格式（9位数字）
        if (alumniId == null || !alumniId.matches("^\\d{9}$")) {
            result.put("code", 1);
            result.put("msg", "学号格式不正确，请输入9位数字");
            result.put("data", null);
            return result;
        }
        
        // 检查学号是否已存在
        Alumni existingAlumni = registerService.findByAlumniId(alumniId);
        if (existingAlumni != null) {
            result.put("code", 1);
            result.put("msg", "该学号已被注册");
            result.put("data", null);
        } else {
            result.put("code", 0);
            result.put("msg", "学号有效");
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/alumni-list")
    public Map<String, Object> getAlumniList(
            @RequestParam(required = false) String alumniId,
            @RequestParam(required = false) String ycywSchoolsAttended,
            @RequestParam(required = false) String chineseName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestAttribute("Authorization") String token) {
        
        // 验证校友信息管理权限（至少需要只读权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }

        // 获取管理员权限信息
        Admin admin = permissionResult.getAdmin();
        
        // 构建权限信息，用于后端过滤数据
        Map<String, Object> adminPermissionInfo = new HashMap<>();
        adminPermissionInfo.put("permissionLevel", admin.getAlumniInfoManagementPermission());
        adminPermissionInfo.put("accessibleSchools", admin.getAccessibleSchools());
        
        java.util.List<Integer> stateList = null;
        if (state != null && !state.isEmpty()) {
            stateList = new java.util.ArrayList<>();
            for (String s : state.split(",")) {
                try { stateList.add(Integer.valueOf(s.trim())); } catch (Exception ignore) {} 
            }
        }
        return registerService.getAlumniList(alumniId, ycywSchoolsAttended, chineseName, email, contactNumber, tagIds, stateList, page, pageSize, adminPermissionInfo);
    }

    @GetMapping("/alumni-detail/{alumniId}")
    public Map<String, Object> getAlumniDetail(@PathVariable String alumniId) {
        return registerService.getAlumniDetailById(alumniId);
    }

    @PutMapping("/alumni-update")
    public Map<String, Object> updateAlumni(@RequestBody Map<String, Object> request, @RequestAttribute("Authorization") String token) {
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
            String userAlumniId = String.valueOf(tokenData.get("alumni_id"));
            
            // 获取要编辑的校友ID
            Map<String, Object> alumniMap = (Map<String, Object>) request.get("alumni");
            String targetAlumniId = (String) alumniMap.get("alumni_id");
            
            // 权限检查：校友只能编辑自己的信息，管理员可以编辑任何校友的信息
            if ("alumni".equals(userRole)) {
                // 校友只能编辑自己的信息
                if (!userAlumniId.equals(targetAlumniId)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", 403);
                    result.put("msg", "只能编辑自己的信息");
                    return result;
                }
            } else if ("admin".equals(userRole)) {
                // 管理员需要验证校友信息管理权限
                PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 2);
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

            Alumni alumni = new Alumni();
            // 设置校友基本信息
            alumni.setAlumniId((String) alumniMap.get("alumni_id"));
            alumni.setSalutation((String) alumniMap.get("salutation"));
            alumni.setFirstName((String) alumniMap.get("first_name"));
            alumni.setLastName((String) alumniMap.get("last_name"));
            alumni.setChineseName((String) alumniMap.get("chinese_name"));
            alumni.setEmail((String) alumniMap.get("email"));
            alumni.setContactNumber((String) alumniMap.get("contact_number"));
            alumni.setWechatId((String) alumniMap.get("wechat_id"));
            alumni.setCorrespondenceAddress((String) alumniMap.get("correspondence_address"));
            alumni.setCurrentLocation((String) alumniMap.get("current_location"));
            alumni.setZohoAlumniNumber((String) alumniMap.get("zoho_alumni_number"));
            alumni.setAffiliation((String) alumniMap.get("affiliation"));
            alumni.setYcywSchoolsAttended((String) alumniMap.get("ycyw_schools_attended"));
            alumni.setStudyPeriodStart((String) alumniMap.get("study_period_start"));
            alumni.setStudyPeriodEnd((String) alumniMap.get("study_period_end"));
            alumni.setLastSchoolDay((String) alumniMap.get("last_school_day"));
            alumni.setYearLeft(alumniMap.get("year_left") == null ? null : Integer.valueOf(alumniMap.get("year_left").toString()));
            alumni.setMaritalStatus((String) alumniMap.get("marital_status"));
            alumni.setShowYearLeft(alumniMap.get("show_year_left") == null ? null : Boolean.valueOf(alumniMap.get("show_year_left").toString()));
            alumni.setShowTertiaryUniversity(alumniMap.get("show_tertiary_university") == null ? null : Boolean.valueOf(alumniMap.get("show_tertiary_university").toString()));
            alumni.setShowTertiaryMajor(alumniMap.get("show_tertiary_major") == null ? null : Boolean.valueOf(alumniMap.get("show_tertiary_major").toString()));
            alumni.setShowCareerCompany(alumniMap.get("show_career_company") == null ? null : Boolean.valueOf(alumniMap.get("show_career_company").toString()));
            alumni.setShowJobTitle(alumniMap.get("show_job_title") == null ? null : Boolean.valueOf(alumniMap.get("show_job_title").toString()));
            alumni.setShowIndustry(alumniMap.get("show_industry") == null ? null : Boolean.valueOf(alumniMap.get("show_industry").toString()));
            alumni.setShowCountry(alumniMap.get("show_country") == null ? null : Boolean.valueOf(alumniMap.get("show_country").toString()));
            alumni.setRegion((String) alumniMap.get("region"));
            alumni.setTagIds((String) alumniMap.get("tag_ids"));
            alumni.setRole((String) alumniMap.get("role"));
            alumni.setPassword((String) alumniMap.get("password"));
            alumni.setBirthday((String) alumniMap.get("birthday"));

            // 处理教育信息
            List<Map<String, Object>> tertiaryList = (List<Map<String, Object>>) request.get("tertiary_information");
            List<TertiaryInformation> tertiaryInfoList = null;
            if (tertiaryList != null) {
                tertiaryInfoList = tertiaryList.stream().map(t -> {
                    TertiaryInformation info = new TertiaryInformation();
                    info.setId((String) t.get("id"));
                    info.setAlumniId(alumni.getAlumniId());
                    info.setUniversityCollege((String) t.get("university_college"));
                    info.setDegree((String) t.get("degree"));
                    info.setMajor((String) t.get("major"));
                    info.setGraduationYear(t.get("graduation_year") == null ? null : Integer.valueOf(t.get("graduation_year").toString()));
                    info.setCountryRegion((String) t.get("country_region"));
                    return info;
                }).toList();
            }

            // 处理工作信息
            List<Map<String, Object>> careerList = (List<Map<String, Object>>) request.get("career_information");
            // 兼容前端拼写错误
            if (careerList == null) {
                careerList = (List<Map<String, Object>>) request.get("carrer_information");
            }
            List<CareerInformation> careerInfoList = null;
            if (careerList != null) {
                careerInfoList = careerList.stream().map(c -> {
                    CareerInformation info = new CareerInformation();
                    info.setId((String) c.get("id"));
                    info.setAlumniId(alumni.getAlumniId());
                    info.setCompanyOrganization((String) c.get("company_organization"));
                    info.setJobTitle((String) c.get("job_title"));
                    info.setIndustry(CareerInformation.Industry.valueOf((String) c.get("industry")));
                    info.setCountryRegion((String) c.get("country_region"));
                    return info;
                }).toList();
            }

            return registerService.updateAlumniById(alumni, tertiaryInfoList, careerInfoList);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("msg", "服务器内部错误");
            return result;
        }
    }

    @DeleteMapping("/tertiary/{id}")
    public Map<String, Object> deleteTertiary(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        int deleted = registerService.deleteTertiaryById(id);
        result.put("code", deleted > 0 ? 0 : 1);
        result.put("msg", deleted > 0 ? "删除成功" : "删除失败");
        return result;
    }

    @DeleteMapping("/career/{id}")
    public Map<String, Object> deleteCareer(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        int deleted = registerService.deleteCareerById(id);
        result.put("code", deleted > 0 ? 0 : 1);
        result.put("msg", deleted > 0 ? "删除成功" : "删除失败");
        return result;
    }

    // 管理员接口：清理过期的注册token
    @PostMapping("/clean-expired-tokens")
    public Map<String, Object> cleanExpiredTokens(@RequestHeader("Authorization") String token) {
        // 检查管理员权限
        if (!JwtUtil.isTokenValid(token)) {
            return Map.of("code", 401, "msg", "未登录或token无效");
        }
        try {
            String role = String.valueOf(JwtUtil.parseToken(token).get("role"));
            if (!"admin".equals(role)) {
                return Map.of("code", 403, "msg", "无权限");
            }
        } catch (Exception e) {
            return Map.of("code", 401, "msg", "token解析失败");
        }
        
        return registerService.cleanExpiredTokens();
    }

    @DeleteMapping("/alumni-list")
    public Map<String, Object> deleteAlumni(@RequestBody Map<String, java.util.List<String>> request, @RequestAttribute("Authorization") String token) {
        // 验证校友信息管理权限（需要读写权限）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 2);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }

        java.util.List<String> alumniIds = request.get("alumniIds");
        int success = 0;
        for (String id : alumniIds) {
            // 按照外键依赖顺序删除相关数据
            // 1. 删除活动报名记录
            registerService.deleteActivityRegistrationByAlumniId(id);
            // 2. 删除分会申请记录
            registerService.deleteChapterApplicationByAlumniId(id);
            // 3. 删除校园参观预约记录
            registerService.deleteVisitRecordByAlumniId(id);
            // 4. 删除学籍信息（根据校友ID删除所有学籍记录）
            registerMapper.deleteTertiaryByAlumniId(id);
            // 5. 删除工作信息（根据校友ID删除所有工作记录）
            registerMapper.deleteCareerByAlumniId(id);
            // 6. 最后删除校友基础信息
            success += registerService.deleteAlumniById(id);
        }
        Map<String, Object> result = new HashMap<>();
        if (success > 0) {
            result.put("code", 0);
            result.put("msg", "删除成功");
        } else {
            result.put("code", 1);
            result.put("msg", "删除失败");
        }
        return result;
    }

    @PostMapping("/generate-alumni-id")
    public Map<String, Object> generateAlumniId() {
        Map<String, Object> result = new HashMap<>();
        try {
            String generatedId = registerService.generateUniqueAlumniId();
            result.put("code", 0);
            result.put("msg", "生成成功");
            result.put("data", generatedId);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "生成学号失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PostMapping("/audit-approve")
    public Map<String, Object> auditApprove(@RequestBody Map<String, String> request, @RequestAttribute("Authorization") String token) {
        String alumniId = request.get("alumniId");
        Map<String, Object> result = new HashMap<>();
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "alumniId不能为空");
            return result;
        }
        // 权限校验（只允许管理员）
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String role = String.valueOf(tokenData.get("role"));
        if (!"admin".equals(role)) {
            result.put("code", 403);
            result.put("msg", "无权限");
            return result;
        }
        Alumni alumni = registerService.findByAlumniId(alumniId);
        if (alumni == null) {
            result.put("code", 1);
            result.put("msg", "未找到该校友");
            return result;
        }
        alumni.setState(1);
        registerService.updateAlumniById(alumni, null, null);
        result.put("code", 0);
        result.put("msg", "审核通过，已激活");
        return result;
    }

    @PostMapping("/audit-reject")
    public Map<String, Object> auditReject(@RequestBody Map<String, String> request, @RequestAttribute("Authorization") String token) {
        String alumniId = request.get("alumniId");
        Map<String, Object> result = new HashMap<>();
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "alumniId不能为空");
            return result;
        }
        // 权限校验（只允许管理员）
        Map<String, Object> tokenData = JwtUtil.parseToken(token);
        String role = String.valueOf(tokenData.get("role"));
        if (!"admin".equals(role)) {
            result.put("code", 403);
            result.put("msg", "无权限");
            return result;
        }
        Alumni oldAlumni = registerService.findByAlumniId(alumniId);
        if (oldAlumni == null) {
            result.put("code", 1);
            result.put("msg", "未找到该校友");
            return result;
        }
        // 生成10位唯一学号
        String newAlumniId;
        do {
            newAlumniId = String.valueOf((long)(Math.random() * 9_000_000_000L) + 1_000_000_000L);
        } while (registerService.findByAlumniId(newAlumniId) != null);
        // 构造新校友对象
        Alumni newAlumni = new Alumni();
        newAlumni.setAlumniId(newAlumniId);
        newAlumni.setSalutation(oldAlumni.getSalutation());
        newAlumni.setFirstName(oldAlumni.getFirstName());
        newAlumni.setLastName(oldAlumni.getLastName());
        newAlumni.setChineseName(oldAlumni.getChineseName());
        newAlumni.setEmail(oldAlumni.getEmail());
        newAlumni.setContactNumber(oldAlumni.getContactNumber());
        newAlumni.setWechatId(oldAlumni.getWechatId());
        newAlumni.setCorrespondenceAddress(oldAlumni.getCorrespondenceAddress());
        newAlumni.setCurrentLocation(oldAlumni.getCurrentLocation());
        newAlumni.setZohoAlumniNumber(oldAlumni.getZohoAlumniNumber());
        newAlumni.setAffiliation(oldAlumni.getAffiliation());
        newAlumni.setYcywSchoolsAttended(oldAlumni.getYcywSchoolsAttended());
        newAlumni.setStudyPeriodStart(oldAlumni.getStudyPeriodStart());
        newAlumni.setStudyPeriodEnd(oldAlumni.getStudyPeriodEnd());
        newAlumni.setLastSchoolDay(oldAlumni.getLastSchoolDay());
        newAlumni.setYearLeft(oldAlumni.getYearLeft());
        newAlumni.setMaritalStatus(oldAlumni.getMaritalStatus());
        newAlumni.setShowYearLeft(oldAlumni.getShowYearLeft());
        newAlumni.setShowTertiaryUniversity(oldAlumni.getShowTertiaryUniversity());
        newAlumni.setShowTertiaryMajor(oldAlumni.getShowTertiaryMajor());
        newAlumni.setShowCareerCompany(oldAlumni.getShowCareerCompany());
        newAlumni.setShowJobTitle(oldAlumni.getShowJobTitle());
        newAlumni.setShowIndustry(oldAlumni.getShowIndustry());
        newAlumni.setShowCountry(oldAlumni.getShowCountry());
        newAlumni.setPassword(oldAlumni.getPassword());
        newAlumni.setUpdateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        newAlumni.setRole(oldAlumni.getRole());
        newAlumni.setTagIds(oldAlumni.getTagIds());
        newAlumni.setRegion(oldAlumni.getRegion());
        newAlumni.setBirthday(oldAlumni.getBirthday());
        newAlumni.setState(3); // 已拒绝
        // 插入新校友
        registerService.insertAlumni(newAlumni);
        // 删除原校友相关信息
        registerService.deleteTertiaryById(alumniId);
        registerService.deleteCareerById(alumniId);
        registerService.deleteAlumniById(alumniId);
        result.put("code", 0);
        result.put("msg", "审核拒绝，已归档新记录");
        result.put("newAlumniId", newAlumniId);
        return result;
    }

    @PostMapping("/match-gmi")
    public Result<List<Alumni>> matchGmiData(@RequestBody MatchGmiRequest request) {
        return Result.success(registerService.matchGmiData(request));
    }

    @PostMapping("/match-and-activate")
    public Result<?> matchAndActivate(@RequestBody Map<String, String> req) {
        String gmiAlumniId = req.get("gmiAlumniId");
        String reviewAlumniId = req.get("reviewAlumniId");
        registerService.matchAndActivate(gmiAlumniId, reviewAlumniId);
        return Result.success(null);
    }

    @GetMapping("/pending-alumni-count")
    public Map<String, Object> getPendingAlumniCount(@RequestAttribute("Authorization") String token) {
        // 校友信息审核权限校验（只读权限即可）
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateAlumniInfoManagementPermission(token, 1);
        Map<String, Object> result = new HashMap<>();
        if (!permissionResult.isSuccess()) {
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        int count = registerService.getPendingAlumniCount();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", count);
        return result;
    }

} 