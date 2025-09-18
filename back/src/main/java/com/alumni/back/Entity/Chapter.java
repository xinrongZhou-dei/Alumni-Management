package com.alumni.back.Entity;

import lombok.Data;

@Data
public class Chapter {
    private Integer tagId;
    private String alumniId;
    private String status;
    private String createTime;
    private String updateTime;
    private String branchName; // 分会名称
    private Integer total; // 分会总人数
} 