package com.life.lifelink.model;

import com.google.gson.annotations.SerializedName;
public class DriverLocation {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public double getLatitude() {
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }
}
