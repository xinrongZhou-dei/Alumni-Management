package com.alumni.back.permission;

import com.alumni.back.Entity.Admin;
import com.alumni.back.dto.NewAdminRequestDTO;

import java.util.Map;

public interface PermissionService {
    Map<String, Object> getAdminList(String query, Integer pageNum, Integer pageSize);
    void addAdmin(NewAdminRequestDTO admin);
    void updateAdmin(String id, Admin admin);
    void deleteAdmin(String id);
} 