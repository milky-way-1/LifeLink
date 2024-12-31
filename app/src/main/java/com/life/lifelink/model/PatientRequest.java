package com.life.lifelink.model;

import java.util.ArrayList;
import java.util.List;

public class PatientRequest {
    private String fullName;
    private int age;
    private Gender gender;
    private List<EmergencyContactDto> emergencyContacts;
    private List<String> medicalHistory;
    private List<PastSurgeryDto> pastSurgeries;
    private List<MedicationDto> currentMedications;
    private List<String> allergies;
    private BloodType bloodType;
    private double weight;
    private double height;
    private List<String> dietaryRestrictions;
    private boolean organDonor;
    private List<String> culturalConsiderations;

    // Constructor
    public PatientRequest() {
        this.emergencyContacts = new ArrayList<>();
        this.medicalHistory = new ArrayList<>();
        this.pastSurgeries = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
        this.allergies = new ArrayList<>();
        this.dietaryRestrictions = new ArrayList<>();
        this.culturalConsiderations = new ArrayList<>();
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<EmergencyContactDto> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContactDto> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<PastSurgeryDto> getPastSurgeries() {
        return pastSurgeries;
    }

    public void setPastSurgeries(List<PastSurgeryDto> pastSurgeries) {
        this.pastSurgeries = pastSurgeries;
    }

    public List<MedicationDto> getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(List<MedicationDto> currentMedications) {
        this.currentMedications = currentMedications;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public boolean isOrganDonor() {
        return organDonor;
    }

    public void setOrganDonor(boolean organDonor) {
        this.organDonor = organDonor;
    }

    public List<String> getCulturalConsiderations() {
        return culturalConsiderations;
    }

    public void setCulturalConsiderations(List<String> culturalConsiderations) {
        this.culturalConsiderations = culturalConsiderations;
    }
}
