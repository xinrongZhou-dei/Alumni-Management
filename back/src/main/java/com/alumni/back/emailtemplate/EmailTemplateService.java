package com.alumni.back.emailtemplate;

import com.alumni.back.Entity.EmailTemplate;
import com.alumni.back.dto.EmailVariableRequest;
import com.alumni.back.vo.EmailVariableVO;
import java.util.List;

public interface EmailTemplateService {
    List<EmailTemplate> listByTargetType(String targetType);
    void add(EmailTemplate emailTemplate);
    void update(EmailTemplate emailTemplate);
    void delete(String id);
    List<EmailVariableVO> getEmailVariables(EmailVariableRequest request);
    java.util.List<com.alumni.back.vo.EmailVariableVO> getEmailVariablesDynamic(com.alumni.back.dto.EmailVariableRequest request);
} 