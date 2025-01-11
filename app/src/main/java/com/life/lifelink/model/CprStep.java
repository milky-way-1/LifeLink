package com.life.lifelink.model;

public class CprStep {
    private int stepNumber;
    private String title;
    private String description;
    private int imageResId;

    public CprStep(int stepNumber, String title, String description, int imageResId) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    // Getters
    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
}
