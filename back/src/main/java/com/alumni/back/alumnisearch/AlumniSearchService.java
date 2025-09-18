package com.alumni.back.alumnisearch;

import com.alumni.back.dto.AlumniSearchRequest;
import com.alumni.back.vo.AlumniSearchResultVO;

import java.util.List;

public interface AlumniSearchService {
    List<AlumniSearchResultVO> searchAlumni(AlumniSearchRequest req);
    int countAlumni(AlumniSearchRequest req);
    List<AlumniSearchResultVO> searchAlumniByChapter(AlumniSearchRequest req);
    int countAlumniByChapter(AlumniSearchRequest req);
} 