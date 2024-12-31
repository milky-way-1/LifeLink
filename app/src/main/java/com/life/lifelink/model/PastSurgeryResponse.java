package com.life.lifelink.model;

public class PastSurgeryResponse {
    private String surgeryType;
    private String approximateDate;

    public PastSurgeryResponse(String type, String date) {
        this.surgeryType = type;
        this.approximateDate = date;
    }

}
