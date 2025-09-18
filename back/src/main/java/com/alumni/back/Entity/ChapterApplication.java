package com.alumni.back.Entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChapterApplication {
    private String id;                    // UUID主键
    private Integer tagId;                // 分会标签ID
    private String alumniId;              // 申请人校友ID
    private String status;                // 申请状态：PENDING、APPROVED、REJECTED
    private LocalDateTime applyTime;      // 申请时间
    private LocalDateTime reviewTime;     // 审核时间
} 