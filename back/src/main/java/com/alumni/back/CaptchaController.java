package com.alumni.back;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

@RestController
public class CaptchaController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/api/captcha")
    public void getCaptcha(HttpServletResponse response) throws IOException {
        // 生成唯一标识
        String uuid = UUID.randomUUID().toString();
        CircleCaptcha circleCaptcha = new CircleCaptcha(120, 40, 6, 20);
        // 使用自定义CodeGenerator生成6位纯数字验证码
        circleCaptcha.setGenerator(new CodeGenerator() {
            @Override
            public String generate() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    sb.append((int)(Math.random() * 10));
                }
                return sb.toString();
            }
            @Override
            public boolean verify(String code, String userInputCode) {
                return code.equals(userInputCode);
            }
        });
        circleCaptcha.createCode();
        String code = circleCaptcha.getCode();
        // 存入redis，60秒有效
        redisTemplate.opsForValue().set("captcha:" + uuid, code, 60, TimeUnit.SECONDS);
        // 设置响应头
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setHeader("Captcha-UUID", uuid);
        // 输出图片
        ImageIO.write(circleCaptcha.getImage(), "png", response.getOutputStream());
        response.getOutputStream().flush();
    }
} 