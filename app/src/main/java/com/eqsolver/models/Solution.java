package com.eqsolver.models;

import java.io.Serializable;

/**
 * Data model representing a solved equation with its step-by-step solution.
 */
public class Solution implements Serializable {
    private String extractedEquation;
    private String stepByStepSolution;
    private String imagePath;
    private long timestamp;

    public Solution() {
        this.timestamp = System.currentTimeMillis();
    }

    public Solution(String extractedEquation, String stepByStepSolution, String imagePath) {
        this.extractedEquation = extractedEquation;
        this.stepByStepSolution = stepByStepSolution;
        this.imagePath = imagePath;
        this.timestamp = System.currentTimeMillis();
    }

    public String getExtractedEquation() {
        return extractedEquation;
    }

    public void setExtractedEquation(String extractedEquation) {
        this.extractedEquation = extractedEquation;
    }

    public String getStepByStepSolution() {
        return stepByStepSolution;
    }

    public void setStepByStepSolution(String stepByStepSolution) {
        this.stepByStepSolution = stepByStepSolution;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
