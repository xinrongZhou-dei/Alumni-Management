package com.alumni.back.Entity;

import lombok.Data;

@Data
public class CareerInformation {
    private String id; // UUID
    private String alumniId;
    private String companyOrganization;
    private String jobTitle;
    private Industry industry;
    private String countryRegion;

    public enum Industry {
        信息技术与互联网,
        金融与经济,
        医疗与健康,
        教育与科研,
        工程与制造,
        创意与媒体,
        销售与市场,
        法律与公共事务,
        服务与旅游,
        农林与牧渔,
        交通与运输,
        其他;

        @Override
        public String toString() {
            return this.name();
        }
    }

    public CareerInformation() {
    }

    public CareerInformation(String id, String alumniId, String companyOrganization, String jobTitle, Industry industry, String countryRegion) {
        this.id = id;
        this.alumniId = alumniId;
        this.companyOrganization = companyOrganization;
        this.jobTitle = jobTitle;
        this.industry = industry;
        this.countryRegion = countryRegion;
    }

    public String getAlumniId() { return alumniId; }
    public void setAlumniId(String alumniId) { this.alumniId = alumniId; }
} 