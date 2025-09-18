package com.alumni.back.permission;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.Admin;
import com.alumni.back.common.CustomException;
import com.alumni.back.dto.NewAdminRequestDTO;
import com.alumni.back.vo.AdminInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Map<String, Object> getAdminList(String query, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        List<AdminInfoVo> list = permissionMapper.selectAdminList(query, offset, pageSize);
        Integer total = permissionMapper.countAdminList(query);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional
    public void addAdmin(NewAdminRequestDTO adminRequest) {
        // 1. 检查校友ID是否已存在
        Integer alumnusCountById = permissionMapper.findAlumnusCountById(adminRequest.getId());
        if (alumnusCountById > 0) {
            throw new CustomException("该学号已被注册!");
        }

        // 2. 检查邮箱是否已存在
        Integer alumnusCountByEmail = permissionMapper.findAlumnusCountByEmail(adminRequest.getEmail());
        if (alumnusCountByEmail > 0) {
            throw new CustomException("该邮箱已被注册!");
        }

        // 3. 创建并插入Alumni信息
        Alumni newAlumnus = new Alumni();
        newAlumnus.setAlumniId(adminRequest.getId());
        newAlumnus.setEmail(adminRequest.getEmail());
        newAlumnus.setRole("admin");
        permissionMapper.insertAlumnus(newAlumnus);

        // 4. 插入admin信息
        permissionMapper.insertAdmin(adminRequest);
    }

    @Override
    public void updateAdmin(String id, Admin admin) {
        // 1. 检查要更新的管理员是否存在
        Admin existingAdmin = permissionMapper.findAdminById(id);
        if (existingAdmin == null) {
            throw new CustomException("要更新的管理员不存在!");
        }
        admin.setId(id);
        permissionMapper.updateAdmin(admin);
    }

    @Override
    public void deleteAdmin(String id) {
        // 1. 检查要删除的管理员是否存在
        Admin existingAdmin = permissionMapper.findAdminById(id);
        if (existingAdmin == null) {
            throw new CustomException("要删除的管理员不存在!");
        }
        permissionMapper.deleteAdminById(id);
    }
} 