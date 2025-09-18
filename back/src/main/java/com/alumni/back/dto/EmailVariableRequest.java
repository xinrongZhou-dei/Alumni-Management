package com.alumni.back.dto;

import lombok.Data;
import java.util.List;
 
@Data
public class EmailVariableRequest {
    private List<String> alumniIds;  // 校友学号列表
    private String templateContent;  // 邮件模板内容
    private List<String> fields;     // 变量名（表名.字段名）列表
    private String activityUuid; // 活动UUID
} 