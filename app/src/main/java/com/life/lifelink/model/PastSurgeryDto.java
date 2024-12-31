package com.life.lifelink.model;

public class PastSurgeryDto {
    private String surgeryType;
    private String approximateDate;

    public PastSurgeryDto(String type, String date) {
        this.surgeryType = type;
        this.approximateDate = date;
    }
}
