package com.alumni.back.alumnisearch;

import com.alumni.back.dto.AlumniSearchRequest;
import com.alumni.back.vo.AlumniSearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alumni.back.util.DatabaseWebSocketHandler;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlumniSearchServiceImpl implements AlumniSearchService {
    
    @Autowired
    private AlumniSearchMapper alumniSearchMapper;
    
    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;
    
    @Override
    public List<AlumniSearchResultVO> searchAlumni(AlumniSearchRequest req) {
        List<AlumniSearchResultVO> results = alumniSearchMapper.searchAlumni(req);
        // 根据用户角色决定是否脱敏，校友需要脱敏，管理员不需要
        String userRole = req.getUserRole();
        if ("alumni".equals(userRole)) {
            return desensitizeAlumniData(results);
        } else {
            // 管理员直接返回原始数据，不受校友显示设置限制
            return results;
        }
    }
    
    @Override
    public int countAlumni(AlumniSearchRequest req) {
        return alumniSearchMapper.countAlumni(req);
    }
    
    @Override
    public List<AlumniSearchResultVO> searchAlumniByChapter(AlumniSearchRequest req) {
        List<AlumniSearchResultVO> results = alumniSearchMapper.searchAlumniByChapter(req);
        // 根据用户角色决定是否脱敏，校友需要脱敏，管理员不需要
        String userRole = req.getUserRole();
        if ("alumni".equals(userRole)) {
            return desensitizeAlumniData(results);
        } else {
            // 管理员直接返回原始数据，不受校友显示设置限制
            return results;
        }
    }
    
    @Override
    public int countAlumniByChapter(AlumniSearchRequest req) {
        return alumniSearchMapper.countAlumniByChapter(req);
    }
    
    /**
     * 对校友数据进行脱敏处理
     * 根据校友的显示设置，将不显示的信息替换为"***"
     * 注意：只有校友用户才需要脱敏，管理员不受此限制
     */
    private List<AlumniSearchResultVO> desensitizeAlumniData(List<AlumniSearchResultVO> alumniList) {
        return alumniList.stream().map(alumni -> {
            AlumniSearchResultVO desensitized = new AlumniSearchResultVO();
            
            // 复制基本信息
            desensitized.setAlumniId(alumni.getAlumniId());
            desensitized.setFirstName(alumni.getFirstName());
            desensitized.setLastName(alumni.getLastName());
            desensitized.setEmail(alumni.getEmail());
            desensitized.setYcywSchoolsAttended(alumni.getYcywSchoolsAttended());
            desensitized.setYearLeft(alumni.getYearLeft());
            desensitized.setRegion(alumni.getRegion());
            
            // 根据显示设置进行脱敏处理
            // 中文姓名 - 必展示字段，不应该被脱敏
            desensitized.setChineseName(alumni.getChineseName());
            
            // 离校年份 - 如果不展示或为空，设为null
            if (alumni.getShowYearLeft() != null && !alumni.getShowYearLeft()) {
                desensitized.setYearLeft(null);
            } else {
                desensitized.setYearLeft(alumni.getYearLeft());
            }
            
            // 大学信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowUniversity() != null && !alumni.getShowUniversity()) {
                desensitized.setUniversity("***");
            } else {
                desensitized.setUniversity(alumni.getUniversity() != null && !alumni.getUniversity().trim().isEmpty() ? alumni.getUniversity() : "***");
            }
            
            // 专业信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowMajor() != null && !alumni.getShowMajor()) {
                desensitized.setMajor("***");
            } else {
                desensitized.setMajor(alumni.getMajor() != null && !alumni.getMajor().trim().isEmpty() ? alumni.getMajor() : "***");
            }
            
            // 公司信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowCompany() != null && !alumni.getShowCompany()) {
                desensitized.setCompany("***");
            } else {
                desensitized.setCompany(alumni.getCompany() != null && !alumni.getCompany().trim().isEmpty() ? alumni.getCompany() : "***");
            }
            
            // 职位信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowJobTitle() != null && !alumni.getShowJobTitle()) {
                desensitized.setJobTitle("***");
            } else {
                desensitized.setJobTitle(alumni.getJobTitle() != null && !alumni.getJobTitle().trim().isEmpty() ? alumni.getJobTitle() : "***");
            }
            
            // 行业信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowIndustry() != null && !alumni.getShowIndustry()) {
                desensitized.setIndustry("***");
            } else {
                desensitized.setIndustry(alumni.getIndustry() != null && !alumni.getIndustry().trim().isEmpty() ? alumni.getIndustry() : "***");
            }
            
            // 国家地区信息 - 如果不展示或为空，显示"***"
            if (alumni.getShowCountryRegion() != null && !alumni.getShowCountryRegion()) {
                desensitized.setCountryRegion("***");
            } else {
                desensitized.setCountryRegion(alumni.getCountryRegion() != null && !alumni.getCountryRegion().trim().isEmpty() ? alumni.getCountryRegion() : "***");
            }
            
            // 复制显示设置（前端可能还需要用到）
            desensitized.setShowYearLeft(alumni.getShowYearLeft());
            desensitized.setShowUniversity(alumni.getShowUniversity());
            desensitized.setShowMajor(alumni.getShowMajor());
            desensitized.setShowCompany(alumni.getShowCompany());
            desensitized.setShowJobTitle(alumni.getShowJobTitle());
            desensitized.setShowIndustry(alumni.getShowIndustry());
            desensitized.setShowCountryRegion(alumni.getShowCountryRegion());
            
            return desensitized;
        }).collect(Collectors.toList());
    }
} 