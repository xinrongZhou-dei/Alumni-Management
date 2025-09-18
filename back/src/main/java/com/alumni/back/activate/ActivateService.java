package com.alumni.back.activate;

import java.util.Map;

public interface ActivateService {
    Map<String, Object> verifyInfo(Map<String, String> request);
    Map<String, Object> verifyCode(String email, String code);
    Map<String, Object> setPassword(String email, String password);
    Map<String, Object> finishActivate(Map<String, String> request);
} 