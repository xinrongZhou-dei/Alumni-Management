package com.alumni.back.emailtemplate;

import com.alumni.back.Entity.EmailTemplate;
import com.alumni.back.dto.EmailVariableRequest;
import com.alumni.back.vo.EmailVariableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-template")
public class EmailTemplateController {
    @Autowired
    private EmailTemplateService emailTemplateService;

    // 查询列表（不返回content字段）
    @GetMapping("/list")
    public List<EmailTemplate> list(@RequestParam String targetType) {
        return emailTemplateService.listByTargetType(targetType);
    }

    // 新增
    @PostMapping
    public void add(@RequestBody EmailTemplate emailTemplate) {
        emailTemplateService.add(emailTemplate);
    }

    // 修改
    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody EmailTemplate emailTemplate) {
        emailTemplate.setId(id);
        emailTemplateService.update(emailTemplate);
    }

    // 删除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        emailTemplateService.delete(id);
    }

    // 邮件变量替换
    @PostMapping("/variables")
    public List<EmailVariableVO> getEmailVariables(@RequestBody EmailVariableRequest request) {
        return emailTemplateService.getEmailVariables(request);
    }

    @PostMapping("/variables-dynamic")
    public List<EmailVariableVO> getEmailVariablesDynamic(@RequestBody EmailVariableRequest request) {
        if (!EmailVariableFieldWhiteListUtil.isFieldsSafe(request.getFields())) {
            throw new IllegalArgumentException("非法字段名或表名！");
        }
        return emailTemplateService.getEmailVariablesDynamic(request);
    }

    // 字段白名单校验工具
    class EmailVariableFieldWhiteListUtil {
        private static final java.util.Map<String, java.util.Set<String>> TABLE_FIELD_WHITELIST = new java.util.HashMap<>();
        static {
            TABLE_FIELD_WHITELIST.put("alumnus", new java.util.HashSet<>(java.util.Arrays.asList(
                "alumni_id", "chinese_name", "email", "contact_number", "ycyw_schools_attended", "first_name", "last_name"
            )));
            TABLE_FIELD_WHITELIST.put("activity", new java.util.HashSet<>(java.util.Arrays.asList(
                "name", "detail"
            )));
            TABLE_FIELD_WHITELIST.put("activity_registration", new java.util.HashSet<>(java.util.Arrays.asList(
                "alumni_id", "activity_uuid", "companions", "payment_proof", "created_at", "salutation", "first_name", "last_name", "chinese_name", "email", "contact_number", "wechat_id", "correspondence_address", "current_location"
            )));
        }
        public static boolean isFieldsSafe(java.util.List<String> fields) {
            if (fields == null) return true;
            for (String field : fields) {
                String[] arr = field.split("\\.");
                if (arr.length != 2) return false;
                String table = arr[0], col = arr[1];
                if (!TABLE_FIELD_WHITELIST.containsKey(table)) return false;
                if (!TABLE_FIELD_WHITELIST.get(table).contains(col)) return false;
            }
            return true;
        }
    }
} 