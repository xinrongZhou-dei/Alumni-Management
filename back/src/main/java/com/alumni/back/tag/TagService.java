package com.alumni.back.tag;

import java.util.Map;

public interface TagService {
    Map<String, Object> tagList(int page, int pageSize);
    Map<String, Object> tagAdd(Map<String, String> req);
    Map<String, Object> tagDelete(Map<String, Integer> req);
    Map<String, Object> updateTag(Map<String, Object> req);
} 