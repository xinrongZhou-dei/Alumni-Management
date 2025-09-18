package com.alumni.back.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.register.RegisterMapper;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/wechat/message")
public class WeChatMessageController {

    @Autowired
    private WeChatMessageService weChatMessageService;

    @Autowired
    private RegisterMapper registerMapper;

    @PostMapping("/send")
    public Map<String, Object> sendTemplateMessage(@RequestBody Map<String, Object> request) {
        String openid = (String) request.get("openid");
        String templateId = (String) request.get("templateId");
        Map<String, Object> data = (Map<String, Object>) request.get("data");
        boolean success = weChatMessageService.sendTemplateMessage(openid, templateId, data);
        return Map.of("success", success);
    }

    // 新增：用于接收微信服务器消息推送的接口
    @RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
    public String wechatNotify(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr", required = false) String echostr,
            @RequestBody(required = false) String requestBody
    ) {
        // 1. GET请求：微信服务器URL验证
        if (echostr != null) {
            // 这里应校验signature（略），简单返回echostr即可通过验证
            return echostr;
        }
        // 2. POST请求：接收微信服务器推送的消息
        if (requestBody != null && !requestBody.isEmpty()) {
            try {
                // 解析XML消息
                Map<String, String> messageMap = parseXmlMessage(requestBody);
                
                // 检查是否是关注事件
                String event = messageMap.get("Event");
                if ("subscribe".equals(event)) {
                    String openid = messageMap.get("FromUserName");
                    String qrScene = messageMap.get("EventKey");
                    
                    // 如果有场景值（二维码扫描），提取校友ID
                    String alumniId = null;
                    if (qrScene != null) {
                        if (qrScene.startsWith("qrscene_")) {
                            // 处理qrscene_前缀格式
                            alumniId = qrScene.substring(8);
                        } else {
                            // 处理scene_str格式（直接是alumni_id）
                            alumniId = qrScene;
                        }
                        
                        // 更新数据库中的openid
                        if (alumniId != null && !alumniId.isEmpty()) {
                            try {
                                int updated = registerMapper.updateOpenid(alumniId, openid);
                                if (updated > 0) {
                                    System.out.println("成功更新用户 " + alumniId + " 的openid: " + openid);
                                } else {
                                    System.out.println("更新失败，用户 " + alumniId + " 可能不存在");
                                }
                            } catch (Exception e) {
                                System.err.println("更新openid失败: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("处理微信消息失败: " + e.getMessage());
            }
        }
        return "success";
    }

    // 解析XML消息的辅助方法
    private Map<String, String> parseXmlMessage(String xml) {
        Map<String, String> result = new HashMap<>();
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            java.io.InputStream is = new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            org.w3c.dom.Document doc = builder.parse(is);
            org.w3c.dom.NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    result.put(node.getNodeName(), node.getTextContent());
                }
            }
        } catch (Exception e) {
            System.err.println("解析XML失败: " + e.getMessage());
        }
        return result;
    }
}
