package com.alumni.back.visitrecord;

import com.alumni.back.Entity.VisitRecord;
import com.alumni.back.Entity.VisitRecord.VisitStatus;
import com.alumni.back.register.RegisterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alumni.back.util.DatabaseWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.alumni.back.wechat.WeChatMessageService;
import com.alumni.back.Entity.Alumni;

@Service
public class VisitRecordServiceImpl implements VisitRecordService {
    @Autowired
    private VisitRecordMapper visitRecordMapper;
    
    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeChatMessageService weChatMessageService;
    @Autowired
    private RegisterService registerService;

    @Override
    @Transactional
    public void createVisitRecord(VisitRecord visitRecord) {
        visitRecord.setVisitUUID(UUID.randomUUID().toString());
        visitRecord.setStatus(VisitStatus.PENDING);
        visitRecordMapper.createVisitRecord(visitRecord);
        
        // 发送WebSocket消息
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "VISIT_RECORD_CREATED");
            message.put("payload", visitRecord);
            databaseWebSocketHandler.sendUpdateMessage(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<VisitRecord> getVisitRecordList(String school, String status, String visitDay, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return visitRecordMapper.getVisitRecordList(school, status, visitDay, offset, pageSize);
    }

    @Override
    public List<VisitRecord> getVisitRecordListByAdminPermission(String school, String status, String visitDay, int pageNum, int pageSize, String accessibleSchools) {
        int offset = (pageNum - 1) * pageSize;
        List<String> schools = getAccessibleSchools(accessibleSchools);
        return visitRecordMapper.getVisitRecordListByAdminPermission(school, status, visitDay, offset, pageSize, schools);
    }

    @Override
    public int getVisitRecordCount(String school, String status, String visitDay) {
        return visitRecordMapper.getVisitRecordCount(school, status, visitDay);
    }

    @Override
    public int getVisitRecordCountByAdminPermission(String school, String status, String visitDay, String accessibleSchools) {
        List<String> schools = getAccessibleSchools(accessibleSchools);
        return visitRecordMapper.getVisitRecordCountByAdminPermission(school, status, visitDay, schools);
    }

    @Override
    public VisitRecord getVisitRecordDetail(String visitUUID) {
        return visitRecordMapper.getVisitRecordDetail(visitUUID);
    }

    @Override
    @Transactional
    public void updateVisitStatus(String visitUUID, VisitStatus status, String remark) {
        visitRecordMapper.updateVisitStatus(visitUUID, status.name(), remark);
        
        // 发送WebSocket消息
        try {
            VisitRecord record = visitRecordMapper.getVisitRecordDetail(visitUUID);
            Map<String, Object> message = new HashMap<>();
            message.put("type", "VISIT_RECORD_UPDATED");
            message.put("payload", record);
            databaseWebSocketHandler.sendUpdateMessage(objectMapper.writeValueAsString(message));

            // 审核通过时发送服务号通知
            if (status == VisitStatus.APPROVED && record != null) {
                // 获取校友openid
                String alumniId = record.getAlumniId();
                Alumni alumni = registerService.findByAlumniId(alumniId);
                if (alumni != null && alumni.getOpenid() != null && !alumni.getOpenid().isEmpty()) {
                    String openid = alumni.getOpenid();
                    String templateId = "xllaeAEEW6SCHFw6Qyey2F4RBI-YG9Yb5eIdCnq4EB4";
                    Map<String, Object> data = new HashMap<>();
                    Map<String, Object> thing2 = new HashMap<>();
                    thing2.put("value", record.getChineseName());
                    data.put("thing2", thing2);
                    Map<String, Object> thing1 = new HashMap<>();
                    thing1.put("value", record.getVisitSchool());
                    data.put("thing1", thing1);
                    weChatMessageService.sendTemplateMessage(openid, templateId, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void batchUpdateVisitStatus(List<String> visitUUIDs, VisitStatus status, String remark) {
        visitRecordMapper.batchUpdateVisitStatus(visitUUIDs, status.name(), remark);
        
        // 发送WebSocket消息
        try {
            for (String visitUUID : visitUUIDs) {
                VisitRecord record = visitRecordMapper.getVisitRecordDetail(visitUUID);
                Map<String, Object> message = new HashMap<>();
                message.put("type", "VISIT_RECORD_UPDATED");
                message.put("payload", record);
                databaseWebSocketHandler.sendUpdateMessage(objectMapper.writeValueAsString(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void updateVisitRecord(VisitRecord visitRecord) {
        // 检查预约记录是否存在
        VisitRecord existingRecord = visitRecordMapper.getVisitRecordDetail(visitRecord.getVisitUUID());
        if (existingRecord == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 检查预约状态是否为待审核
        if (existingRecord.getStatus() != VisitStatus.PENDING) {
            throw new RuntimeException("只能修改待审核的预约记录");
        }
        
        // 更新预约记录
        visitRecordMapper.updateVisitRecord(visitRecord);
        
        // 发送WebSocket消息
        try {
            VisitRecord updatedRecord = visitRecordMapper.getVisitRecordDetail(visitRecord.getVisitUUID());
            Map<String, Object> message = new HashMap<>();
            message.put("type", "VISIT_RECORD_UPDATED");
            message.put("payload", updatedRecord);
            databaseWebSocketHandler.sendUpdateMessage(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getAccessibleSchools(String accessibleSchools) {
        if (accessibleSchools == null || accessibleSchools.trim().isEmpty()) {
            // 如果管理员没有设置可访问学校，返回所有可能的学校代码
            return Arrays.asList(
                "YCIS_HK", "YCIS_BJ", "YCIS_CQ", "YCIS_QD", "YCIS_SH",
                "YWIES_BJ", "YWIES_GZ", "YWIES_SH_GB", "YWIES_SH_LG", "YWIES_TX", "YWIES_YT"
            );
        }
        
        // 将权限管理中的学校名称转换为预约记录中的学校代码
        List<String> schoolNames = Arrays.stream(accessibleSchools.split(","))
                .map(String::trim)
                .filter(school -> !school.isEmpty())
                .collect(Collectors.toList());
        
        return schoolNames.stream()
                .map(this::convertSchoolNameToCode)
                .filter(code -> code != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 将学校名称转换为学校代码
     */
    private String convertSchoolNameToCode(String schoolName) {
        switch (schoolName) {
            case "YCIS Hong Kong":
                return "YCIS_HK";
            case "YCIS Beijing":
                return "YCIS_BJ";
            case "YCIS Chongqing":
                return "YCIS_CQ";
            case "YCIS Qingdao":
                return "YCIS_QD";
            case "YCIS Shanghai":
                return "YCIS_SH";
            case "YWIES Beijing":
                return "YWIES_BJ";
            case "YWIES Guangzhou":
                return "YWIES_GZ";
            case "YWIES Shanghai Gubei":
                return "YWIES_SH_GB";
            case "YWIES Shanghai Lingang":
                return "YWIES_SH_LG";
            case "YWIES Tongxiang":
                return "YWIES_TX";
            case "YWIES Yantai":
                return "YWIES_YT";
            default:
                // 如果已经是代码格式，直接返回
                if (schoolName.matches("^[A-Z]+_[A-Z]+$")) {
                    return schoolName;
                }
                return null;
        }
    }

    @Override
    public int getPendingVisitRecordCount() {
        return visitRecordMapper.countPendingVisitRecords();
    }
} 