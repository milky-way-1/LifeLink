package com.life.lifelink.model;

public class MedicationDto {
    private String medicationName;
    private String dosage;

    public MedicationDto(String name, String dosage) {
        this.medicationName= name;
        this.dosage = dosage;
    }
}