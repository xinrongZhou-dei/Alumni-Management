package com.alumni.back.vo;

import lombok.Data;

@Data
public class AlumniSearchResultVO {
    private String alumniId;
    private String chineseName;
    private String firstName;
    private String lastName;
    private String email;
    private String ycywSchoolsAttended;
    private Integer yearLeft;
    private String university;
    private String major;
    private String company;
    private String jobTitle;
    private String industry;
    private String countryRegion;
    private String region;
    private Boolean showYearLeft;
    private Boolean showUniversity;
    private Boolean showMajor;
    private Boolean showCompany;
    private Boolean showJobTitle;
    private Boolean showIndustry;
    private Boolean showCountryRegion;
} 