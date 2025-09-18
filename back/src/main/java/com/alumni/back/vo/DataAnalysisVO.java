package com.alumni.back.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DataAnalysisVO {
    // 基础统计数据
    private Integer totalAlumni;          // 总校友数
    private Integer currentYearGraduates; // 本年度毕业生
    private Integer activeAlumni;         // 活跃校友
    private Integer currentYearActivity;  // 活动参与
    
    // 毕业年份分布（Top5）
    private List<YearDistribution> graduationYears;
    
    // 地理分布
    private LocationDistribution locationDistribution;
    
    // YCYW Schools分布（Top5）
    private List<SchoolDistribution> schools;
    
    // 职业领域分布
    private List<IndustryDistribution> industries;
    
    @Data
    public static class YearDistribution {
        private String year;
        private Integer count;
    }
    
    @Data
    public static class LocationDistribution {
        private Integer mainland;  // 中国大陆
        private Integer hongKong;  // 香港
        private Integer overseas;  // 海外
    }
    
    @Data
    public static class SchoolDistribution {
        private String name;
        private Integer count;
    }
    
    @Data
    public static class IndustryDistribution {
        private String name;
        private Integer value;
    }
} 