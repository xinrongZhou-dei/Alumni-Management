package com.alumni.back.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Admin {
    @JsonProperty("id")
    private String id; // 学号，主键
    @JsonProperty("accessible_schools")
    private String accessibleSchools; // 可访问学校列表
    @JsonProperty("alumni_info_management_permission")
    private int alumniInfoManagementPermission; // 校友信息管理权限
    @JsonProperty("alumni_info_review_permission")
    private int alumniInfoReviewPermission; // 校友信息审核权限
    @JsonProperty("chapter_review_permission")
    private int chapterReviewPermission; // 分会审核权限
    @JsonProperty("activity_management_permission")
    private int activityManagementPermission; // 活动管理权限
    @JsonProperty("tag_management_permission")
    private int tagManagementPermission; // 标签管理权限
    @JsonProperty("email_template_permission")
    private int emailTemplatePermission; // 邮件模板权限
    @JsonProperty("data_analysis_permission")
    private int dataAnalysisPermission; // 数据分析权限
    @JsonProperty("permission_management_permission")
    private int permissionManagementPermission; // 权限管理权限
    @JsonProperty("campus_visit_record_permission")
    private int campusVisitRecordPermission; // 校园参观记录权限
} 