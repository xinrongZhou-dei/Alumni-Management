package com.alumni.back.chapter;

import com.alumni.back.Entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChapterMapper {
    int insert(Chapter chapter);
    List<Chapter> selectAll();
    List<Chapter> selectMyChapters(String alumniId);
    List<Map<String, Object>> selectReviewList();
    void updateReviewStatus(List<Integer> tagIds, String status);
    String selectAlumnusTagIds(String alumniId);
    int updateAlumnusTagIds(String alumniId, String tagIds);
    List<Map<String, Object>> selectChaptersByTagIds(List<Integer> tagIds);
    List<Map<String, Object>> selectAllWithMap();
    List<Map<String, Object>> selectHotChapters();
    int updateChapterTotal(Integer tagId, Integer increment);
    int initChapterTotal(Integer tagId);
    Chapter selectByTagId(Integer tagId);
    int deleteByTagId(Integer tagId);
    int removeTagIdFromAllAlumni(Integer tagId);
    @Select("SELECT COUNT(*) FROM chapter WHERE status = 'pending'")
    int countPendingChapters();
} 