package com.alumni.back.permission;

import com.alumni.back.Entity.Admin;
import com.alumni.back.Entity.Alumni;
import com.alumni.back.vo.AdminInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<AdminInfoVo> selectAdminList(@Param("query") String query,
                                      @Param("offset") Integer offset,
                                      @Param("pageSize") Integer pageSize);

    Integer countAdminList(@Param("query") String query);

    Admin findAdminById(@Param("id") String id);

    Integer findAlumnusCountById(@Param("id") String id);

    void insertAdmin(Admin admin);

    void updateAdmin(Admin admin);

    void deleteAdminById(@Param("id") String id);

    void insertAlumnus(Alumni alumnus);

    Integer findAlumnusCountByEmail(@Param("email") String email);
} 