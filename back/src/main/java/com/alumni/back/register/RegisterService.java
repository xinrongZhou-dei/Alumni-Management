package com.alumni.back.register;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.TertiaryInformation;
import com.alumni.back.Entity.CareerInformation;
import com.alumni.back.dto.MatchGmiRequest;

import java.util.Map;
import java.util.List;

public interface RegisterService {
    Alumni findByEmail(String email);
    Alumni findByAlumniId(String alumniId);
    Map<String, Object> verifyEmail(String email);
    Map<String, Object> verifyCode(String token, String verifyCode);
    Map<String, Object> setPassword(String token, String password);
    Map<String, Object> setAlumniInfo(String token, Map<String, Object> request);
    Map<String, Object> changePassword(String token, String pastPassword, String newPassword);
    Map<String, Object> getAlumniList(String alumniId, String ycywSchoolsAttended, String chineseName, String email, String contactNumber, String tagIds, java.util.List<Integer> stateList, int page, int pageSize, Map<String, Object> adminPermissionInfo);
    Map<String, Object> getAlumniDetailById(String alumniId);
    Map<String, Object> updateAlumniById(Alumni alumni, List<TertiaryInformation> tertiaryList, List<CareerInformation> careerList);
    boolean isEmailAllowed(String email);
    int deleteTertiaryById(String id);
    int deleteCareerById(String id);
    int deleteAlumniById(String id);
    Map<String, Object> cleanExpiredTokens();
    
    // 删除校友相关的所有数据
    int deleteActivityRegistrationByAlumniId(String alumniId);
    int deleteChapterApplicationByAlumniId(String alumniId);
    int deleteVisitRecordByAlumniId(String alumniId);

    /**
     * 生成唯一的9位学号
     * @return 生成的学号
     */
    String generateUniqueAlumniId();

    int insertAlumni(Alumni alumni);

    List<Alumni> matchGmiData(MatchGmiRequest request);

    void matchAndActivate(String gmiAlumniId, String reviewAlumniId);

    int getPendingAlumniCount();
}
