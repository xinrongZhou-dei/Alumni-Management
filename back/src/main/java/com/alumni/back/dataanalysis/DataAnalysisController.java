package com.alumni.back.dataanalysis;

import com.alumni.back.vo.DataAnalysisVO;
import com.alumni.back.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DataAnalysisController {
    
    @Autowired
    private DataAnalysisService dataAnalysisService;
    
    @Autowired
    private PermissionUtil permissionUtil;
    
    @GetMapping("/data-analysis")
    public Object getDataAnalysis(@RequestAttribute("Authorization") String token) {
        // 验证数据分析权限 - 只读权限
        PermissionUtil.PermissionResult permissionResult = permissionUtil.validateDataAnalysisPermission(token, 1);
        if (!permissionResult.isSuccess()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", permissionResult.getCode());
            result.put("msg", permissionResult.getMessage());
            return result;
        }
        
        return dataAnalysisService.getDataAnalysis();
    }
} 