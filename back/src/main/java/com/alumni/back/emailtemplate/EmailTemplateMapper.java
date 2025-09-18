package com.alumni.back.emailtemplate;

import com.alumni.back.Entity.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmailTemplateMapper {
    List<EmailTemplate> selectListByTargetType(@Param("targetType") String targetType);
    int insert(EmailTemplate emailTemplate);
    int update(EmailTemplate emailTemplate);
    int deleteById(@Param("id") String id);
} 