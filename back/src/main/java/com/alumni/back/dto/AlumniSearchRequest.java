package com.alumni.back.dto;

import lombok.Data;

@Data
public class AlumniSearchRequest {
    private String name; // 支持中文名、英文名模糊搜索
    private String email; // 邮箱
    private String ycywSchoolsAttended; // 就读学校
    private String university; // 大学
    private Integer pageNum = 1; // 分页页码
    private Integer pageSize = 10; // 分页大小
    private Integer offset; // 分页偏移量
    private String region; // 所在地区（大陆、香港、海外）
    private Integer chapterId; // 分会ID，用于查询分会成员
    
    // 添加权限相关字段
    private Integer alumniInfoManagementPermission = 0; // 校友信息管理权限级别
    private String permissionSchool; // 权限限定的学校范围
    private String userRole; // 当前用户角色（alumni/admin）
} 