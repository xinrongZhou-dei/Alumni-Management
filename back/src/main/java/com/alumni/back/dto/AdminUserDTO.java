package com.alumni.back.dto;

import com.alumni.back.Entity.Admin;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

@Data
public class AdminUserDTO {
    // alumni 表中的字段
    private String chineseName;
    private String email;

    // admin 表中的所有字段
    @JsonUnwrapped
    private Admin admin;
} 