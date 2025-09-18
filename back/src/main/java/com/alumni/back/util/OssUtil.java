package com.alumni.back.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.UUID;

@Component
public class OssUtil {
    private static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com"; // 北京地域
    private static final String ACCESS_KEY_ID = "xxxxxxxxxxxxxxxxxxxxxxx";
    private static final String ACCESS_KEY_SECRET = "xxxxxxxxxxxxxxxxxxxxxx";
    private static final String BUCKET_NAME = "********"; // 你的实际bucket名
    private static final String BUCKET_DOMAIN = "yourbucketdomain";

    public String upload(InputStream inputStream, String originalFileName) {
        String ext = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String objectName = "uploads/" + UUID.randomUUID().toString().replace("-", "") + ext;
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        try {
            ossClient.putObject(new PutObjectRequest(BUCKET_NAME, objectName, inputStream));
        } finally {
            ossClient.shutdown();
        }
        return BUCKET_DOMAIN + objectName;
    }
} 