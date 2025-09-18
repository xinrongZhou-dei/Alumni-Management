package com.alumni.back.vo;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ActivityVO {
    private String uuid;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signupStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signupEnd;
    private String coverUrl;
    private Integer signupTotal;
    private Integer signupActual;
    private String signupStatus;
    private String detail;
    private String venue;
    private Boolean wechat_id_required;
    private Boolean address_required;
    private Boolean current_location_required;
    private Boolean companions_required;
    private Boolean payment_proof_required;
    private String link;
    private String qrcodeUrl;

    // getter和setter
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

    public LocalDate getSignupStart() { return signupStart; }
    public void setSignupStart(LocalDate signupStart) { this.signupStart = signupStart; }

    public LocalDate getSignupEnd() { return signupEnd; }
    public void setSignupEnd(LocalDate signupEnd) { this.signupEnd = signupEnd; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Integer getSignupTotal() { return signupTotal; }
    public void setSignupTotal(Integer signupTotal) { this.signupTotal = signupTotal; }

    public Integer getSignupActual() { return signupActual; }
    public void setSignupActual(Integer signupActual) { this.signupActual = signupActual; }

    public String getSignupStatus() { return signupStatus; }
    public void setSignupStatus(String signupStatus) { this.signupStatus = signupStatus; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Boolean getWechat_id_required() { return wechat_id_required; }
    public void setWechat_id_required(Boolean wechat_id_required) { this.wechat_id_required = wechat_id_required; }

    public Boolean getAddress_required() { return address_required; }
    public void setAddress_required(Boolean address_required) { this.address_required = address_required; }

    public Boolean getCurrent_location_required() { return current_location_required; }
    public void setCurrent_location_required(Boolean current_location_required) { this.current_location_required = current_location_required; }

    public Boolean getCompanions_required() { return companions_required; }
    public void setCompanions_required(Boolean companions_required) { this.companions_required = companions_required; }

    public Boolean getPayment_proof_required() { return payment_proof_required; }
    public void setPayment_proof_required(Boolean payment_proof_required) { this.payment_proof_required = payment_proof_required; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }
} 