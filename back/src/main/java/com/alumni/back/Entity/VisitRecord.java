package com.alumni.back.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VisitRecord {
    private String visitUUID;      // 预约记录ID
    private String alumniId;       // 校友ID
    private String chineseName;    // 中文姓名
    private String currentLocation;// 当前位置
    private String contactNumber;  // 联系方式
    private String visitSchool;    // 预约校区
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDay;
    
    private VisitTime visitTime;
    private VisitStatus status;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public enum VisitTime {
        MORNING("上午(9:00-11:30)"),
        AFTERNOON("下午(13:30-16:00)");

        private final String description;

        VisitTime(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum VisitStatus {
        PENDING("待审核"),
        APPROVED("已批准"),
        REJECTED("已拒绝");

        private final String description;

        VisitStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 