package com.alumni.back.wechat;

import java.util.Map;

public interface WeChatMessageService {
    /**
     * 发送微信模板消息
     * @param openid 用户openid
     * @param templateId 模板ID
     * @param data 模板消息内容
     * @return 是否发送成功
     */
    boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data);
    
    /**
     * 发送分会申请审核通知
     * @param openid 申请人openid
     * @param applicantName 申请人姓名
     * @param chapterName 分会名称
     * @return 是否发送成功
     */
    boolean sendChapterApplicationNotification(String openid, String applicantName, String chapterName);

    /**
     * 发送创建分会管理员审核通过通知
     * @param openid 管理员openid
     * @param adminName 管理员中文名
     * @param chapterName 分会名称
     * @return 是否发送成功
     */
    boolean sendCreateChapterAdminNotification(String openid, String adminName, String chapterName);
}
