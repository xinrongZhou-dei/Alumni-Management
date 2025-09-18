package com.alumni.back.Entity;

import lombok.Data;

@Data
public class Alumni {
    private String alumniId;
    private String salutation;
    private String firstName;
    private String lastName;
    private String chineseName;
    private String email;
    private String contactNumber;
    private String wechatId;
    private String correspondenceAddress;
    private String currentLocation;
    private String zohoAlumniNumber;
    private String affiliation; // Alumni/Donor/Staff
    private String ycywSchoolsAttended;
    private String studyPeriodStart; // yyyy-MM-dd
    private String studyPeriodEnd;   // yyyy-MM-dd
    private String lastSchoolDay;    // yyyy-MM-dd
    private Integer yearLeft;
    private String maritalStatus;
    private Boolean showYearLeft;
    private Boolean showTertiaryUniversity;
    private Boolean showTertiaryMajor;
    private Boolean showCareerCompany;
    private Boolean showJobTitle;
    private Boolean showIndustry;
    private Boolean showCountry;
    private String password;
    private String updateTime;
    private String role; // alumni/admin/SuperAdmin
    private String tagIds; // 标签ID列表，仅管理员后台可见
    private String region; // 所在地区（大陆、香港、海外）
    private Integer state; // 激活状态，0未激活，1已激活
    private String birthday; // 出生日期，格式：yyyy-MM-dd
    private String openid; // 微信服务号openid
} 