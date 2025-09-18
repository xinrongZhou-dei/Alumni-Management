package com.alumni.back.chapter;

import com.alumni.back.Entity.Chapter;
import java.util.List;
import java.util.Map;

public interface ChapterService {
    boolean createChapter(String alumniId, String branchName);
    List<Chapter> getChapters();
    List<Chapter> getMyChapters(String alumniId);
    List<Map<String, Object>> getReviewList();
    void updateReviewStatus(List<Integer> tagIds, String status);
    boolean joinChapter(String alumniId, Integer tagId);
    boolean leaveChapter(String alumniId, Integer tagId);
    List<Map<String, Object>> getChaptersWithMap();
    List<Map<String, Object>> getHotChapters();
    String getAlumnusTagIds(String alumniId);
    Map<String, String> getApplicationStatus(String alumniId, List<Integer> tagIds);
    boolean isChapterOwner(String alumniId, Integer tagId);
    boolean dissolveChapter(Integer tagId);
    int getPendingChapterCount();
} 