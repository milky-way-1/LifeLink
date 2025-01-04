package com.life.lifelink.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

public class Location {
    @SerializedName("coordinates")
    private List<Double> coordinates;

    @SerializedName("type")
    private String type = "Point";

    public Location(Double latitude, Double longitude) {
        this.coordinates = Arrays.asList(longitude, latitude); // MongoDB expects [longitude, latitude]
    }

    public Location(double latitude, double longitude){
        this.coordinates = Arrays.asList(longitude, latitude);
    }

    // Getters
    public List<Double> getCoordinates() {
        return coordinates;
    }

    public String getType() {
        return type;
    }

    public Double getLatitude() {
        return coordinates != null && coordinates.size() > 1 ? coordinates.get(1) : null;
    }

    public Double getLongitude() {
        return coordinates != null && coordinates.size() > 0 ? coordinates.get(0) : null;
    }

    // Setters
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLatitude(Double latitude) {
        if (coordinates == null) {
            coordinates = Arrays.asList(0.0, latitude);
        } else {
            coordinates.set(1, latitude);
        }
    }

    public void setLongitude(Double longitude) {
        if (coordinates == null) {
            coordinates = Arrays.asList(longitude, 0.0);
        } else {
            coordinates.set(0, longitude);
        }
    }
}
