package com.life.lifelink.model;

public class EmergencyContactDto {
    private String contactName;
    private String phoneNumber;

    public EmergencyContactDto(String name, String phoneNumber) {
        this.contactName = name;
        this.phoneNumber = phoneNumber;
    }
}
