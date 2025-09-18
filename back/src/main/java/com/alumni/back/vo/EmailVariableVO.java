package com.alumni.back.vo;

import lombok.Data;
import java.util.Map;
 
@Data
public class EmailVariableVO {
    private String alumniId;  // 校友学号
    private Map<String, String> variables;  // 变量名和对应的值
} 