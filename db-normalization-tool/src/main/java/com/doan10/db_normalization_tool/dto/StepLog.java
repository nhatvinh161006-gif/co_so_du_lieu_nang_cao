package com.doan10.db_normalization_tool.dto;

public class StepLog {
    private int stepNumber;
    private String title;
    private String description;
    private String snapshot;

    public StepLog() {}

    public StepLog(int stepNumber, String title, String description, String snapshot) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
        this.snapshot = snapshot;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String toString() {
        return "Bước " + stepNumber + ": [" + title + "] " + description + (snapshot != null ? " -> " + snapshot : "");
    }
}
