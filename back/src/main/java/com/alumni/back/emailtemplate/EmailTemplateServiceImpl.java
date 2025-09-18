package com.alumni.back.emailtemplate;

import com.alumni.back.Entity.EmailTemplate;
import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.TertiaryInformation;
import com.alumni.back.Entity.CareerInformation;
import com.alumni.back.activity.ActivityMapper;
import com.alumni.back.activity.ActivityRegistrationMapper;
import com.alumni.back.dto.EmailVariableRequest;
import com.alumni.back.register.RegisterMapper;
import com.alumni.back.vo.EmailVariableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
    @Autowired
    private EmailTemplateMapper emailTemplateMapper;
    
    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    public List<EmailTemplate> listByTargetType(String targetType) {
        return emailTemplateMapper.selectListByTargetType(targetType);
    }

    @Override
    public void add(EmailTemplate emailTemplate) {
        emailTemplate.setId(UUID.randomUUID().toString());
        emailTemplateMapper.insert(emailTemplate);
    }

    @Override
    public void update(EmailTemplate emailTemplate) {
        emailTemplateMapper.update(emailTemplate);
    }

    @Override
    public void delete(String id) {
        emailTemplateMapper.deleteById(id);
    }

    @Override
    public List<EmailVariableVO> getEmailVariables(EmailVariableRequest request) {
        List<EmailVariableVO> result = new ArrayList<>();
        
        // 提取模板中的变量名
        Set<String> variableNames = extractVariableNames(request.getTemplateContent());
        
        // 为每个校友ID查询信息并构建变量映射
        for (String alumniId : request.getAlumniIds()) {
            EmailVariableVO vo = new EmailVariableVO();
            vo.setAlumniId(alumniId);
            
            // 查询校友基本信息
            Alumni alumni = registerMapper.findByAlumniId(alumniId);
            if (alumni == null) {
                continue;
            }
            
            // 查询教育信息
            List<TertiaryInformation> tertiaryList = registerMapper.selectTertiaryByAlumniId(alumniId);
            
            // 查询职业信息
            List<CareerInformation> careerList = registerMapper.selectCareerByAlumniId(alumniId);
            
            // 构建变量映射
            Map<String, String> variables = buildVariableMap(alumni, tertiaryList, careerList, variableNames);
            vo.setVariables(variables);
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 从模板内容中提取变量名
     */
    private Set<String> extractVariableNames(String templateContent) {
        Set<String> variableNames = new HashSet<>();
        if (templateContent == null || templateContent.isEmpty()) {
            return variableNames;
        }
        
        // 匹配 {{变量名}} 格式的变量
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(templateContent);
        
        while (matcher.find()) {
            variableNames.add(matcher.group(1).trim());
        }
        
        return variableNames;
    }
    
    /**
     * 构建变量映射
     */
    private Map<String, String> buildVariableMap(Alumni alumni, List<TertiaryInformation> tertiaryList, 
                                               List<CareerInformation> careerList, Set<String> variableNames) {
        Map<String, String> variables = new HashMap<>();
        
        for (String varName : variableNames) {
            String value = getVariableValue(alumni, tertiaryList, careerList, varName);
            variables.put(varName, value != null ? value : "");
        }
        
        return variables;
    }
    
    /**
     * 根据变量名获取对应的值
     */
    private String getVariableValue(Alumni alumni, List<TertiaryInformation> tertiaryList, 
                                  List<CareerInformation> careerList, String varName) {
        switch (varName.toLowerCase()) {
            // 基本信息
            case "alumni_id":
            case "学号":
                return alumni.getAlumniId();
            case "salutation":
            case "称谓":
                return alumni.getSalutation();
            case "first_name":
            case "名":
                return alumni.getFirstName();
            case "last_name":
            case "姓":
                return alumni.getLastName();
            case "chinese_name":
            case "中文名":
                return alumni.getChineseName();
            case "email":
            case "邮箱":
                return alumni.getEmail();
            case "contact_number":
            case "联系电话":
                return alumni.getContactNumber();
            case "wechat_id":
            case "微信号":
                return alumni.getWechatId();
            case "correspondence_address":
            case "通讯地址":
                return alumni.getCorrespondenceAddress();
            case "current_location":
            case "现居地":
                return alumni.getCurrentLocation();
            case "zoho_alumni_number":
            case "zoho校友号":
                return alumni.getZohoAlumniNumber();
            case "affiliation":
            case "身份":
                return alumni.getAffiliation();
            case "ycyw_schools_attended":
            case "就读学校":
                return alumni.getYcywSchoolsAttended();
            case "study_period_start":
            case "就读开始时间":
                return alumni.getStudyPeriodStart();
            case "study_period_end":
            case "就读结束时间":
                return alumni.getStudyPeriodEnd();
            case "last_school_day":
            case "最后在校日":
                return alumni.getLastSchoolDay();
            case "year_left":
            case "毕业年份":
                return alumni.getYearLeft() != null ? alumni.getYearLeft().toString() : "";
            case "marital_status":
            case "婚姻状况":
                return alumni.getMaritalStatus();
            case "region":
            case "地区":
                return alumni.getRegion();
                
            // 教育信息（取第一条记录）
            case "university_college":
            case "大学":
                return tertiaryList != null && !tertiaryList.isEmpty() ? tertiaryList.get(0).getUniversityCollege() : "";
            case "degree":
            case "学位":
                return tertiaryList != null && !tertiaryList.isEmpty() ? tertiaryList.get(0).getDegree() : "";
            case "major":
            case "专业":
                return tertiaryList != null && !tertiaryList.isEmpty() ? tertiaryList.get(0).getMajor() : "";
            case "graduation_year":
            case "大学毕业年份":
                return tertiaryList != null && !tertiaryList.isEmpty() && tertiaryList.get(0).getGraduationYear() != null 
                       ? tertiaryList.get(0).getGraduationYear().toString() : "";
            case "tertiary_country_region":
            case "大学所在地区":
                return tertiaryList != null && !tertiaryList.isEmpty() ? tertiaryList.get(0).getCountryRegion() : "";
                
            // 职业信息（取第一条记录）
            case "company_organization":
            case "公司":
                return careerList != null && !careerList.isEmpty() ? careerList.get(0).getCompanyOrganization() : "";
            case "job_title":
            case "职位":
                return careerList != null && !careerList.isEmpty() ? careerList.get(0).getJobTitle() : "";
            case "industry":
            case "行业":
                return careerList != null && !careerList.isEmpty() && careerList.get(0).getIndustry() != null 
                       ? careerList.get(0).getIndustry().toString() : "";
            case "career_country_region":
            case "工作地区":
                return careerList != null && !careerList.isEmpty() ? careerList.get(0).getCountryRegion() : "";
                
            default:
                return "";
        }
    }

    // 新增：动态多表字段查询实现
    public List<EmailVariableVO> getEmailVariablesDynamic(EmailVariableRequest request) {
        List<String> alumniIds = request.getAlumniIds();
        List<String> fields = request.getFields();
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 按表分组字段
        Map<String, List<String>> tableFieldMap = new HashMap<>();
        for (String field : fields) {
            String[] arr = field.split("\\.");
            if (arr.length != 2) continue;
            tableFieldMap.computeIfAbsent(arr[0], k -> new ArrayList<>()).add(arr[1]);
        }

        // 2. 查询每个表的数据
        Map<String, Map<String, Map<String, String>>> tableData = new HashMap<>();
        for (String table : tableFieldMap.keySet()) {
            List<String> tableFields = tableFieldMap.get(table);
            Map<String, Map<String, String>> data = new HashMap<>();
            if ("alumnus".equals(table)) {
                List<Map<String, Object>> rows = registerMapper.batchSelectFields(alumniIds, tableFields);
                for (Map<String, Object> row : rows) {
                    String alumniId = (String) row.get("alumni_id");
                    Map<String, String> fieldMap = new HashMap<>();
                    for (String f : tableFields) {
                        fieldMap.put(f, row.get(f) != null ? row.get(f).toString() : "");
                    }
                    data.put(alumniId, fieldMap);
                }
            } else if ("activity_registration".equals(table)) {
                List<Map<String, Object>> rows = activityRegistrationMapper.batchSelectFields(alumniIds, tableFields);
                for (Map<String, Object> row : rows) {
                    String alumniId = (String) row.get("alumni_id");
                    Map<String, String> fieldMap = new HashMap<>();
                    for (String f : tableFields) {
                        fieldMap.put(f, row.get(f) != null ? row.get(f).toString() : "");
                    }
                    data.put(alumniId, fieldMap);
                }
            } else if ("activity".equals(table)) {
                // 用activityUuid查一次，所有校友共用
                String activityUuid = request.getActivityUuid();
                if (activityUuid != null && !activityUuid.isEmpty()) {
                    List<String> activityUuids = java.util.Collections.singletonList(activityUuid);
                    List<Map<String, Object>> rows = activityMapper.batchSelectFields(activityUuids, tableFields);
                    for (Map<String, Object> row : rows) {
                        String uuid = (String) row.get("uuid");
                        Map<String, String> fieldMap = new HashMap<>();
                        for (String f : tableFields) {
                            fieldMap.put(f, row.get(f) != null ? row.get(f).toString() : "");
                        }
                        // 所有校友都用同一个活动内容
                        for (String alumniId : alumniIds) {
                            data.put(alumniId, fieldMap);
                        }
                    }
                }
            }
            tableData.put(table, data);
        }

        // 3. 组装每个校友的变量Map
        List<EmailVariableVO> result = new ArrayList<>();
        for (String alumniId : alumniIds) {
            Map<String, String> variables = new HashMap<>();
            for (String field : fields) {
                String[] arr = field.split("\\.");
                if (arr.length != 2) continue;
                String table = arr[0], col = arr[1];
                String value = tableData.getOrDefault(table, Collections.emptyMap())
                        .getOrDefault(alumniId, Collections.emptyMap())
                        .getOrDefault(col, "");
                variables.put(field, value);
            }
            EmailVariableVO vo = new EmailVariableVO();
            vo.setAlumniId(alumniId);
            vo.setVariables(variables);
            result.add(vo);
        }
        return result;
    }
} 