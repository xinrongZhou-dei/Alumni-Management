package com.alumni.back.activate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alumni.back.register.RegisterMapper;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActivateServiceImpl implements ActivateService {
    @Autowired
    private RegisterMapper registerMapper;

    // 临时保存token->明文密码
    private static final Map<String, String> tempPasswordMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> verifyInfo(Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String alumniId = request.get("alumniId");
        String chineseName = request.get("chineseName");
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String campusEmail = request.get("campusEmail");
        String birthday = request.get("birthday");
        // 只查state=0的记录
        com.alumni.back.Entity.Alumni alumni = registerMapper.findByAlumniId(alumniId);
        if (alumni == null || alumni.getState() == null || alumni.getState() != 0) {
            result.put("code", 1);
            result.put("msg", "未找到未激活的校友记录");
            result.put("match_count", 0);
            return result;
        }
        int matchCount = 0;
        if (alumni.getAlumniId() != null && alumni.getAlumniId().equals(alumniId)) matchCount++;
        if (alumni.getChineseName() != null && alumni.getChineseName().equals(chineseName)) matchCount++;
        boolean enNameMatch = alumni.getFirstName() != null && alumni.getFirstName().equals(firstName)
            && alumni.getLastName() != null && alumni.getLastName().equals(lastName);
        if (enNameMatch) matchCount++;
        if (alumni.getEmail() != null && alumni.getEmail().equals(campusEmail)) matchCount++;
        if (alumni.getBirthday() != null && alumni.getBirthday().equals(birthday)) matchCount++;
        if (matchCount >= 3) {
            // 生成5分钟有效的临时token，包含alumniId
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("alumniId", alumniId);
            String token = com.alumni.back.util.JwtUtil.generateToken(claims, 300); // 5分钟
            result.put("code", 0);
            result.put("msg", "success");
            result.put("match_count", matchCount);
            result.put("token", token);
        } else {
            result.put("code", 2);
            result.put("msg", "匹配项不足，请重新进行身份校验或注册");
            result.put("match_count", matchCount);
        }
        return result;
    }

    @Override
    public Map<String, Object> verifyCode(String email, String code) {
        Map<String, Object> result = new HashMap<>();
        // email参数实际为token
        String token = email;
        if (!com.alumni.back.util.JwtUtil.isTokenValid(token)) {
            result.put("code", 1002);
            result.put("msg", "token无效或已过期");
            return result;
        }
        if (!"123456".equals(code)) {
            result.put("code", 1);
            result.put("msg", "验证码错误");
            return result;
        }
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @Override
    public Map<String, Object> setPassword(String email, String password) {
        Map<String, Object> result = new HashMap<>();
        // email参数实际为token
        String token = email;
        if (!com.alumni.back.util.JwtUtil.isTokenValid(token)) {
            result.put("code", 1002);
            result.put("msg", "token无效或已过期");
            return result;
        }
        String alumniId = String.valueOf(com.alumni.back.util.JwtUtil.parseToken(token).get("alumniId"));
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1003);
            result.put("msg", "token中无alumniId");
            return result;
        }
        if (password == null || password.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "密码不能为空");
            return result;
        }
        String encryptedPwd = org.springframework.util.DigestUtils.md5DigestAsHex(password.getBytes());
        int updateCount = registerMapper.updatePasswordByAlumniId(alumniId, encryptedPwd);
        if (updateCount > 0) {
            result.put("code", 0);
            result.put("msg", "success");
            // 保存明文密码到临时Map
            tempPasswordMap.put(token, password);
        } else {
            result.put("code", 2);
            result.put("msg", "密码设置失败，学号不存在");
        }
        return result;
    }

    @Override
    public Map<String, Object> finishActivate(Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String token = request.get("token");
        String newEmail = request.get("new_email");
        if (!com.alumni.back.util.JwtUtil.isTokenValid(token)) {
            result.put("code", 1002);
            result.put("msg", "token无效或已过期");
            return result;
        }
        String alumniId = String.valueOf(com.alumni.back.util.JwtUtil.parseToken(token).get("alumniId"));
        if (alumniId == null || alumniId.isEmpty()) {
            result.put("code", 1003);
            result.put("msg", "token中无alumniId");
            return result;
        }
        if (newEmail == null || newEmail.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "新邮箱不能为空");
            return result;
        }
        com.alumni.back.Entity.Alumni alumni = registerMapper.findByAlumniId(alumniId);
        if (alumni == null) {
            result.put("code", 2);
            result.put("msg", "学号不存在");
            return result;
        }
        alumni.setEmail(newEmail);
        alumni.setState(1);
        int updateCount = registerMapper.updateAlumniById(alumni);
        if (updateCount > 0) {
            result.put("code", 0);
            result.put("msg", "success");
            result.put("alumniId", alumni.getAlumniId());
            // 返回明文密码
            String plainPwd = tempPasswordMap.get(token);
            result.put("password", plainPwd != null ? plainPwd : "");
            tempPasswordMap.remove(token);
        } else {
            result.put("code", 3);
            result.put("msg", "激活失败，数据库更新异常");
        }
        return result;
    }
} 