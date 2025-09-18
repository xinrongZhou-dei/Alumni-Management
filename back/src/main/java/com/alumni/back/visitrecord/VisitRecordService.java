package com.alumni.back.visitrecord;

import com.alumni.back.Entity.VisitRecord;
import com.alumni.back.Entity.VisitRecord.VisitStatus;
import java.util.List;

public interface VisitRecordService {
    void createVisitRecord(VisitRecord visitRecord);
    List<VisitRecord> getVisitRecordList(String school, String status, String visitDay, int pageNum, int pageSize);
    List<VisitRecord> getVisitRecordListByAdminPermission(String school, String status, String visitDay, int pageNum, int pageSize, String accessibleSchools);
    int getVisitRecordCount(String school, String status, String visitDay);
    int getVisitRecordCountByAdminPermission(String school, String status, String visitDay, String accessibleSchools);
    VisitRecord getVisitRecordDetail(String visitUUID);
    void updateVisitStatus(String visitUUID, VisitStatus status, String remark);
    void batchUpdateVisitStatus(List<String> visitUUIDs, VisitStatus status, String remark);
    void updateVisitRecord(VisitRecord visitRecord);
    List<String> getAccessibleSchools(String accessibleSchools);
    int getPendingVisitRecordCount();
} 