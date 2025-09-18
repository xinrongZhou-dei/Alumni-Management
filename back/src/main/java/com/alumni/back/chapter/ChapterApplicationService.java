package com.alumni.back.chapter;

import com.alumni.back.Entity.ChapterApplication;
import java.util.List;
import java.util.Map;

public interface ChapterApplicationService {
    boolean applyForChapter(String alumniId, Integer tagId);
    boolean reviewApplication(String applicationId, String status);
    List<Map<String, Object>> getAllApplications();
    List<Map<String, Object>> getMyApplications(String alumniId);
    List<Map<String, Object>> getChapterApplications(Integer tagId);
    ChapterApplication getLatestApplication(Integer tagId, String alumniId);
    void deleteByTagId(Integer tagId);
    int countPendingApplicationsByTagIds(List<Integer> tagIds);
} 