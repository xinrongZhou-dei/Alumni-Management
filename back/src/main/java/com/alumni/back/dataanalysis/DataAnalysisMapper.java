package com.alumni.back.dataanalysis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.alumni.back.vo.DataAnalysisVO;
import com.alumni.back.vo.DataAnalysisVO.YearDistribution;
import com.alumni.back.vo.DataAnalysisVO.SchoolDistribution;
import com.alumni.back.vo.DataAnalysisVO.IndustryDistribution;
import java.util.List;
import java.util.Map;

@Mapper
public interface DataAnalysisMapper {
    @Select("SELECT COUNT(*) FROM alumnus")
    Integer getTotalAlumni();

    @Select("SELECT COUNT(*) FROM alumnus WHERE year_left = #{currentYear}")
    Integer getCurrentYearGraduates(@Param("currentYear") Integer currentYear);

    @Select("SELECT id FROM tag WHERE name = '活跃校友'")
    String getActiveAlumniTagId();

    @Select("SELECT COUNT(*) FROM alumnus WHERE FIND_IN_SET(#{tagId}, tag_ids)")
    Integer getActiveAlumniCount(@Param("tagId") String tagId);

    @Select("SELECT COUNT(*) FROM activity_registration")
    Integer getCurrentYearActivity();

    @Select("SELECT year_left as year, COUNT(*) as count " +
            "FROM alumnus " +
            "WHERE year_left IS NOT NULL " +
            "GROUP BY year_left " +
            "ORDER BY count DESC " +
            "LIMIT 5")
    List<YearDistribution> getGraduationYearDistribution();

    @Select("SELECT ycyw_schools_attended as name, COUNT(*) as count " +
            "FROM alumnus " +
            "WHERE ycyw_schools_attended IS NOT NULL AND ycyw_schools_attended != '' " +
            "GROUP BY ycyw_schools_attended " +
            "ORDER BY count DESC " +
            "LIMIT 5")
    List<SchoolDistribution> getSchoolDistribution();

    @Select("SELECT region, COUNT(*) as count FROM alumnus WHERE region IS NOT NULL GROUP BY region")
    List<Map<String, Object>> getRegionDistribution();

    @Select("SELECT industry as name, COUNT(*) as value " +
            "FROM carrer_information " +
            "WHERE industry IS NOT NULL AND industry != '' " +
            "GROUP BY industry " +
            "ORDER BY value DESC " +
            "LIMIT 5")
    List<IndustryDistribution> getIndustryDistribution();

    // TODO: 添加数据统计相关的方法
} 