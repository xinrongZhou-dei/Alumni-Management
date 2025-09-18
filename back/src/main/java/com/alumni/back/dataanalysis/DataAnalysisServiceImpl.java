package com.alumni.back.dataanalysis;

import com.alumni.back.vo.DataAnalysisVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {
    
    @Autowired
    private DataAnalysisMapper dataAnalysisMapper;
    
    @Override
    public DataAnalysisVO getDataAnalysis() {
        DataAnalysisVO vo = new DataAnalysisVO();
        
        // 获取总校友数
        vo.setTotalAlumni(dataAnalysisMapper.getTotalAlumni());
        
        // 获取本年度毕业生数
        int currentYear = LocalDate.now().getYear();
        vo.setCurrentYearGraduates(dataAnalysisMapper.getCurrentYearGraduates(currentYear));
        
        // 获取活跃校友数
        String activeAlumniTagId = dataAnalysisMapper.getActiveAlumniTagId();
        if (activeAlumniTagId != null) {
            vo.setActiveAlumni(dataAnalysisMapper.getActiveAlumniCount(activeAlumniTagId));
        } else {
            vo.setActiveAlumni(0);
        }
        
        // 获取活动参与总数
        vo.setCurrentYearActivity(dataAnalysisMapper.getCurrentYearActivity());
        
        // 获取毕业年份分布统计
        vo.setGraduationYears(dataAnalysisMapper.getGraduationYearDistribution());
        
        // 获取YCYW Schools分布
        vo.setSchools(dataAnalysisMapper.getSchoolDistribution());
        
        // 获取地理分布统计
        List<Map<String, Object>> regionStats = dataAnalysisMapper.getRegionDistribution();
        
        DataAnalysisVO.LocationDistribution locationDistribution = new DataAnalysisVO.LocationDistribution();
        // 初始化默认值
        locationDistribution.setMainland(0);
        locationDistribution.setHongKong(0);
        locationDistribution.setOverseas(0);
        
        if (regionStats != null) {
            for (Map<String, Object> stat : regionStats) {
                String region = (String) stat.get("region");
                Integer count = ((Number) stat.get("count")).intValue();
                switch (region) {
                    case "大陆":
                        locationDistribution.setMainland(count);
                        break;
                    case "香港":
                        locationDistribution.setHongKong(count);
                        break;
                    case "海外":
                        locationDistribution.setOverseas(count);
                        break;
                }
            }
        }
        vo.setLocationDistribution(locationDistribution);
        
        // 获取工作领域分布统计
        vo.setIndustries(dataAnalysisMapper.getIndustryDistribution());
        
        return vo;
    }
} 