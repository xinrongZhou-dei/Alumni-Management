package com.alumni.back.config;

import com.alumni.back.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.annotation.Resource;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    // 需要管理员权限的接口路径片段
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/activity/add", "/api/activity/edit", "/api/activity/delete",
            "/api/alumni/manage", "/api/alumni-add", "/api/alumni/edit", "/api/alumni/delete",
            "/api/tag-manage", "/api/tag/add", "/api/tag/edit", "/api/tag/delete"
            // 可继续补充其它敏感接口
    );

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求(OPTIONS)直接放行
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = null;
        
        // 优先从请求头中获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉"Bearer "前缀
        }
        
        // 如果请求头中没有token，再从Cookie中获取
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("alumni-token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        
        if (token == null || !JwtUtil.isTokenValid(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token无效\"}");
            return false;
        }
        // 新增：校验token是否在redis中
        String redisKey = "token:" + token;
        Object alumniId = redisTemplate.opsForValue().get(redisKey);
        if (alumniId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token无效\"}");
            return false;
        }
        // 单点登录校验：token必须和sso:alumniId一致
        String ssoKey = "sso:" + alumniId;
        String ssoToken = (String) redisTemplate.opsForValue().get(ssoKey);
        if (!token.equals(ssoToken)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"账号已在其他地方登录\"}");
            return false;
        }

        // 验证通过后，将解析出的token（不带Bearer前缀）放入request attribute，方便后续controller使用
        request.setAttribute("Authorization", token);

        // 判断是否需要管理员权限
        String path = request.getRequestURI();
        boolean needAdmin = ADMIN_PATHS.stream().anyMatch(path::contains);
        if (needAdmin) {
            Claims claims = JwtUtil.parseToken(token);
            String role = String.valueOf(claims.get("role"));
            if (!"admin".equals(role)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限\"}");
                return false;
            }
        }
        return true;
    }
} 