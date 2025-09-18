package com.alumni.back.chapter;

import com.alumni.back.Entity.ChapterApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChapterApplicationMapper {
    int insert(ChapterApplication application);
    int updateById(ChapterApplication application);
    ChapterApplication selectById(String id);
    List<Map<String, Object>> selectAllApplications();
    List<Map<String, Object>> selectMyApplications(String alumniId);
    List<Map<String, Object>> selectChapterApplications(Integer tagId);
    ChapterApplication selectLatestApplication(@Param("tagId") Integer tagId, @Param("alumniId") String alumniId);
    int deleteByTagId(Integer tagId);
    
    // 根据校友ID删除分会申请记录
    @Delete("DELETE FROM chapter_application WHERE alumni_id = #{alumniId}")
    int deleteByAlumniId(@Param("alumniId") String alumniId);

    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM chapter_application WHERE tag_id IN (${tagIds}) AND status = 'PENDING'")
    int countPendingApplicationsByTagIds(@org.apache.ibatis.annotations.Param("tagIds") String tagIds);
} 