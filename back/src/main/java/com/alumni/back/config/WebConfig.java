package com.alumni.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/login",
                    "/api/register/verify-email",
                    "/api/register/verify",
                    "/api/register/set-password",
                    "/api/forgot-password",
                    "/api/forgot-password-verify",
                    "/api/forgot-password-put",
                    "/api/activate/**",
                    "/api/wechat/message/notify",
                    "/api/wechat/qrcode/**",
                    "/api/wechat/verify-follow",
                    "/api/captcha"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有源，更宽松的配置
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                .exposedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers") // 暴露的响应头
                .allowCredentials(true) // 允许携带凭证
                .maxAge(3600); // 预检请求的缓存时间
    }
} 