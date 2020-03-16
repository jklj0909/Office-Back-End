package com.office.student.entity;

public class QuestionStepDetail {
    private Integer step;
    private String stepDescription;
    private double stepAverageScore;
    private double stepScore;

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public double getStepAverageScore() {
        return stepAverageScore;
    }

    public void setStepAverageScore(double stepAverageScore) {
        this.stepAverageScore = stepAverageScore;
    }

    public double getStepScore() {
        return stepScore;
    }

    public void setStepScore(double stepScore) {
        this.stepScore = stepScore;
    }
}
