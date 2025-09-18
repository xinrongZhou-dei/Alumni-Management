package com.alumni.back.activate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/activate")
public class ActivateController {
    @Autowired
    private ActivateService activateService;

    // 1. 校验激活信息（学号、姓名、生日、原邮箱）
    @PostMapping("/verify-info")
    public Map<String, Object> verifyInfo(@RequestBody Map<String, String> request) {
        return activateService.verifyInfo(request);
    }

    // 2. 校验验证码
    @PostMapping("/verify-code")
    public Map<String, Object> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        return activateService.verifyCode(email, code);
    }

    // 3. 设置新密码
    @PostMapping("/set-password")
    public Map<String, Object> setPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        return activateService.setPassword(email, password);
    }

    // 4. 完成激活（设置私人邮箱、state=1）
    @PostMapping("/finish")
    public Map<String, Object> finishActivate(@RequestBody Map<String, String> request) {
        return activateService.finishActivate(request);
    }
} 