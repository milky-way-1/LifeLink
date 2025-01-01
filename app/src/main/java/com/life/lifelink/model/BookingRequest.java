package com.life.lifelink.model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("userId")
    private String userId;

    @SerializedName("pickupLocation")
    private Location pickupLocation;

    @SerializedName("destinationLocation")
    private Location destinationLocation;

    // Getters and setters
    public BookingRequest() {}

    // Constructor with parameters
    public BookingRequest(String userId, Location pickupLocation, Location destinationLocation) {
        this.userId = userId;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }
}