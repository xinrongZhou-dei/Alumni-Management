package com.alumni.back.Entity;

import lombok.Data;
import java.util.Date;

@Data
public class EmailTemplate {
    private String id;
    private String templateName;
    private String content;
    private String targetType;
    private String theme;
    private Date createTime;
    private Date updateTime;
} 