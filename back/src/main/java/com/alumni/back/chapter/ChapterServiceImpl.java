package com.alumni.back.chapter;

import com.alumni.back.Entity.Chapter;
import com.alumni.back.Entity.ChapterApplication;
import com.alumni.back.register.RegisterMapper;
import com.alumni.back.tag.TagMapper;
import com.alumni.back.wechat.WeChatMessageService;
import com.alumni.back.util.DatabaseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChapterServiceImpl implements ChapterService {

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ChapterApplicationService chapterApplicationService;

    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private WeChatMessageService weChatMessageService;

    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;

    @Override
    @Transactional
    public boolean createChapter(String alumniId, String branchName) {
        // 1. 创建标签
        tagMapper.insertTag(branchName, "#e6a23c");
        Integer tagId = tagMapper.selectTagIdByName(branchName);
        if (tagId == null || tagId <= 0) {
            return false;
        }

        // 2. 创建分会
        Chapter chapter = new Chapter();
        chapter.setTagId(tagId);
        chapter.setAlumniId(alumniId);
        chapter.setStatus("pending");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        chapter.setCreateTime(now);
        chapter.setUpdateTime(now);
        
        boolean success = chapterMapper.insert(chapter) > 0;
        if (success) {
            // 3. 初始化分会人数为0（因为还没审核通过）
            chapterMapper.initChapterTotal(tagId);
            // 发送WebSocket消息
            databaseWebSocketHandler.sendUpdateMessage("chapter changed");
        }
        return success;
    }

    @Override
    public List<Chapter> getChapters() {
        return chapterMapper.selectAll();
    }

    @Override
    public List<Chapter> getMyChapters(String alumniId) {
        return chapterMapper.selectMyChapters(alumniId);
    }

    @Override
    public List<Map<String, Object>> getReviewList() {
        return chapterMapper.selectReviewList();
    }

    @Override
    @Transactional
    public void updateReviewStatus(List<Integer> tagIds, String status) {
        chapterMapper.updateReviewStatus(tagIds, status);
        if ("approved".equals(status)) {
            // 查询所有分会负责人
            List<Map<String, Object>> chapters = chapterMapper.selectChaptersByTagIds(tagIds);
            for (Map<String, Object> chapter : chapters) {
                String alumniId = String.valueOf(chapter.get("alumni_id"));
                Integer tagId = (Integer) chapter.get("tag_id");
                // 查询当前tag_ids
                String tagIdsStr = chapterMapper.selectAlumnusTagIds(alumniId);
                boolean needUpdate = true;
                if (tagIdsStr != null && !tagIdsStr.isEmpty()) {
                    String[] arr = tagIdsStr.split(",");
                    for (String id : arr) {
                        if (id.trim().equals(String.valueOf(tagId))) {
                            needUpdate = false;
                            break;
                        }
                    }
                } else {
                    tagIdsStr = "";
                }
                if (needUpdate) {
                    String newTagIds = tagIdsStr.isEmpty() ? String.valueOf(tagId) : tagIdsStr + "," + tagId;
                    chapterMapper.updateAlumnusTagIds(alumniId, newTagIds);
                    // 分会审核通过时，创建人自动加入，人数+1
                    chapterMapper.updateChapterTotal(tagId, 1);
                }
                // 发送服务号通知
                try {
                    var applicant = registerMapper.findByAlumniId(alumniId);
                    if (applicant != null && applicant.getOpenid() != null && !applicant.getOpenid().isEmpty()) {
                        String chapterName = tagMapper.selectTagNameById(tagId);
                        if (chapterName != null) {
                            weChatMessageService.sendCreateChapterAdminNotification(
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
        }
        // 发送WebSocket消息
        databaseWebSocketHandler.sendUpdateMessage("chapter changed");
    }

    @Override
    @Transactional
    public boolean joinChapter(String alumniId, Integer tagId) {
        // 查询当前tag_ids
        String tagIds = chapterMapper.selectAlumnusTagIds(alumniId);
        if (tagIds == null || tagIds.isEmpty()) {
            boolean success = chapterMapper.updateAlumnusTagIds(alumniId, String.valueOf(tagId)) > 0;
            if (success) {
                // 加入分会成功，人数+1
                chapterMapper.updateChapterTotal(tagId, 1);
                // 发送WebSocket消息
                databaseWebSocketHandler.sendUpdateMessage("chapter changed");
            }
            return success;
        }
        String[] arr = tagIds.split(",");
        for (String id : arr) {
            if (id.trim().equals(String.valueOf(tagId))) {
                return true; // 已经加入
            }
        }
        String newTagIds = tagIds + "," + tagId;
        boolean success = chapterMapper.updateAlumnusTagIds(alumniId, newTagIds) > 0;
        if (success) {
            // 加入分会成功，人数+1
            chapterMapper.updateChapterTotal(tagId, 1);
            // 发送WebSocket消息
            databaseWebSocketHandler.sendUpdateMessage("chapter changed");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean leaveChapter(String alumniId, Integer tagId) {
        // 查询当前tag_ids
        String tagIds = chapterMapper.selectAlumnusTagIds(alumniId);
        if (tagIds == null || tagIds.isEmpty()) {
            return true; // 本来就没有加入
        }
        
        String[] arr = tagIds.split(",");
        boolean found = false;
        StringBuilder newTagIds = new StringBuilder();
        
        for (String id : arr) {
            String trimmedId = id.trim();
            if (!trimmedId.equals(String.valueOf(tagId))) {
                if (newTagIds.length() > 0) {
                    newTagIds.append(",");
                }
                newTagIds.append(trimmedId);
            } else {
                found = true;
            }
        }
        
        if (!found) {
            return true; // 本来就没有加入该分会
        }
        
        String resultTagIds = newTagIds.toString();
        boolean success = chapterMapper.updateAlumnusTagIds(alumniId, resultTagIds) > 0;
        if (success) {
            // 退出分会成功，人数-1
            chapterMapper.updateChapterTotal(tagId, -1);
            // 发送WebSocket消息
            databaseWebSocketHandler.sendUpdateMessage("chapter changed");
        }
        return success;
    }

    @Override
    public List<Map<String, Object>> getChaptersWithMap() {
        return chapterMapper.selectAllWithMap();
    }

    @Override
    public List<Map<String, Object>> getHotChapters() {
        return chapterMapper.selectHotChapters();
    }

    @Override
    public String getAlumnusTagIds(String alumniId) {
        return chapterMapper.selectAlumnusTagIds(alumniId);
    }

    @Override
    public Map<String, String> getApplicationStatus(String alumniId, List<Integer> tagIds) {
        Map<String, String> statusMap = new HashMap<>();
        for (Integer tagId : tagIds) {
            ChapterApplication latest = chapterApplicationService.getLatestApplication(tagId, alumniId);
            if (latest != null) {
                statusMap.put(String.valueOf(tagId), latest.getStatus());
            }
        }
        return statusMap;
    }

    @Override
    public boolean isChapterOwner(String alumniId, Integer tagId) {
        Chapter chapter = chapterMapper.selectByTagId(tagId);
        return chapter != null && alumniId.equals(chapter.getAlumniId());
    }

    @Override
    @Transactional
    public boolean dissolveChapter(Integer tagId) {
        // 1. 删除chapter_application表中该分会的所有申请
        chapterApplicationService.deleteByTagId(tagId);
        // 2. 删除chapter表中该分会
        chapterMapper.deleteByTagId(tagId);
        // 3. 删除所有校友tag_ids中的分会id
        chapterMapper.removeTagIdFromAllAlumni(tagId);
        // 4. 删除tag表中该分会的标签
        tagMapper.deleteById(tagId);
        // 发送WebSocket消息
        databaseWebSocketHandler.sendUpdateMessage("chapter changed");
        return true;
    }

    @Override
    public int getPendingChapterCount() {
        return chapterMapper.countPendingChapters();
    }
} 