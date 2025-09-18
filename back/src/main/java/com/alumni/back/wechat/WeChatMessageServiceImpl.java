package com.alumni.back.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatMessageServiceImpl implements WeChatMessageService {

    @Autowired
    private WeChatUtil weChatUtil;
    
    @Value("${wechat.template.chapter-review-success}")
    private String chapterReviewSuccessTemplateId;

    @Value("${wechat.template.create-chapter-admin}")
    private String createChapterAdminTemplateId;

    @Override
    public boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data) {
        try {
            java.util.List<String> allOpenids = weChatUtil.getAllOpenids();
            if (!allOpenids.contains(openid)) {
                return false;
            }
            return weChatUtil.sendTemplateMessage(openid, templateId, data);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈，便于调试
            // 可加日志记录
            return false;
        }
    }
    
    @Override
    public boolean sendChapterApplicationNotification(String openid, String applicantName, String chapterName) {
        try {
            // 构建模板消息数据
            Map<String, Object> data = new HashMap<>();
            
            // 根据模板格式：{{thing1.DATA}}是申请人的姓名，{{thing3.DATA}}是分会名称
            Map<String, Object> thing1 = new HashMap<>();
            thing1.put("value", applicantName);
            data.put("thing1", thing1);
            
            Map<String, Object> thing3 = new HashMap<>();
            thing3.put("value", chapterName);
            data.put("thing3", thing3);
            
            return sendTemplateMessage(openid, chapterReviewSuccessTemplateId, data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendCreateChapterAdminNotification(String openid, String adminName, String chapterName) {
        try {
            Map<String, Object> data = new HashMap<>();
            // thing2: 管理员中文名
            Map<String, Object> thing2 = new HashMap<>();
            thing2.put("value", adminName);
            data.put("thing2", thing2);
            // thing1: 分会名称
            Map<String, Object> thing1 = new HashMap<>();
            thing1.put("value", chapterName);
            data.put("thing1", thing1);
            return sendTemplateMessage(openid, createChapterAdminTemplateId, data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
