package com.life.lifelink.model;

import com.google.gson.annotations.SerializedName;

public class BookingResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName("status")
    private String status;

    @SerializedName("driverId")
    private String driverId;

    // Getters and setters
    public BookingResponse() {}

    // Constructor with parameters
    public BookingResponse(boolean success, String message, String bookingId,
                           String status, String driverId) {
        this.success = success;
        this.message = message;
        this.bookingId = bookingId;
        this.status = status;
        this.driverId = driverId;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getStatus() {
        return status;
    }

    public String getDriverId() {
        return driverId;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    // Helper methods
    public boolean isAssigned() {
        return "ASSIGNED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}