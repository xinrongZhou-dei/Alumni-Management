package com.alumni.back.Entity;

import lombok.Data;

@Data
public class TertiaryInformation {
    private String id; // UUID
    private String alumniId;
    private String universityCollege;
    private String degree;
    private String major;
    private Integer graduationYear;
    private String countryRegion;

    public String getAlumniId() { return alumniId; }
    public void setAlumniId(String alumniId) { this.alumniId = alumniId; }
} 