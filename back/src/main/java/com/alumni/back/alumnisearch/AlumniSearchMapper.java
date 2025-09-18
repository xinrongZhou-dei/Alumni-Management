package com.alumni.back.alumnisearch;

import com.alumni.back.dto.AlumniSearchRequest;
import com.alumni.back.vo.AlumniSearchResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlumniSearchMapper {
    List<AlumniSearchResultVO> searchAlumni(@Param("req") AlumniSearchRequest req);
    int countAlumni(@Param("req") AlumniSearchRequest req);
    List<AlumniSearchResultVO> searchAlumniByChapter(@Param("req") AlumniSearchRequest req);
    int countAlumniByChapter(@Param("req") AlumniSearchRequest req);
} 