package com.alumni.back.vo;

import com.alumni.back.Entity.Admin;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminInfoVo extends Admin {
    // 从Alumni表继承的字段
    private String chineseName;
    private String firstName;
    private String lastName;
    private String email;
} 