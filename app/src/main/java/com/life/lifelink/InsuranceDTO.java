package com.life.lifelink;

public class InsuranceDTO {
    private String providerId;
    private String providerName;
    private String policyNumber;
    private String groupNumber;
    private String insuranceType;
    private String policyholderName;
    private String relationship;
    private String startDate;
    private String endDate;
    private String planType;
    private boolean coversEmergencyServices;
    private boolean coversAmbulanceServices;
    private String helplineNumber;
    private String frontImageUrl;
    private String backImageUrl;

    // Default Constructor
    public InsuranceDTO() {
    }

    // Constructor with essential fields
    public InsuranceDTO(String providerName, String policyNumber, String policyholderName) {
        this.providerName = providerName;
        this.policyNumber = policyNumber;
        this.policyholderName = policyholderName;
    }

    // Getters and Setters
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public String getPolicyholderName() {
        return policyholderName;
    }

    public void setPolicyholderName(String policyholderName) {
        this.policyholderName = policyholderName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    public boolean isCoversEmergencyServices() {
        return coversEmergencyServices;
    }

    public void setCoversEmergencyServices(boolean coversEmergencyServices) {
        this.coversEmergencyServices = coversEmergencyServices;
    }

    public boolean isCoversAmbulanceServices() {
        return coversAmbulanceServices;
    }

    public void setCoversAmbulanceServices(boolean coversAmbulanceServices) {
        this.coversAmbulanceServices = coversAmbulanceServices;
    }

    public String getHelplineNumber() {
        return helplineNumber;
    }

    public void setHelplineNumber(String helplineNumber) {
        this.helplineNumber = helplineNumber;
    }

    public String getFrontImageUrl() {
        return frontImageUrl;
    }

    public void setFrontImageUrl(String frontImageUrl) {
        this.frontImageUrl = frontImageUrl;
    }

    public String getBackImageUrl() {
        return backImageUrl;
    }

    public void setBackImageUrl(String backImageUrl) {
        this.backImageUrl = backImageUrl;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "InsuranceDTO{" +
                "providerId='" + providerId + '\'' +
                ", providerName='" + providerName + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                ", groupNumber='" + groupNumber + '\'' +
                ", insuranceType='" + insuranceType + '\'' +
                ", policyholderName='" + policyholderName + '\'' +
                ", relationship='" + relationship + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", planType='" + planType + '\'' +
                ", coversEmergencyServices=" + coversEmergencyServices +
                ", coversAmbulanceServices=" + coversAmbulanceServices +
                ", helplineNumber='" + helplineNumber + '\'' +
                ", frontImageUrl='" + frontImageUrl + '\'' +
                ", backImageUrl='" + backImageUrl + '\'' +
                '}';
    }
}
