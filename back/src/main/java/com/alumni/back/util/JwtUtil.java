package com.alumni.back.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtUtil {
    // 建议放到配置文件中
    private static final String SECRET = "alumni-platform-secret-key-1234567890123456"; // 32位
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 生成token
    public static String generateToken(Map<String, Object> claims, long expireSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireSeconds * 1000))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析token
    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

    // 校验token是否有效
    public static boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 