package com.alumni.back;

import com.alumni.back.util.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Autowired
    private OssUtil ossUtil;

    @PostMapping("/oss")
    public Map<String, Object> uploadToOss(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        try {
            String url = ossUtil.upload(file.getInputStream(), file.getOriginalFilename());
            res.put("code", 0);
            res.put("url", url);
            res.put("msg", "上传成功");
        } catch (Exception e) {
            res.put("code", -1);
            res.put("msg", "上传失败: " + e.getMessage());
        }
        return res;
    }
} 