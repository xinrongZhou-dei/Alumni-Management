package com.alumni.back.chapter;

import com.alumni.back.Entity.ChapterApplication;
import com.alumni.back.register.RegisterMapper;
import com.alumni.back.tag.TagMapper;
import com.alumni.back.util.DatabaseWebSocketHandler;
import com.alumni.back.wechat.WeChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChapterApplicationServiceImpl implements ChapterApplicationService {

    @Autowired
    private ChapterApplicationMapper chapterApplicationMapper;

    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private RegisterMapper registerMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;
    
    @Autowired
    private WeChatMessageService weChatMessageService;

    @Override
    @Transactional
    public boolean applyForChapter(String alumniId, Integer tagId) {
        // 1. 检查是否已在分会中
        String tagIds = chapterMapper.selectAlumnusTagIds(alumniId);
        if (tagIds != null && !tagIds.isEmpty()) {
            String[] arr = tagIds.split(",");
            for (String id : arr) {
                if (id.trim().equals(String.valueOf(tagId))) {
                    return false; // 已在分会中
                }
            }
        }

        // 2. 检查是否已有待审核的申请
        ChapterApplication latest = getLatestApplication(tagId, alumniId);
        if (latest != null && "PENDING".equals(latest.getStatus())) {
            return false; // 已有待审核申请
        }

        // 3. 创建新申请记录
        ChapterApplication application = new ChapterApplication();
        application.setId(UUID.randomUUID().toString());
        application.setTagId(tagId);
        application.setAlumniId(alumniId);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());

        boolean success = chapterApplicationMapper.insert(application) > 0;
        if (success) {
            // 发送WebSocket消息
            databaseWebSocketHandler.sendUpdateMessage("chapter application created");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean reviewApplication(String applicationId, String status) {
        ChapterApplication application = chapterApplicationMapper.selectById(applicationId);
        if (application == null) {
            return false;
        }

        // 更新申请状态
        application.setStatus(status);
        application.setReviewTime(LocalDateTime.now());
        boolean success = chapterApplicationMapper.updateById(application) > 0;

        // 如果审核通过，执行加入逻辑
        if (success && "APPROVED".equals(status)) {
            // 更新校友tag_ids
            String tagIds = chapterMapper.selectAlumnusTagIds(application.getAlumniId());
            if (tagIds == null || tagIds.isEmpty()) {
                chapterMapper.updateAlumnusTagIds(application.getAlumniId(), String.valueOf(application.getTagId()));
            } else {
                String newTagIds = tagIds + "," + application.getTagId();
                chapterMapper.updateAlumnusTagIds(application.getAlumniId(), newTagIds);
            }
            // 分会人数+1
            chapterMapper.updateChapterTotal(application.getTagId(), 1);
            // 只发送分会审核成功服务号通知
            try {
                var applicant = registerMapper.findByAlumniId(application.getAlumniId());
                if (applicant != null && applicant.getOpenid() != null && !applicant.getOpenid().isEmpty()) {
                    String chapterName = tagMapper.selectTagNameById(application.getTagId());
                    if (chapterName != null) {
                        weChatMessageService.sendChapterApplicationNotification(
                            applicant.getOpenid(),
                            applicant.getChineseName(),
                            chapterName
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (success) {
            // 发送WebSocket消息
            databaseWebSocketHandler.sendUpdateMessage("chapter application reviewed");
        }
        return success;
    }

    @Override
    public List<Map<String, Object>> getAllApplications() {
        return chapterApplicationMapper.selectAllApplications();
    }

    @Override
    public List<Map<String, Object>> getMyApplications(String alumniId) {
        return chapterApplicationMapper.selectMyApplications(alumniId);
    }

    @Override
    public List<Map<String, Object>> getChapterApplications(Integer tagId) {
        return chapterApplicationMapper.selectChapterApplications(tagId);
    }

    @Override
    public ChapterApplication getLatestApplication(Integer tagId, String alumniId) {
        return chapterApplicationMapper.selectLatestApplication(tagId, alumniId);
    }

    @Override
    public void deleteByTagId(Integer tagId) {
        chapterApplicationMapper.deleteByTagId(tagId);
    }

    @Override
    public int countPendingApplicationsByTagIds(List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return 0;
        String tagIdsStr = tagIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        return chapterApplicationMapper.countPendingApplicationsByTagIds(tagIdsStr);
    }
} 