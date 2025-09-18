package com.alumni.back.dto;

import com.alumni.back.Entity.Admin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewAdminRequestDTO extends Admin {
    private String email;
} 