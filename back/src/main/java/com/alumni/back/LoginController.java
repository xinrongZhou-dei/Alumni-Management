package com.alumni.back;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.Admin;
import com.alumni.back.admin.AdminMapper;
import com.alumni.back.register.RegisterMapper;
import com.alumni.back.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.DigestUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private com.alumni.back.register.RegisterService registerService;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private com.alumni.back.wechat.WeChatUtil weChatUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        String alumniId = request.get("alumni_id");
        String password = request.get("password");
        String captchaCode = request.get("captchaCode");
        String captchaUuid = request.get("captchaUuid");
        String redisKey = "captcha:" + captchaUuid;
        String codeInRedis = (String) redisTemplate.opsForValue().get(redisKey);
        // 校验验证码
        if (codeInRedis == null || captchaCode == null || !codeInRedis.equalsIgnoreCase(captchaCode)) {
            // 删除旧验证码
            redisTemplate.delete(redisKey);
            // 生成新验证码
            CaptchaResult newCaptcha = generateAndSaveCaptcha();
            result.put("code", 1001);
            result.put("msg", "验证码错误或已失效");
            result.put("captcha", newCaptcha);
            return result;
        }
        // 校验通过后删除验证码，防止重用
        redisTemplate.delete(redisKey);
        Alumni alumni = registerMapper.findByAlumniId(alumniId);
        if (alumni == null) {
            CaptchaResult newCaptcha = generateAndSaveCaptcha();
            result.put("code", 1);
            result.put("msg", "用户不存在");
            result.put("captcha", newCaptcha);
            return result;
        }
        if (!password.equals(alumni.getPassword())) {
            CaptchaResult newCaptcha = generateAndSaveCaptcha();
            result.put("code", 1);
            result.put("msg", "密码错误");
            result.put("captcha", newCaptcha);
            return result;
        }
        if (alumni.getState() == null || alumni.getState() != 1) {
            result.put("code", 2);
            result.put("msg", "账号未激活，请在首页点击'获取校友AlumniID'或等待管理员审核");
            result.put("data", null);
            return result;
        }
        // 检查微信关注状态
        try {
            java.util.List<String> allOpenids = weChatUtil.getAllOpenids();
            String userOpenid = alumni.getOpenid();
            if (userOpenid == null || userOpenid.isEmpty() || !allOpenids.contains(userOpenid)) {
                result.put("code", 3);
                result.put("msg", "请先关注微信服务号");
                result.put("data", null);
                return result;
            }
        } catch (Exception e) {
            System.err.println("微信API调用失败: " + e.getMessage());
            e.printStackTrace();
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("alumni_id", alumniId);
        claims.put("chineseName", alumni.getChineseName());
        claims.put("affiliation", alumni.getAffiliation());
        String role = alumni.getRole() == null || alumni.getRole().isEmpty() ? "alumni" : alumni.getRole();
        claims.put("role", role);
        long expirationInSeconds = 60 * 60 * 12; // 12小时
        String token = JwtUtil.generateToken(claims, expirationInSeconds);
        ResponseCookie cookie = ResponseCookie.from("alumni-token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(expirationInSeconds)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("chineseName", alumni.getChineseName());
        data.put("affiliation", alumni.getAffiliation());
        data.put("role", role);
        data.put("alumni_id", alumniId);
        data.put("token", token);
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", data);
        // 单点登录：踢掉旧token
        String ssoKey = "sso:" + alumniId;
        String oldToken = (String) redisTemplate.opsForValue().get(ssoKey);
        if (oldToken != null) {
            redisTemplate.delete("token:" + oldToken);
        }
        // 保存新token
        redisTemplate.opsForValue().set(ssoKey, token, expirationInSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("token:" + token, alumniId, expirationInSeconds, TimeUnit.SECONDS);
        return result;
    }

    // 生成新验证码并写入redis，返回base64图片和uuid
    private CaptchaResult generateAndSaveCaptcha() {
        String uuid = UUID.randomUUID().toString();
        CircleCaptcha circleCaptcha = new CircleCaptcha(120, 40, 6, 20);
        // 使用自定义CodeGenerator生成6位纯数字验证码
        circleCaptcha.setGenerator(new CodeGenerator() {
            @Override
            public String generate() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    sb.append((int)(Math.random() * 10));
                }
                return sb.toString();
            }
            @Override
            public boolean verify(String code, String userInputCode) {
                return code.equals(userInputCode);
            }
        });
        circleCaptcha.createCode();
        String code = circleCaptcha.getCode();
        redisTemplate.opsForValue().set("captcha:" + uuid, code, 60, TimeUnit.SECONDS);
        String base64Img = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(circleCaptcha.getImage(), "png", baos);
            base64Img = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            base64Img = "";
        }
        return new CaptchaResult(uuid, base64Img);
    }

    // 验证码返回对象
    public static class CaptchaResult {
        public String uuid;
        public String img;
        public CaptchaResult(String uuid, String img) {
            this.uuid = uuid;
            this.img = img;
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 从cookie或header获取token
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("alumni-token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        // 2. 删除redis中的token和sso:alumniId
        if (token != null) {
            String alumniId = (String) redisTemplate.opsForValue().get("token:" + token);
            redisTemplate.delete("token:" + token);
            if (alumniId != null) {
                redisTemplate.delete("sso:" + alumniId);
            }
        }
        // 3. 清除cookie
        ResponseCookie cookie = ResponseCookie.from("alumni-token", "")
                .httpOnly(true)
                .secure(true) // 在生产环境中应为true
                .path("/")
                .maxAge(0) //立即过期
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @PutMapping("/password")
    public Map<String, Object> changePassword(@RequestAttribute("Authorization") String token, @RequestBody Map<String, String> request) {
        String pastPassword = request.get("pastPassword");
        String newPassword = request.get("newPassword");
        return registerService.changePassword(token, pastPassword, newPassword);
    }

    // 忘记密码：邮箱校验并发验证码，返回token（token只含邮箱）
    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String email = request.get("email");
        Alumni alumni = registerMapper.findByEmail(email);
        if (alumni == null) {
            result.put("code", 1);
            result.put("msg", "邮箱不存在");
            result.put("data", null);
            return result;
        }
        // 生成只含邮箱的token，有效期10分钟
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        String token = JwtUtil.generateToken(claims, 600);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        result.put("code", 0);
        result.put("msg", "验证码已发送（测试用：123456）");
        result.put("data", data);
        return result;
    }

    // 忘记密码：验证码校验（静态123456）
    @PutMapping("/forgot-password-verify")
    public Map<String, Object> forgotPasswordVerify(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        // 从Authorization头中提取token
        String token = authHeader.replace("Bearer ", "");
        
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 1);
            result.put("msg", "token无效或已过期");
            result.put("data", null);
            return result;
        }
        String verifyCode = request.get("verifyCode");
        if (!"123456".equals(verifyCode)) {
            result.put("code", 1);
            result.put("msg", "验证码错误");
            result.put("data", "验证码错误");
            return result;
        }
        result.put("code", 0);
        result.put("msg", "验证成功");
        result.put("data", null);
        return result;
    }

    // 忘记密码：重置密码
    @PutMapping("/forgot-password-put")
    public Map<String, Object> forgotPasswordPut(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        
        if (email == null || email.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "邮箱不能为空");
            result.put("data", null);
            return result;
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "新密码不能为空");
            result.put("data", null);
            return result;
        }
        
        Alumni alumni = registerMapper.findByEmail(email);
        if (alumni == null) {
            result.put("code", 1);
            result.put("msg", "用户不存在");
            result.put("data", null);
            return result;
        }
        
        int updated = registerMapper.updatePassword(email, newPassword);
        if (updated > 0) {
            result.put("code", 0);
            result.put("msg", "重置成功");
            result.put("data", null);
        } else {
            result.put("code", 1);
            result.put("msg", "重置失败");
            result.put("data", null);
        }
        return result;
    }

    // 微信关注验证接口
    @PostMapping("/wechat/verify-follow")
    public Map<String, Object> verifyWeChatFollow(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String alumniId = request.get("alumni_id");
        
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "校友ID不能为空");
            result.put("data", null);
            return result;
        }
        
        try {
            Alumni alumni = registerMapper.findByAlumniId(alumniId);
            if (alumni == null) {
                result.put("code", 1);
                result.put("msg", "用户不存在");
                result.put("data", null);
                return result;
            }
            
            java.util.List<String> allOpenids = weChatUtil.getAllOpenids();
            String userOpenid = alumni.getOpenid();
            
            if (userOpenid != null && !userOpenid.isEmpty() && allOpenids.contains(userOpenid)) {
                result.put("code", 0);
                result.put("msg", "已关注微信服务号");
                result.put("data", Map.of("followed", true));
            } else {
                result.put("code", 0);
                result.put("msg", "未关注微信服务号");
                result.put("data", Map.of("followed", false));
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "验证失败: " + e.getMessage());
            result.put("data", null);
        }
        
        return result;
    }

    // 二维码生成接口，直接返回微信二维码图片URL
    @GetMapping("/wechat/qrcode/{alumniId}")
    public Map<String, Object> getQRCodeUrl(@PathVariable String alumniId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证校友ID是否存在
            Alumni alumni = registerMapper.findByAlumniId(alumniId);
            if (alumni == null) {
                result.put("code", 1);
                result.put("msg", "用户不存在");
                result.put("data", null);
                return result;
            }
            // 生成微信临时二维码URL
            String qrCodeUrl = weChatUtil.generateTemporaryQRCode(alumniId);
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", Map.of("qrCodeUrl", qrCodeUrl));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "生成二维码失败: " + e.getMessage());
            result.put("data", null);
            return result;
        }
    }

    // 更新openid接口
    @PostMapping("/wechat/update-openid")
    public Map<String, Object> updateOpenid(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String alumniId = request.get("alumni_id");
        String openid = request.get("openid");
        
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "校友ID不能为空");
            result.put("data", null);
            return result;
        }
        
        if (openid == null || openid.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "openid不能为空");
            result.put("data", null);
            return result;
        }
        
        try {
            int updated = registerMapper.updateOpenid(alumniId, openid);
            if (updated > 0) {
                result.put("code", 0);
                result.put("msg", "更新成功");
                result.put("data", null);
            } else {
                result.put("code", 1);
                result.put("msg", "更新失败，用户可能不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "更新失败: " + e.getMessage());
            result.put("data", null);
        }
        
        return result;
    }

    @GetMapping("/alumni-list")
    public Map<String, Object> getAlumniList(
            @RequestParam(value = "alumniID", required = false) String alumniId,
            @RequestParam(value = "ycyw_schools_attended", required = false) String ycywSchoolsAttended,
            @RequestParam(value = "chinese_name", required = false) String chineseName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contact_number", required = false) String contactNumber,
            @RequestParam(value = "tagIds", required = false) String tagIds,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "permissionLevel", required = false, defaultValue = "0") Integer permissionLevel,
            @RequestParam(value = "accessibleSchools", required = false) String accessibleSchools
    ) {
        // 使用前端传来的权限信息
        Map<String, Object> adminPermissionInfo = new HashMap<>();
        
        // 确保值不为null
        permissionLevel = (permissionLevel != null) ? permissionLevel : 0;
        accessibleSchools = (accessibleSchools != null) ? accessibleSchools : "";
        
        adminPermissionInfo.put("permissionLevel", permissionLevel);
        adminPermissionInfo.put("accessibleSchools", accessibleSchools);
        adminPermissionInfo.put("permissionSchool", accessibleSchools);
        
        // 调用service层的方法，传入管理员权限信息
        // 兼容新参数，默认不筛选state
        return registerService.getAlumniList(alumniId, ycywSchoolsAttended, chineseName, email, contactNumber, tagIds, null, page, pageSize, adminPermissionInfo);
    }

    @DeleteMapping("/alumni-list")
    public Map<String, Object> deleteAlumni(@RequestBody Map<String, java.util.List<String>> request) {
        java.util.List<String> alumniIds = request.get("alumniIds");
        int success = 0;
        for (String id : alumniIds) {
            // 先删学籍信息
            registerMapper.deleteTertiaryByAlumniId(id);
            // 再删工作信息
            registerMapper.deleteCareerByAlumniId(id);
            // 最后删基础表
            success += registerMapper.deleteAlumniById(id);
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

    @PostMapping("/alumni-add")
    public Map<String, Object> addAlumni(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查必填字段
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            if (email == null || email.isEmpty()) {
                result.put("code", 1);
                result.put("msg", "邮箱为必填项");
                return result;
            }
            if (password == null || password.isEmpty()) {
                result.put("code", 1);
                result.put("msg", "密码为必填项");
                return result;
            }
            // 检查邮箱是否已存在
            Alumni existingAlumni = registerMapper.findByEmail(email);
            if (existingAlumni != null) {
                result.put("code", 1);
                result.put("msg", "该邮箱已被注册");
                return result;
            }
            // 生成唯一学号
            String generatedAlumniId = request.get("alumniId") != null ? (String) request.get("alumniId") : registerService.generateUniqueAlumniId();
            // 创建校友对象
            Alumni alumni = new Alumni();
            alumni.setAlumniId(generatedAlumniId);
            alumni.setEmail(email);
            alumni.setPassword(DigestUtils.md5DigestAsHex(password.getBytes())); // 加密存储
            alumni.setState(1); // 管理员添加校友时，激活状态为1
            alumni.setUpdateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            // 其余字段赋值
            if (request.get("salutation") != null) alumni.setSalutation((String) request.get("salutation"));
            if (request.get("title") != null) alumni.setSalutation((String) request.get("title"));
            if (request.get("firstName") != null) alumni.setFirstName((String) request.get("firstName"));
            if (request.get("lastName") != null) alumni.setLastName((String) request.get("lastName"));
            if (request.get("chineseName") != null) alumni.setChineseName((String) request.get("chineseName"));
            if (request.get("contactNumber") != null) alumni.setContactNumber((String) request.get("contactNumber"));
            if (request.get("phone") != null) alumni.setContactNumber((String) request.get("phone"));
            if (request.get("wechatId") != null) alumni.setWechatId((String) request.get("wechatId"));
            if (request.get("correspondenceAddress") != null) alumni.setCorrespondenceAddress((String) request.get("correspondenceAddress"));
            if (request.get("address") != null) alumni.setCorrespondenceAddress((String) request.get("address"));
            if (request.get("currentLocation") != null) alumni.setCurrentLocation((String) request.get("currentLocation"));
            if (request.get("zohoAlumniNumber") != null) alumni.setZohoAlumniNumber((String) request.get("zohoAlumniNumber"));
            if (request.get("zohoId") != null) alumni.setZohoAlumniNumber((String) request.get("zohoId"));
            if (request.get("affiliation") != null) alumni.setAffiliation((String) request.get("affiliation"));
            if (request.get("identity") != null) alumni.setAffiliation((String) request.get("identity"));
            if (request.get("ycywSchoolsAttended") != null) alumni.setYcywSchoolsAttended((String) request.get("ycywSchoolsAttended"));
            if (request.get("school") != null) alumni.setYcywSchoolsAttended((String) request.get("school"));
            if (request.get("studyPeriodStart") != null) alumni.setStudyPeriodStart(normalizeDate(request.get("studyPeriodStart")));
            if (request.get("studyStart") != null) alumni.setStudyPeriodStart(normalizeDate(request.get("studyStart")));
            if (request.get("studyPeriodEnd") != null) alumni.setStudyPeriodEnd(normalizeDate(request.get("studyPeriodEnd")));
            if (request.get("studyEnd") != null) alumni.setStudyPeriodEnd(normalizeDate(request.get("studyEnd")));
            if (request.get("lastSchoolDay") != null) alumni.setLastSchoolDay(normalizeDate(request.get("lastSchoolDay")));
            if (request.get("lastDate") != null) alumni.setLastSchoolDay(normalizeDate(request.get("lastDate")));
            if (request.get("yearLeft") != null) {
                try { alumni.setYearLeft(Integer.valueOf(request.get("yearLeft").toString())); } catch (Exception ignore) {}
            }
            if (request.get("leaveYear") != null && (alumni.getYearLeft() == null)) {
                try { alumni.setYearLeft(Integer.valueOf(request.get("leaveYear").toString())); } catch (Exception ignore) {}
            }
            if (request.get("maritalStatus") != null) alumni.setMaritalStatus((String) request.get("maritalStatus"));
            if (request.get("showYearLeft") != null) alumni.setShowYearLeft(Boolean.valueOf(request.get("showYearLeft").toString()));
            if (request.get("showTertiaryUniversity") != null) alumni.setShowTertiaryUniversity(Boolean.valueOf(request.get("showTertiaryUniversity").toString()));
            if (request.get("showTertiaryMajor") != null) alumni.setShowTertiaryMajor(Boolean.valueOf(request.get("showTertiaryMajor").toString()));
            if (request.get("showCareerCompany") != null) alumni.setShowCareerCompany(Boolean.valueOf(request.get("showCareerCompany").toString()));
            if (request.get("showJobTitle") != null) alumni.setShowJobTitle(Boolean.valueOf(request.get("showJobTitle").toString()));
            if (request.get("showIndustry") != null) alumni.setShowIndustry(Boolean.valueOf(request.get("showIndustry").toString()));
            if (request.get("showCountry") != null) alumni.setShowCountry(Boolean.valueOf(request.get("showCountry").toString()));
            if (request.get("region") != null) alumni.setRegion((String) request.get("region"));
            if (request.get("birthday") != null) alumni.setBirthday(normalizeDate(request.get("birthday")));
            if (request.get("tagIds") != null) alumni.setTagIds((String) request.get("tagIds"));
            if (request.get("role") != null) alumni.setRole((String) request.get("role"));
            // 插入主表
            int inserted = registerMapper.insertAlumni(alumni);
            // 处理教育经历
            if (request.get("tertiary_information") instanceof java.util.List) {
                java.util.List<Map<String, Object>> tertiaryList = (java.util.List<Map<String, Object>>) request.get("tertiary_information");
                for (Map<String, Object> t : tertiaryList) {
                    if (t == null) continue;
                    com.alumni.back.Entity.TertiaryInformation info = new com.alumni.back.Entity.TertiaryInformation();
                    info.setId(java.util.UUID.randomUUID().toString());
                    info.setAlumniId(generatedAlumniId);
                    info.setUniversityCollege((String) t.get("university_college"));
                    info.setDegree((String) t.get("degree"));
                    info.setMajor((String) t.get("major"));
                    if (t.get("graduation_year") != null) {
                        try { info.setGraduationYear(Integer.valueOf(t.get("graduation_year").toString())); } catch (Exception ignore) {}
                    }
                    info.setCountryRegion((String) t.get("country_region"));
                    registerMapper.insertTertiaryInfo(info);
                }
            }
            // 处理工作经历
            if (request.get("carrer_information") instanceof java.util.List) {
                java.util.List<Map<String, Object>> careerList = (java.util.List<Map<String, Object>>) request.get("carrer_information");
                for (Map<String, Object> c : careerList) {
                    if (c == null) continue;
                    com.alumni.back.Entity.CareerInformation info = new com.alumni.back.Entity.CareerInformation();
                    info.setId(java.util.UUID.randomUUID().toString());
                    info.setAlumniId(generatedAlumniId);
                    info.setCompanyOrganization((String) c.get("company_organization"));
                    info.setJobTitle((String) c.get("job_title"));
                    info.setIndustry(c.get("industry") != null ? com.alumni.back.Entity.CareerInformation.Industry.valueOf((String) c.get("industry")) : null);
                    info.setCountryRegion((String) c.get("country_region"));
                    registerMapper.insertCareerInfo(info);
                }
            }
            if (inserted > 0) {
                result.put("code", 0);
                result.put("msg", "添加成功");
                result.put("data", generatedAlumniId); // 返回生成的学号
            } else {
                result.put("code", 1);
                result.put("msg", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "添加失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/check-auth")
    public Map<String, Object> checkAuth(@RequestAttribute(name = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        if (token == null || !JwtUtil.isTokenValid(token)) {
            result.put("code", 401);
            result.put("msg", "未登录或token无效");
            return result;
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            String alumniId = claims.get("alumni_id", String.class);
            String role = claims.get("role", String.class);
            String chineseName = claims.get("chineseName", String.class);
            String affiliation = claims.get("affiliation", String.class);

            Map<String, Object> data = new HashMap<>();
            data.put("alumniId", alumniId);
            data.put("role", role);
            data.put("chineseName", chineseName);
            data.put("affiliation", affiliation);

            result.put("code", 0);
            result.put("msg", "已登录");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "Token解析失败");
        }
        return result;
    }

    @GetMapping("/alumni/me")
    public Alumni getCurrentAlumni(@RequestAttribute("Authorization") String token) {
        Claims claims = JwtUtil.parseToken(token);
        String alumniId = String.valueOf(claims.get("alumni_id"));
        return registerMapper.findByAlumniId(alumniId);
    }

    private String normalizeDate(Object dateObj) {
        if (dateObj == null) return null;
        String dateStr = dateObj.toString();
        if (dateStr.trim().isEmpty()) return null;
        // yyyy/MM or yyyy/M to yyyy-MM-01
        if (dateStr.matches("^\\d{4}/\\d{1,2}$")) {
            String[] parts = dateStr.split("/");
            return String.format("%s-%02d-01", parts[0], Integer.parseInt(parts[1]));
        }
        // yyyy/MM/dd or yyyy/M/d to yyyy-MM-dd
        if (dateStr.matches("^\\d{4}/\\d{1,2}/\\d{1,2}$")) {
            String[] parts = dateStr.split("/");
            return String.format("%s-%02d-%02d", parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        // 已经是 yyyy-MM-dd
        if (dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) return dateStr;
        return dateStr;
    }


} 