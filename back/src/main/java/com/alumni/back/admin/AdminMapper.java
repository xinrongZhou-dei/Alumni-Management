package com.alumni.back.admin;

import com.alumni.back.Entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.alumni.back.dto.AdminUserDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(String id);
}