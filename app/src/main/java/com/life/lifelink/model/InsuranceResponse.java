package com.life.lifelink.model;

import com.google.gson.annotations.SerializedName;

public class InsuranceResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("insuranceProviderName")
    private String insuranceProviderName;

    @SerializedName("policyNumber")
    private String policyNumber;

    @SerializedName("groupNumber")
    private String groupNumber;

    @SerializedName("insuranceType")
    private String insuranceType;

    @SerializedName("policyHolderName")
    private String policyHolderName;

    @SerializedName("relationshipToPolicyHolder")
    private String relationshipToPolicyHolder;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("planType")
    private String planType;

    @SerializedName("coversEmergencyService")
    private boolean coversEmergencyService;

    @SerializedName("coversAmbulanceService")
    private boolean coversAmbulanceService;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("lastUpdatedAt")
    private String lastUpdatedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInsuranceProviderName() {
        return insuranceProviderName;
    }

    public void setInsuranceProviderName(String insuranceProviderName) {
        this.insuranceProviderName = insuranceProviderName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getPolicyHolderName() {
        return policyHolderName;
    }

    public void setPolicyHolderName(String policyHolderName) {
        this.policyHolderName = policyHolderName;
    }

    public String getRelationshipToPolicyHolder() {
        return relationshipToPolicyHolder;
    }

    public void setRelationshipToPolicyHolder(String relationshipToPolicyHolder) {
        this.relationshipToPolicyHolder = relationshipToPolicyHolder;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public boolean isCoversEmergencyService() {
        return coversEmergencyService;
    }

    public void setCoversEmergencyService(boolean coversEmergencyService) {
        this.coversEmergencyService = coversEmergencyService;
    }

    public boolean isCoversAmbulanceService() {
        return coversAmbulanceService;
    }

    public void setCoversAmbulanceService(boolean coversAmbulanceService) {
        this.coversAmbulanceService = coversAmbulanceService;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}