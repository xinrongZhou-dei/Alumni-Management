package com.alumni.back.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class WeChatUtil {
    @Value("${wechat.appid}")
    private String appid;
    @Value("${wechat.secret}")
    private String secret;

    private static String accessToken = null;
    private static long expiresAt = 0;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public synchronized String getAccessToken() throws Exception {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < expiresAt) {
            return accessToken;
        }
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
            appid, secret
        );
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[DEBUG] 微信API返回: " + response.body());
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        if (result.get("access_token") != null) {
            accessToken = (String) result.get("access_token");
            int expiresIn = (Integer) result.get("expires_in");
            expiresAt = now + (expiresIn - 200) * 1000;
            return accessToken;
        } else {
            throw new RuntimeException("获取access_token失败：" + response.body());
        }
    }

    public boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data) throws Exception {
        String accessToken = null;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",
            accessToken
        );
        Map<String, Object> message = Map.of(
            "touser", openid,
            "template_id", templateId,
            "data", data
        );
        String jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        Map<String, Object> result = null;
        try {
            result = objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return result.get("errcode") != null && ((Integer) result.get("errcode")) == 0;
    }

    /**
     * 获取所有关注者的openid列表
     */
    public java.util.List<String> getAllOpenids() throws Exception {
        String accessToken = getAccessToken();
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/user/get?access_token=%s",
            accessToken
        );
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        java.util.List<String> openids = new java.util.ArrayList<>();
        if (result.get("data") != null) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data.get("openid") instanceof java.util.List) {
                openids = (java.util.List<String>) data.get("openid");
            }
        }
        // 微信有next_openid，分页拉取，这里只实现单页（最多1万），如需全量可递归/循环实现
        return openids;
    }

    /**
     * 生成微信临时二维码（使用scene_str，确保回调时EventKey为qrscene_学号字符串）
     */
    public String generateTemporaryQRCode(String alumniId) throws Exception {
        String accessToken = getAccessToken();
        String url = String.format(
            "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s",
            accessToken
        );
        
        // 构建请求体，使用QR_STR_SCENE和scene_str
        Map<String, Object> requestBody = Map.of(
            "expire_seconds", 600, // 10分钟有效期
            "action_name", "QR_STR_SCENE",
            "action_info", Map.of(
                "scene", Map.of(
                    "scene_str", alumniId // 只传字符串学号
                )
            )
        );
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        
        if (result.get("ticket") != null) {
            String ticket = (String) result.get("ticket");
            return String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s", ticket);
        } else {
            throw new RuntimeException("生成临时二维码失败：" + response.body());
        }
    }
}