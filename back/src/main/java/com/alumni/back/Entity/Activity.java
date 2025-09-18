package com.alumni.back.Entity;

import java.util.Date;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Activity {
    private String uuid;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signupStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signupEnd;
    private String detail;
    private String coverUrl;
    private String link;
    private String qrcodeUrl;
    private Date createdAt;
    private Date updatedAt;
    private Integer signupTotal;
    private Integer signupActual;
    private String venue;
    private Boolean wechatIdRequired;
    private Boolean addressRequired;
    private Boolean currentLocationRequired;
    private Boolean companionsRequired;
    private Boolean paymentProofRequired;

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

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Integer getSignupTotal() { return signupTotal; }
    public void setSignupTotal(Integer signupTotal) { this.signupTotal = signupTotal; }

    public Integer getSignupActual() { return signupActual; }
    public void setSignupActual(Integer signupActual) { this.signupActual = signupActual; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Boolean getWechatIdRequired() { return wechatIdRequired; }
    public void setWechatIdRequired(Boolean wechatIdRequired) { this.wechatIdRequired = wechatIdRequired; }

    public Boolean getAddressRequired() { return addressRequired; }
    public void setAddressRequired(Boolean addressRequired) { this.addressRequired = addressRequired; }

    public Boolean getCurrentLocationRequired() { return currentLocationRequired; }
    public void setCurrentLocationRequired(Boolean currentLocationRequired) { this.currentLocationRequired = currentLocationRequired; }

    public Boolean getCompanionsRequired() { return companionsRequired; }
    public void setCompanionsRequired(Boolean companionsRequired) { this.companionsRequired = companionsRequired; }

    public Boolean getPaymentProofRequired() { return paymentProofRequired; }
    public void setPaymentProofRequired(Boolean paymentProofRequired) { this.paymentProofRequired = paymentProofRequired; }
} 