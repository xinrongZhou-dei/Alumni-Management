package com.alumni.back.Entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityRegistration {
    private String alumniId;
    private String activityUuid;
    private String salutation;
    private String firstName;
    private String lastName;
    private String chineseName;
    private String email;
    private String contactNumber;
    private String wechatId;
    private String correspondenceAddress;
    private String currentLocation;
    private Integer companions;
    private String paymentProof;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 