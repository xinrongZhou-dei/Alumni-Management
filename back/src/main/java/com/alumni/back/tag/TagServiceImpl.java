package com.alumni.back.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import com.alumni.back.util.DatabaseWebSocketHandler;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private DatabaseWebSocketHandler databaseWebSocketHandler;

    @Override
    public Map<String, Object> tagList(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * pageSize;
        java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> tagRows = tagMapper.selectTagList(offset, pageSize);
        int total = tagMapper.countTagList();
        for (Map<String, Object> tag : tagRows) {
            int tagId = (int) tag.get("id");
            int usage = tagMapper.countAlumniByTagId(tagId);
            tag.put("usage_count", usage);
            rows.add(tag);
        }
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", Map.of(
            "rows", rows,
            "total", total,
            "hasMore", offset + pageSize < total
        ));
        return result;
    }

    @Override
    public Map<String, Object> tagAdd(Map<String, String> req) {
        Map<String, Object> result = new HashMap<>();
        try {
            int inserted = tagMapper.insertTag(req.get("name"), req.get("color"));
            if (inserted > 0) {
                result.put("code", 0);
                result.put("msg", "添加成功");
                databaseWebSocketHandler.sendUpdateMessage("tag changed");
            } else {
                result.put("code", 1);
                result.put("msg", "添加失败");
            }
        } catch (DuplicateKeyException e) {
            result.put("code", 1);
            result.put("msg", "标签名称已存在");
        }
        return result;
    }

    @Override
    public Map<String, Object> tagDelete(Map<String, Integer> req) {
        Map<String, Object> result = new HashMap<>();
        int deleted = tagMapper.deleteTagById(req.get("id"));
        if (deleted > 0) {
            result.put("code", 0);
            result.put("msg", "删除成功");
            databaseWebSocketHandler.sendUpdateMessage("tag changed");
        } else {
            result.put("code", 1);
            result.put("msg", "删除失败");
        }
        return result;
    }

    @Override
    public Map<String, Object> updateTag(Map<String, Object> req) {
        Map<String, Object> result = new HashMap<>();
        int updated = tagMapper.updateTag((Integer) req.get("id"), (String) req.get("name"), (String) req.get("color"));
        if (updated > 0) {
            result.put("code", 0);
            result.put("msg", "修改成功");
            databaseWebSocketHandler.sendUpdateMessage("tag changed");
        } else {
            result.put("code", 1);
            result.put("msg", "修改失败");
        }
        return result;
    }
} 