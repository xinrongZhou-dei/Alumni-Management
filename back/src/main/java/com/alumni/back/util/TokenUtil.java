package com.alumni.back.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);
    
    /**
     * 从请求中获取token，优先从Cookie中获取，如果Cookie中没有则从请求头中获取
     * @param request HTTP请求
     * @param headerToken 请求头中的token（可能为null）
     * @return token字符串，如果没有找到则返回null
     */
    public static String getTokenFromRequest(HttpServletRequest request, String headerToken) {
        // 从Cookie中获取token
        String token = null;
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            logger.debug("Found {} cookies in request", cookies.length);
            for (Cookie cookie : cookies) {
                logger.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "...");
                if ("alumni_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    logger.debug("Found alumni_token in cookie");
                    break;
                }
            }
        } else {
            logger.debug("No cookies found in request");
        }
        
        // 如果Cookie中没有token，尝试从请求头中获取
        if (token == null && headerToken != null) {
            token = headerToken;
            // 如果请求头中的token带有Bearer前缀，去掉前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            logger.debug("Using token from Authorization header");
        } else if (token == null) {
            logger.debug("No token found in cookies or headers");
        }
        
        return token;
    }
} 