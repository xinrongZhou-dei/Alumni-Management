package com.alumni.back.register;

import com.alumni.back.Entity.Alumni;
import com.alumni.back.Entity.TertiaryInformation;
import com.alumni.back.Entity.CareerInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface RegisterMapper {
    @Select("SELECT * FROM alumnus WHERE email = #{email}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "chineseName", column = "chinese_name"),
        @org.apache.ibatis.annotations.Result(property = "alumniId", column = "alumni_id"),
        @org.apache.ibatis.annotations.Result(property = "salutation", column = "salutation"),
        @org.apache.ibatis.annotations.Result(property = "firstName", column = "first_name"),
        @org.apache.ibatis.annotations.Result(property = "lastName", column = "last_name"),
        @org.apache.ibatis.annotations.Result(property = "email", column = "email"),
        @org.apache.ibatis.annotations.Result(property = "contactNumber", column = "contact_number"),
        @org.apache.ibatis.annotations.Result(property = "wechatId", column = "wechat_id"),
        @org.apache.ibatis.annotations.Result(property = "correspondenceAddress", column = "correspondence_address"),
        @org.apache.ibatis.annotations.Result(property = "currentLocation", column = "current_location"),
        @org.apache.ibatis.annotations.Result(property = "zohoAlumniNumber", column = "zoho_alumni_number"),
        @org.apache.ibatis.annotations.Result(property = "affiliation", column = "affiliation"),
        @org.apache.ibatis.annotations.Result(property = "ycywSchoolsAttended", column = "ycyw_schools_attended"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodStart", column = "study_period_start"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodEnd", column = "study_period_end"),
        @org.apache.ibatis.annotations.Result(property = "lastSchoolDay", column = "last_school_day"),
        @org.apache.ibatis.annotations.Result(property = "yearLeft", column = "year_left"),
        @org.apache.ibatis.annotations.Result(property = "maritalStatus", column = "marital_status"),
        @org.apache.ibatis.annotations.Result(property = "showYearLeft", column = "show_year_left"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryUniversity", column = "show_tertiary_university"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryMajor", column = "show_tertiary_major"),
        @org.apache.ibatis.annotations.Result(property = "showCareerCompany", column = "show_career_company"),
        @org.apache.ibatis.annotations.Result(property = "showJobTitle", column = "show_job_title"),
        @org.apache.ibatis.annotations.Result(property = "showIndustry", column = "show_industry"),
        @org.apache.ibatis.annotations.Result(property = "showCountry", column = "show_country"),
        @org.apache.ibatis.annotations.Result(property = "password", column = "password"),
        @org.apache.ibatis.annotations.Result(property = "updateTime", column = "update_time"),
        @org.apache.ibatis.annotations.Result(property = "role", column = "role"),
        @org.apache.ibatis.annotations.Result(property = "tagIds", column = "tag_ids"),
        @org.apache.ibatis.annotations.Result(property = "region", column = "region"),
        @org.apache.ibatis.annotations.Result(property = "state", column = "state"),
        @org.apache.ibatis.annotations.Result(property = "birthday", column = "birthday"),
        @org.apache.ibatis.annotations.Result(property = "openid", column = "openid")
    })
    Alumni findByEmail(String email);

    @Select("SELECT * FROM alumnus WHERE alumni_id = #{alumniId}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "chineseName", column = "chinese_name"),
        @org.apache.ibatis.annotations.Result(property = "alumniId", column = "alumni_id"),
        @org.apache.ibatis.annotations.Result(property = "salutation", column = "salutation"),
        @org.apache.ibatis.annotations.Result(property = "firstName", column = "first_name"),
        @org.apache.ibatis.annotations.Result(property = "lastName", column = "last_name"),
        @org.apache.ibatis.annotations.Result(property = "email", column = "email"),
        @org.apache.ibatis.annotations.Result(property = "contactNumber", column = "contact_number"),
        @org.apache.ibatis.annotations.Result(property = "wechatId", column = "wechat_id"),
        @org.apache.ibatis.annotations.Result(property = "correspondenceAddress", column = "correspondence_address"),
        @org.apache.ibatis.annotations.Result(property = "currentLocation", column = "current_location"),
        @org.apache.ibatis.annotations.Result(property = "zohoAlumniNumber", column = "zoho_alumni_number"),
        @org.apache.ibatis.annotations.Result(property = "affiliation", column = "affiliation"),
        @org.apache.ibatis.annotations.Result(property = "ycywSchoolsAttended", column = "ycyw_schools_attended"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodStart", column = "study_period_start"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodEnd", column = "study_period_end"),
        @org.apache.ibatis.annotations.Result(property = "lastSchoolDay", column = "last_school_day"),
        @org.apache.ibatis.annotations.Result(property = "yearLeft", column = "year_left"),
        @org.apache.ibatis.annotations.Result(property = "maritalStatus", column = "marital_status"),
        @org.apache.ibatis.annotations.Result(property = "showYearLeft", column = "show_year_left"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryUniversity", column = "show_tertiary_university"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryMajor", column = "show_tertiary_major"),
        @org.apache.ibatis.annotations.Result(property = "showCareerCompany", column = "show_career_company"),
        @org.apache.ibatis.annotations.Result(property = "showJobTitle", column = "show_job_title"),
        @org.apache.ibatis.annotations.Result(property = "showIndustry", column = "show_industry"),
        @org.apache.ibatis.annotations.Result(property = "showCountry", column = "show_country"),
        @org.apache.ibatis.annotations.Result(property = "password", column = "password"),
        @org.apache.ibatis.annotations.Result(property = "updateTime", column = "update_time"),
        @org.apache.ibatis.annotations.Result(property = "role", column = "role"),
        @org.apache.ibatis.annotations.Result(property = "tagIds", column = "tag_ids"),
        @org.apache.ibatis.annotations.Result(property = "region", column = "region"),
        @org.apache.ibatis.annotations.Result(property = "state", column = "state"),
        @org.apache.ibatis.annotations.Result(property = "birthday", column = "birthday"),
        @org.apache.ibatis.annotations.Result(property = "openid", column = "openid")
    })
    Alumni findByAlumniId(String alumniId);

    // 使用XML配置实现动态插入，避免password为null的问题
    int insertAlumni(Alumni alumni);

    @Update("UPDATE alumnus SET password = #{password} WHERE email = #{email}")
    int updatePassword(@Param("email") String email, @Param("password") String password);

    @Update("UPDATE alumnus SET password = #{password} WHERE alumni_id = #{alumniId}")
    int updatePasswordByAlumniId(@Param("alumniId") String alumniId, @Param("password") String password);

    @Update("UPDATE alumnus SET region = #{region} WHERE alumni_id = #{alumniId}")
    int updateRegionByAlumniId(@Param("alumniId") String alumniId, @Param("region") String region);

    @Insert("INSERT INTO tertiary_information (id, alumni_id, university_college, degree, major, graduation_year, country_region) VALUES (#{id}, #{alumniId}, #{universityCollege}, #{degree}, #{major}, #{graduationYear}, #{countryRegion})")
    int insertTertiaryInfo(TertiaryInformation info);

    int insertCareerInfo(CareerInformation info);

    int updateCareerInfo(CareerInformation info);

    // 用XML实现多标签筛选
    java.util.List<Alumni> selectAlumniList(
        @Param("alumniId") String alumniId,
        @Param("ycywSchoolsAttended") String ycywSchoolsAttended,
        @Param("chineseName") String chineseName,
        @Param("email") String email,
        @Param("contactNumber") String contactNumber,
        @Param("tagIds") String tagIds,
        @Param("tagIdsList") java.util.List<String> tagIdsList,
        @Param("stateList") java.util.List<Integer> stateList,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize,
        @Param("permissionLevel") Integer permissionLevel,
        @Param("accessibleSchools") String accessibleSchools,
        @Param("permissionSchool") String permissionSchool
    );
    int countAlumniList(
        @Param("alumniId") String alumniId,
        @Param("ycywSchoolsAttended") String ycywSchoolsAttended,
        @Param("chineseName") String chineseName,
        @Param("email") String email,
        @Param("contactNumber") String contactNumber,
        @Param("tagIds") String tagIds,
        @Param("tagIdsList") java.util.List<String> tagIdsList,
        @Param("stateList") java.util.List<Integer> stateList,
        @Param("permissionLevel") Integer permissionLevel,
        @Param("accessibleSchools") String accessibleSchools,
        @Param("permissionSchool") String permissionSchool
    );

    @Select("SELECT * FROM alumnus WHERE alumni_id = #{alumniId}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "chineseName", column = "chinese_name"),
        @org.apache.ibatis.annotations.Result(property = "alumniId", column = "alumni_id"),
        @org.apache.ibatis.annotations.Result(property = "salutation", column = "salutation"),
        @org.apache.ibatis.annotations.Result(property = "firstName", column = "first_name"),
        @org.apache.ibatis.annotations.Result(property = "lastName", column = "last_name"),
        @org.apache.ibatis.annotations.Result(property = "email", column = "email"),
        @org.apache.ibatis.annotations.Result(property = "contactNumber", column = "contact_number"),
        @org.apache.ibatis.annotations.Result(property = "wechatId", column = "wechat_id"),
        @org.apache.ibatis.annotations.Result(property = "correspondenceAddress", column = "correspondence_address"),
        @org.apache.ibatis.annotations.Result(property = "currentLocation", column = "current_location"),
        @org.apache.ibatis.annotations.Result(property = "zohoAlumniNumber", column = "zoho_alumni_number"),
        @org.apache.ibatis.annotations.Result(property = "affiliation", column = "affiliation"),
        @org.apache.ibatis.annotations.Result(property = "ycywSchoolsAttended", column = "ycyw_schools_attended"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodStart", column = "study_period_start"),
        @org.apache.ibatis.annotations.Result(property = "studyPeriodEnd", column = "study_period_end"),
        @org.apache.ibatis.annotations.Result(property = "lastSchoolDay", column = "last_school_day"),
        @org.apache.ibatis.annotations.Result(property = "yearLeft", column = "year_left"),
        @org.apache.ibatis.annotations.Result(property = "maritalStatus", column = "marital_status"),
        @org.apache.ibatis.annotations.Result(property = "showYearLeft", column = "show_year_left"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryUniversity", column = "show_tertiary_university"),
        @org.apache.ibatis.annotations.Result(property = "showTertiaryMajor", column = "show_tertiary_major"),
        @org.apache.ibatis.annotations.Result(property = "showCareerCompany", column = "show_career_company"),
        @org.apache.ibatis.annotations.Result(property = "showJobTitle", column = "show_job_title"),
        @org.apache.ibatis.annotations.Result(property = "showIndustry", column = "show_industry"),
        @org.apache.ibatis.annotations.Result(property = "showCountry", column = "show_country"),
        @org.apache.ibatis.annotations.Result(property = "password", column = "password"),
        @org.apache.ibatis.annotations.Result(property = "updateTime", column = "update_time"),
        @org.apache.ibatis.annotations.Result(property = "role", column = "role"),
        @org.apache.ibatis.annotations.Result(property = "tagIds", column = "tag_ids"),
        @org.apache.ibatis.annotations.Result(property = "region", column = "region"),
        @org.apache.ibatis.annotations.Result(property = "state", column = "state"),
        @org.apache.ibatis.annotations.Result(property = "birthday", column = "birthday"),
        @org.apache.ibatis.annotations.Result(property = "openid", column = "openid")
    })
    Alumni findAlumniDetailById(@Param("alumniId") String alumniId);

    
    int updateAlumniById(Alumni alumni);

    @Select("SELECT * FROM tertiary_information WHERE alumni_id = #{alumniId}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "id", column = "id"),
        @org.apache.ibatis.annotations.Result(property = "alumniId", column = "alumni_id"),
        @org.apache.ibatis.annotations.Result(property = "universityCollege", column = "university_college"),
        @org.apache.ibatis.annotations.Result(property = "degree", column = "degree"),
        @org.apache.ibatis.annotations.Result(property = "major", column = "major"),
        @org.apache.ibatis.annotations.Result(property = "graduationYear", column = "graduation_year"),
        @org.apache.ibatis.annotations.Result(property = "countryRegion", column = "country_region")
    })
    List<TertiaryInformation> selectTertiaryByAlumniId(@Param("alumniId") String alumniId);

    @Select("SELECT * FROM carrer_information WHERE alumni_id = #{alumniId}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "id", column = "id"),
        @org.apache.ibatis.annotations.Result(property = "alumniId", column = "alumni_id"),
        @org.apache.ibatis.annotations.Result(property = "companyOrganization", column = "company_organization"),
        @org.apache.ibatis.annotations.Result(property = "jobTitle", column = "job_title"),
        @org.apache.ibatis.annotations.Result(property = "industry", column = "industry"),
        @org.apache.ibatis.annotations.Result(property = "countryRegion", column = "country_region")
    })
    List<CareerInformation> selectCareerByAlumniId(@Param("alumniId") String alumniId);

    @Delete("DELETE FROM tertiary_information WHERE alumni_id = #{alumniId}")
    int deleteTertiaryByAlumniId(@Param("alumniId") String alumniId);

    @Delete("DELETE FROM carrer_information WHERE alumni_id = #{alumniId}")
    int deleteCareerByAlumniId(@Param("alumniId") String alumniId);

    @Delete("DELETE FROM alumnus WHERE alumni_id = #{alumniId}")
    int deleteAlumniById(@Param("alumniId") String alumniId);

    @Select("SELECT COUNT(*) FROM alumnus WHERE email = #{email}")
    int countEmailExists(@Param("email") String email);

    int deleteTertiaryById(@Param("id") String id);
    int deleteCareerById(@Param("id") String id);

    @Select("SELECT COUNT(*) FROM alumnus")
    int countTotalAlumni();

    @Select("SELECT COUNT(*) FROM alumnus WHERE year_left = #{year}")
    int countGraduatesByYear(@Param("year") int year);

    @Select("SELECT year_left, COUNT(*) as num FROM alumnus WHERE year_left IS NOT NULL GROUP BY year_left ORDER BY num DESC LIMIT 5")
    java.util.List<java.util.Map<String, Object>> top5GraduationYears();

    @Select("SELECT current_location FROM alumnus WHERE current_location IS NOT NULL AND current_location != ''")
    java.util.List<String> getAllCurrentLocations();

    @Select("SELECT ycyw_schools_attended, COUNT(*) as num FROM alumnus WHERE ycyw_schools_attended IS NOT NULL AND ycyw_schools_attended != '' GROUP BY ycyw_schools_attended ORDER BY num DESC LIMIT 5")
    java.util.List<java.util.Map<String, Object>> top5Schools();

    @Select("SELECT industry FROM carrer_information WHERE industry IS NOT NULL AND industry != ''")
    java.util.List<String> getAllIndustries();

    // 动态字段批量查询
    java.util.List<java.util.Map<String, Object>> batchSelectFields(
        @org.apache.ibatis.annotations.Param("alumniIds") java.util.List<String> alumniIds,
        @org.apache.ibatis.annotations.Param("fields") java.util.List<String> fields
    );

    List<Alumni> selectStateZeroAlumni();

    void updateAlumniStateById(@Param("alumniId") String alumniId, @Param("state") int state);

    @Update("UPDATE alumnus SET openid = #{openid} WHERE alumni_id = #{alumniId}")
    int updateOpenidByAlumniId(@Param("alumniId") String alumniId, @Param("openid") String openid);

    // 新增：更新openid方法（与updateOpenidByAlumniId功能相同，保持命名一致性）
    @Update("UPDATE alumnus SET openid = #{openid} WHERE alumni_id = #{alumniId}")
    int updateOpenid(@Param("alumniId") String alumniId, @Param("openid") String openid);

    @Select("SELECT COUNT(*) FROM alumnus WHERE state = 2")
    int countPendingAlumni();
}
