package com.alumni.back.register;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.activity.ActivityRegistrationMapper;
import com.alumni.back.chapter.ChapterApplicationMapper;
import com.alumni.back.util.JwtUtil;
import com.alumni.back.visitrecord.VisitRecordMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.util.DigestUtils;
import com.alumni.back.Entity.TertiaryInformation;
import com.alumni.back.Entity.CareerInformation;
import java.util.List;
import java.util.UUID;
import com.alumni.back.util.DatabaseWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.ArrayList;
import com.alumni.back.dto.MatchGmiRequest;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private RegisterMapper registerMapper;
    @Autowired
    private ActivityRegistrationMapper activityRegistrationMapper;
    @Autowired
    private ChapterApplicationMapper chapterApplicationMapper;
    @Autowired
    private VisitRecordMapper visitRecordMapper;
    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;

    // 临时保存token->password的Map（开发测试用，生产建议用Redis等）
    private static final Map<String, String> tempPasswordMap = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void init() {
        // 启动定时清理任务，每5分钟清理一次过期的token
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokensInternal, 5, 5, TimeUnit.MINUTES);
    }
    
    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }
    
    // 清理过期的token（内部方法）
    private void cleanExpiredTokensInternal() {
        try {
            Iterator<Entry<String, String>> iterator = tempPasswordMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                String token = entry.getKey();
                try {
                    // 检查token是否过期
                    if (!JwtUtil.isTokenValid(token)) {
                        iterator.remove();
                    }
                } catch (Exception e) {
                    // token解析失败，也清理掉
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            System.err.println("清理过期token时发生错误: " + e.getMessage());
        }
    }
    
    // 手动清理过期token（管理员接口）
    @Override
    public Map<String, Object> cleanExpiredTokens() {
        Map<String, Object> result = new HashMap<>();
        try {
            int beforeSize = tempPasswordMap.size();
            cleanExpiredTokensInternal();
            int afterSize = tempPasswordMap.size();
            int cleanedCount = beforeSize - afterSize;
            
            result.put("code", 0);
            result.put("msg", "清理完成");
            result.put("data", Map.of(
                "cleaned_count", cleanedCount,
                "remaining_count", afterSize
            ));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "清理失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @Override
    public Alumni findByEmail(String email) {
        return registerMapper.findByEmail(email);
    }

    @Override
    public Alumni findByAlumniId(String alumniId) {
        return registerMapper.findByAlumniId(alumniId);
    }

    @Override
    public boolean isEmailAllowed(String email) {
        return registerMapper.countEmailExists(email) == 0;
    }

    @Override
    public Map<String, Object> verifyEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        // 1. 先判断邮箱是否已被注册
        if (!isEmailAllowed(email)) {
            result.put("code", 1);
            result.put("msg", "该邮箱已被注册");
            result.put("data", null);
            return result;
        }
        // 2. 判断是否已注册（这个检查现在可以移除，因为上面的检查已经覆盖了）
        // Alumni alumni = registerMapper.findByEmail(email);
        // if (alumni != null && alumni.getPassword() != null && !alumni.getPassword().isEmpty()) {
        //     result.put("code", 1);
        //     result.put("msg", "该邮箱已被注册");
        //     result.put("data", null);
        //     return result;
        // }
        // 3. 生成临时token
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        String token = JwtUtil.generateToken(claims, 600); // 10分钟
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expires_in", 600);
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> verifyCode(String token, String verifyCode) {
        Map<String, Object> result = new HashMap<>();
        // 校验token
        if (!JwtUtil.isTokenValid(token)) {
            result.put("code", 1002);
            result.put("msg", "jwt解析失败");
            result.put("data", null);
            return result;
        }
        // 静态验证码校验
        if (!"123456".equals(verifyCode)) {
            result.put("code", 1);
            result.put("msg", "验证码错误");
            result.put("data", "验证码错误");
            return result;
        }
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", null);
        return result;
    }

    @Override
    public Map<String, Object> setPassword(String token, String password) {
        Map<String, Object> result = new HashMap<>();
        // 统一去掉 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 1002);
                result.put("msg", "jwt解析失败或已过期");
                result.put("data", null);
                return result;
            }
            // 密码加密（MD5）
            String encryptedPwd = DigestUtils.md5DigestAsHex(password.getBytes());
            // 临时保存token->加密密码
            tempPasswordMap.put(token, encryptedPwd);
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 1002);
            result.put("msg", "jwt解析失败或已过期");
            result.put("data", null);
        }
        return result;
    }

    @Override
    public Map<String, Object> setAlumniInfo(String token, Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        // 统一去掉 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 1003);
                result.put("msg", "jwt已过期或无效");
                result.put("data", null);
                return result;
            }
            // 解析alumni主信息
            Map<String, Object> alumniMap;
            // 判断数据结构：编辑流程使用嵌套的alumni对象，注册流程直接使用request
            if (request.containsKey("alumni")) {
                alumniMap = (Map<String, Object>) request.get("alumni"); // 编辑流程
            } else {
                alumniMap = request; // 注册流程
            }
            Alumni alumni = new Alumni();
            
            // 兼容两种字段名：注册流程和编辑流程
            alumni.setAlumniId((String) (alumniMap.get("alumni_id") != null ? alumniMap.get("alumni_id") : alumniMap.get("alumniId")));
            alumni.setSalutation((String) (alumniMap.get("salutation") != null ? alumniMap.get("salutation") : alumniMap.get("title")));
            alumni.setFirstName((String) (alumniMap.get("first_name") != null ? alumniMap.get("first_name") : alumniMap.get("firstName")));
            alumni.setLastName((String) (alumniMap.get("last_name") != null ? alumniMap.get("last_name") : alumniMap.get("lastName")));
            alumni.setChineseName((String) (alumniMap.get("chinese_name") != null ? alumniMap.get("chinese_name") : alumniMap.get("chineseName")));
            alumni.setEmail((String) alumniMap.get("email"));
            alumni.setContactNumber((String) (alumniMap.get("contact_number") != null ? alumniMap.get("contact_number") : alumniMap.get("phone")));
            alumni.setWechatId((String) (alumniMap.get("wechat_id") != null ? alumniMap.get("wechat_id") : alumniMap.get("wechatId")));
            alumni.setCorrespondenceAddress((String) (alumniMap.get("correspondence_address") != null ? alumniMap.get("correspondence_address") : alumniMap.get("address")));
            alumni.setCurrentLocation((String) (alumniMap.get("current_location") != null ? alumniMap.get("current_location") : alumniMap.get("currentLocation")));
            alumni.setZohoAlumniNumber((String) (alumniMap.get("zoho_alumni_number") != null ? alumniMap.get("zoho_alumni_number") : alumniMap.get("zohoId")));
            alumni.setAffiliation((String) (alumniMap.get("affiliation") != null ? alumniMap.get("affiliation") : alumniMap.get("identity")));
            alumni.setYcywSchoolsAttended((String) (alumniMap.get("ycyw_schools_attended") != null ? alumniMap.get("ycyw_schools_attended") : alumniMap.get("school")));
            alumni.setStudyPeriodStart(normalizeDate(alumniMap.get("study_period_start") != null ? alumniMap.get("study_period_start") : alumniMap.get("studyStart")));
            alumni.setStudyPeriodEnd(normalizeDate(alumniMap.get("study_period_end") != null ? alumniMap.get("study_period_end") : alumniMap.get("studyEnd")));
            alumni.setLastSchoolDay(normalizeDate(alumniMap.get("last_school_day") != null ? alumniMap.get("last_school_day") : alumniMap.get("lastDate")));
            alumni.setYearLeft(alumniMap.get("year_left") != null ? Integer.valueOf(alumniMap.get("year_left").toString()) : 
                              (alumniMap.get("leaveYear") != null && !alumniMap.get("leaveYear").toString().isEmpty() ? Integer.valueOf(alumniMap.get("leaveYear").toString()) : null));
            alumni.setMaritalStatus((String) (alumniMap.get("marital_status") != null ? alumniMap.get("marital_status") : alumniMap.get("maritalStatus")));
            alumni.setShowYearLeft(alumniMap.get("show_year_left") != null ? Boolean.valueOf(alumniMap.get("show_year_left").toString()) : 
                                 (alumniMap.get("showYearLeft") != null ? Boolean.valueOf(alumniMap.get("showYearLeft").toString()) : false));
            alumni.setShowTertiaryUniversity(alumniMap.get("show_tertiary_university") != null ? Boolean.valueOf(alumniMap.get("show_tertiary_university").toString()) : 
                                           (alumniMap.get("showTertiaryUniversity") != null ? Boolean.valueOf(alumniMap.get("showTertiaryUniversity").toString()) : false));
            alumni.setShowTertiaryMajor(alumniMap.get("show_tertiary_major") != null ? Boolean.valueOf(alumniMap.get("show_tertiary_major").toString()) : 
                                      (alumniMap.get("showTertiaryMajor") != null ? Boolean.valueOf(alumniMap.get("showTertiaryMajor").toString()) : false));
            alumni.setShowCareerCompany(alumniMap.get("show_career_company") != null ? Boolean.valueOf(alumniMap.get("show_career_company").toString()) : 
                                      (alumniMap.get("showCareerCompany") != null ? Boolean.valueOf(alumniMap.get("showCareerCompany").toString()) : false));
            alumni.setShowJobTitle(alumniMap.get("show_job_title") != null ? Boolean.valueOf(alumniMap.get("show_job_title").toString()) : 
                                 (alumniMap.get("showJobTitle") != null ? Boolean.valueOf(alumniMap.get("showJobTitle").toString()) : false));
            alumni.setShowIndustry(alumniMap.get("show_industry") != null ? Boolean.valueOf(alumniMap.get("show_industry").toString()) : 
                                 (alumniMap.get("showIndustry") != null ? Boolean.valueOf(alumniMap.get("showIndustry").toString()) : false));
            alumni.setShowCountry(alumniMap.get("show_country") != null ? Boolean.valueOf(alumniMap.get("show_country").toString()) : 
                                (alumniMap.get("showCountry") != null ? Boolean.valueOf(alumniMap.get("showCountry").toString()) : false));
            alumni.setRegion((String) alumniMap.get("region"));
            alumni.setBirthday((String) (alumniMap.get("birthday") != null ? alumniMap.get("birthday") : ""));
            
            // 判断是否为注册流程还是编辑流程
            boolean isRegistration = !request.containsKey("alumni");
            if (isRegistration) {
                alumni.setState(2); // 注册流程设置状态为2（注册待审核）
            } else if (alumniMap.get("state") != null) {
                alumni.setState(Integer.valueOf(alumniMap.get("state").toString())); // 编辑流程使用传入的状态
            }
            
            alumni.setTagIds((String) alumniMap.get("tag_ids"));
            alumni.setRole((String) alumniMap.get("role"));
            alumni.setPassword((String) alumniMap.get("password"));
            
            // 先查数据库原有alumnus记录，role只要不是alumni就强制保留
            Alumni dbAlumni = registerMapper.findByAlumniId(alumni.getAlumniId());
            if (dbAlumni != null && dbAlumni.getRole() != null && !"alumni".equalsIgnoreCase(dbAlumni.getRole())) {
                alumni.setRole(dbAlumni.getRole());
            } else if (alumniMap.get("role") != null && !((String) alumniMap.get("role")).isEmpty()) {
                alumni.setRole((String) alumniMap.get("role"));
            } else {
                alumni.setRole("alumni"); // 默认角色
            }
            // 处理多标签字段（仅管理员端允许写入）
            boolean allowTagEdit = false;
            try {
                String roleInToken = (String) JwtUtil.parseToken(token).get("role");
                if (roleInToken != null && (roleInToken.equals("Admin") || roleInToken.equals("SuperAdmin"))) {
                    allowTagEdit = true;
                }
            } catch (Exception ignore) {}
            if (allowTagEdit && alumniMap.get("tagIds") != null) {
                alumni.setTagIds((String) alumniMap.get("tagIds"));
            }
            
            // 密码处理：先检查是否为校友自己更新信息
            boolean isUpdateExistingInfo = false;
            Alumni existingAlumni = registerMapper.findByAlumniId(alumni.getAlumniId());
            if (existingAlumni != null && existingAlumni.getPassword() != null && !existingAlumni.getPassword().isEmpty()) {
                isUpdateExistingInfo = true;
            }
            
            if (isUpdateExistingInfo) {
                // 更新现有校友信息，不设置密码字段，保持原密码
                // 不调用 setPassword，让 SQL 中的条件判断生效
            } else {
                // 新校友注册，从临时存储中恢复密码
                String encryptedPwd = tempPasswordMap.get(token);
                if (encryptedPwd == null || encryptedPwd.isEmpty()) {
                    result.put("code", 1004);
                    result.put("msg", "密码信息丢失，请返回上一步重新设置密码");
                    result.put("data", null);
                    return result;
                }
                alumni.setPassword(encryptedPwd);
            }
            
            alumni.setUpdateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            // 校验关键字段
            if (alumni.getAlumniId() == null || alumni.getAlumniId().isEmpty() || alumni.getEmail() == null || alumni.getEmail().isEmpty()) {
                result.put("code", 1004);
                result.put("msg", "学号和邮箱为必填项，不能为空");
                result.put("data", null);
                return result;
            }
            
            // 先插入或更新主表记录
            int updated = registerMapper.updateAlumniById(alumni);
            if (updated == 0) {
                // 如果更新失败，尝试插入新记录
                try {
                    registerMapper.insertAlumni(alumni);
                } catch (Exception e) {
                    result.put("code", 1);
                    result.put("msg", "信息保存失败");
                    result.put("data", null);
                    return result;
                }
            }
            
            // 处理前端传来的待删除教育/工作信息id
            List<String> deleteTertiaryIds = (List<String>) request.get("delete_tertiary_ids");
            if (deleteTertiaryIds != null) {
                for (String id : deleteTertiaryIds) {
                    registerMapper.deleteTertiaryById(id);
                }
            }
            List<String> deleteCareerIds = (List<String>) request.get("delete_career_ids");
            if (deleteCareerIds != null) {
                for (String id : deleteCareerIds) {
                    registerMapper.deleteCareerById(id);
                }
            }
            // 先删除该校友所有教育和工作信息，避免主键冲突
            registerMapper.deleteTertiaryByAlumniId(alumni.getAlumniId());
            registerMapper.deleteCareerByAlumniId(alumni.getAlumniId());
            
            // 教育信息
            List<Map<String, Object>> tertiaryList = (List<Map<String, Object>>) request.get("tertiary_information");
            if (tertiaryList != null) {
                for (Map<String, Object> t : tertiaryList) {
                    // 关键字段全为空则跳过
                    if (
                        (t.get("university_college") == null || ((String)t.get("university_college")).trim().isEmpty()) &&
                        (t.get("degree") == null || ((String)t.get("degree")).trim().isEmpty()) &&
                        (t.get("major") == null || ((String)t.get("major")).trim().isEmpty()) &&
                        (t.get("graduation_year") == null || t.get("graduation_year").toString().trim().isEmpty()) &&
                        (t.get("country_region") == null || ((String)t.get("country_region")).trim().isEmpty())
                    ) {
                        continue;
                    }
                    TertiaryInformation info = new TertiaryInformation();
                    info.setId(UUID.randomUUID().toString()); // 一律用新UUID
                    info.setAlumniId(alumni.getAlumniId());
                    info.setUniversityCollege((String) t.get("university_college"));
                    info.setDegree((String) t.get("degree"));
                    info.setMajor((String) t.get("major"));
                    info.setGraduationYear(t.get("graduation_year") == null ? null : Integer.valueOf(t.get("graduation_year").toString()));
                    info.setCountryRegion((String) t.get("country_region"));
                    registerMapper.insertTertiaryInfo(info);
                }
            }
            // 工作信息
            List<Map<String, Object>> careerList = (List<Map<String, Object>>) request.get("carrer_information");
            if (careerList != null) {
                for (Map<String, Object> c : careerList) {
                    if (
                        (c.get("company_organization") == null || ((String)c.get("company_organization")).trim().isEmpty()) &&
                        (c.get("job_title") == null || ((String)c.get("job_title")).trim().isEmpty()) &&
                        (c.get("industry") == null || ((String)c.get("industry")).trim().isEmpty()) &&
                        (c.get("country_region") == null || ((String)c.get("country_region")).trim().isEmpty())
                    ) {
                        continue;
                    }
                    CareerInformation info = new CareerInformation();
                    info.setId(UUID.randomUUID().toString()); // 一律用新UUID
                    info.setAlumniId(alumni.getAlumniId());
                    info.setCompanyOrganization((String) c.get("company_organization"));
                    info.setJobTitle((String) c.get("job_title"));
                    info.setIndustry(CareerInformation.Industry.valueOf((String) c.get("industry")));
                    info.setCountryRegion((String) c.get("country_region"));
                    registerMapper.insertCareerInfo(info);
                }
            }
            
            result.put("code", 0);
            result.put("msg", "success");
            // 判断是否本人自助更新
            String tokenAlumniId = String.valueOf(JwtUtil.parseToken(token).get("alumni_id"));
            String updateAlumniId = String.valueOf(alumni.getAlumniId());
            if (tokenAlumniId != null && tokenAlumniId.equals(updateAlumniId)) {
                // 生成新token
                Map<String, Object> claims = new HashMap<>();
                claims.put("alumni_id", alumni.getAlumniId());
                claims.put("chineseName", alumni.getChineseName());
                claims.put("affiliation", alumni.getAffiliation());
                claims.put("role", alumni.getRole());
                String newToken = JwtUtil.generateToken(claims, 60 * 60 * 12);
                Map<String, Object> data = new HashMap<>();
                data.put("token", newToken);
                data.put("chineseName", alumni.getChineseName());
                data.put("affiliation", alumni.getAffiliation());
                data.put("role", alumni.getRole());
                data.put("alumni_id", alumni.getAlumniId());
                result.put("data", data);
            } else {
                result.put("data", null);
            }
            tempPasswordMap.remove(token);
            databaseWebSocketHandler.sendUpdateMessage("alumni changed");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "信息保存异常");
            result.put("data", null);
        }
        return result;
    }

    @Override
    public Map<String, Object> changePassword(String token, String pastPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!JwtUtil.isTokenValid(token)) {
                result.put("code", 3);
                result.put("msg", "未登录或 token 无效");
                result.put("data", null);
                return result;
            }
            String alumniId = JwtUtil.parseToken(token).get("alumni_id", String.class);
            Alumni alumni = registerMapper.findByAlumniId(alumniId);
            if (alumni == null) {
                result.put("code", 2);
                result.put("msg", "用户不存在");
                result.put("data", null);
                return result;
            }
            // 直接比对加密串
            if (!pastPassword.equals(alumni.getPassword())) {
                result.put("code", 1);
                result.put("msg", "原密码错误");
                result.put("data", "原密码错误");
                return result;
            }
            // 新密码仍需加密存储
            String encryptedNewPwd = newPassword;
            int updated = registerMapper.updatePasswordByAlumniId(alumniId, encryptedNewPwd);
            if (updated > 0) {
                result.put("code", 0);
                result.put("msg", "操作成功");
                result.put("data", null);
                databaseWebSocketHandler.sendUpdateMessage("alumni changed");
            } else {
                result.put("code", 1);
                result.put("msg", "修改失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 3);
            result.put("msg", "未登录或 token 无效");
            result.put("data", null);
        }
        return result;
    }

    @Override
    public Map<String, Object> getAlumniList(String alumniId, String ycywSchoolsAttended, String chineseName, String email, String contactNumber, String tagIds, java.util.List<Integer> stateList, int page, int pageSize, Map<String, Object> adminPermissionInfo) {
        int offset = (page - 1) * pageSize;
        java.util.List<String> tagIdsList = new java.util.ArrayList<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            for (String tid : tagIds.split(",")) {
                if (!tid.trim().isEmpty()) tagIdsList.add(tid.trim());
            }
        }
        
        // 处理管理员权限信息
        Integer permissionLevel = 0; // 默认为0（无权限）
        String accessibleSchools = "";
        String permissionSchool = ""; // 添加permissionSchool参数
        if (adminPermissionInfo != null && !adminPermissionInfo.isEmpty()) {
            // 确保安全获取值
            Object levelObj = adminPermissionInfo.get("permissionLevel");
            permissionLevel = (levelObj != null) ? 
                             ((levelObj instanceof Integer) ? (Integer)levelObj : 
                              (levelObj instanceof Number) ? ((Number)levelObj).intValue() : 0) 
                             : 0;
            
            Object schoolsObj = adminPermissionInfo.get("accessibleSchools");
            accessibleSchools = (schoolsObj != null && schoolsObj instanceof String) ? (String)schoolsObj : "";
            
            // 获取permissionSchool参数
            Object permissionSchoolObj = adminPermissionInfo.get("permissionSchool");
            permissionSchool = (permissionSchoolObj != null && permissionSchoolObj instanceof String) ? (String)permissionSchoolObj : "";
            
            // 如果permissionSchool为空但accessibleSchools不为空，使用accessibleSchools的值
            if ((permissionSchool == null || permissionSchool.isEmpty()) && accessibleSchools != null && !accessibleSchools.isEmpty()) {
                permissionSchool = accessibleSchools;
            }
        }
        
        // 根据权限级别过滤查询
        java.util.List<com.alumni.back.Entity.Alumni> rows = registerMapper.selectAlumniList(
            alumniId, ycywSchoolsAttended, chineseName, email, contactNumber, 
            tagIds, tagIdsList, stateList, offset, pageSize, 
            permissionLevel, accessibleSchools, permissionSchool
        );
        
        int total = registerMapper.countAlumniList(
            alumniId, ycywSchoolsAttended, chineseName, email, contactNumber, 
            tagIds, tagIdsList, stateList, permissionLevel, accessibleSchools, permissionSchool
        );
        
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("total", total);
        data.put("rows", rows);
        
        // 添加权限信息到返回结果
        data.put("permissionLevel", permissionLevel);
        data.put("accessibleSchools", accessibleSchools);
        data.put("permissionSchool", permissionSchool); // 添加permissionSchool到返回结果
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> getAlumniDetailById(String alumniId) {
        Map<String, Object> result = new HashMap<>();
        Alumni alumni = registerMapper.findAlumniDetailById(alumniId);
        if (alumni != null) {
            alumni.setPassword(null); // 屏蔽密码字段
        }
        List<TertiaryInformation> tertiaryList = registerMapper.selectTertiaryByAlumniId(alumniId);
        List<CareerInformation> careerList = registerMapper.selectCareerByAlumniId(alumniId);
        Map<String, Object> data = new HashMap<>();
        data.put("alumni", alumni);
        data.put("tertiary_information", tertiaryList);
        data.put("carrer_information", careerList);
        if (alumni != null) {
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", data);
        } else {
            result.put("code", 1);
            result.put("msg", "未找到该校友");
            result.put("data", null);
        }
        return result;
    }

    @Override
    public Map<String, Object> updateAlumniById(Alumni alumni, List<TertiaryInformation> tertiaryList, List<CareerInformation> careerList) {
        Map<String, Object> result = new HashMap<>();
        alumni.setUpdateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        // 只在有新tagIds时才更新，否则保持原值
        // 不要强制置空tagIds
        // role默认值
        if (alumni.getRole() == null || alumni.getRole().isEmpty()) {
            alumni.setRole("alumni");
        }
        // 只在有新密码时才更新密码，否则保持原密码
        if (alumni.getPassword() == null || alumni.getPassword().isEmpty()) {
            Alumni old = registerMapper.findByAlumniId(alumni.getAlumniId());
            if (old != null) {
                alumni.setPassword(old.getPassword());
            }
        }
        int updated = registerMapper.updateAlumniById(alumni);
        // 先删后插
        registerMapper.deleteTertiaryByAlumniId(alumni.getAlumniId());
        registerMapper.deleteCareerByAlumniId(alumni.getAlumniId());
        if (tertiaryList != null) {
            for (TertiaryInformation t : tertiaryList) {
                t.setId(java.util.UUID.randomUUID().toString());
                t.setAlumniId(alumni.getAlumniId());
                registerMapper.insertTertiaryInfo(t);
            }
        }
        if (careerList != null) {
            for (CareerInformation c : careerList) {
                c.setId(java.util.UUID.randomUUID().toString());
                c.setAlumniId(alumni.getAlumniId());
                registerMapper.insertCareerInfo(c);
            }
        }
        if (updated > 0) {
            result.put("code", 0);
            result.put("msg", "更新成功");
            result.put("data", null);
            databaseWebSocketHandler.sendUpdateMessage("alumni changed");
        } else {
            result.put("code", 1);
            result.put("msg", "更新失败");
            result.put("data", null);
        }
        return result;
    }

    @Override
    public int deleteTertiaryById(String id) {
        int result = registerMapper.deleteTertiaryById(id);
        if (result > 0) {
            databaseWebSocketHandler.sendUpdateMessage("tertiary changed");
        }
        return result;
    }

    @Override
    public int deleteCareerById(String id) {
        int result = registerMapper.deleteCareerById(id);
        if (result > 0) {
            databaseWebSocketHandler.sendUpdateMessage("career changed");
        }
        return result;
    }

    @Override
    public int deleteAlumniById(String id) {
        int result = registerMapper.deleteAlumniById(id);
        if (result > 0) {
            databaseWebSocketHandler.sendUpdateMessage("alumni changed");
        }
        return result;
    }
    
    @Override
    public int deleteActivityRegistrationByAlumniId(String alumniId) {
        return activityRegistrationMapper.deleteByAlumniId(alumniId);
    }
    
    @Override
    public int deleteChapterApplicationByAlumniId(String alumniId) {
        return chapterApplicationMapper.deleteByAlumniId(alumniId);
    }
    
    @Override
    public int deleteVisitRecordByAlumniId(String alumniId) {
        return visitRecordMapper.deleteByAlumniId(alumniId);
    }

    @Override
    public int getPendingAlumniCount() {
        return registerMapper.countPendingAlumni();
    }

    private String normalizeDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        String dateStr = dateObj.toString();
        if (dateStr.trim().isEmpty()) {
            return null;
        }

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

        // Return as is if already in yyyy-MM-dd or another format
        return dateStr;
    }

    @Override
    public String generateUniqueAlumniId() {
        Random random = new Random();
        String generatedId;
        int maxAttempts = 100; // 最大尝试次数，避免无限循环
        int attempts = 0;
        
        do {
            // 生成9位随机数字
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                sb.append(random.nextInt(10));
            }
            generatedId = sb.toString();
            attempts++;
            
            // 检查学号是否已存在
            com.alumni.back.Entity.Alumni existingAlumni = registerMapper.findByAlumniId(generatedId);
            if (existingAlumni == null) {
                return generatedId; // 找到唯一学号
            }
        } while (attempts < maxAttempts);
        
        throw new RuntimeException("无法生成唯一学号，请稍后重试");
    }

    @Override
    public int insertAlumni(Alumni alumni) {
        return registerMapper.insertAlumni(alumni);
    }

    @Override
    public List<Alumni> matchGmiData(MatchGmiRequest request) {
        List<Alumni> all = registerMapper.selectStateZeroAlumni();
        List<Alumni> result = new ArrayList<>();
        for (Alumni a : all) {
            int matchCount = 0;
            if (request.getChineseName() != null && request.getChineseName().equals(a.getChineseName())) matchCount++;
            if (request.getFirstName() != null && request.getLastName() != null
                && request.getFirstName().equals(a.getFirstName())
                && request.getLastName().equals(a.getLastName())) matchCount++;
            if (request.getBirthday() != null && request.getBirthday().equals(a.getBirthday())) matchCount++;
            if (matchCount >= 2) result.add(a);
        }
        return result;
    }

    @Override
    public void matchAndActivate(String gmiAlumniId, String reviewAlumniId) {
        // 删除GMI原始记录
        registerMapper.deleteAlumniById(gmiAlumniId);
        // 更新审核校友为已激活
        registerMapper.updateAlumniStateById(reviewAlumniId, 1);
    }
} 