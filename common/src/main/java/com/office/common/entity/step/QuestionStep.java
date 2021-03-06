package com.office.common.entity.step;

import javax.persistence.Transient;

public class QuestionStep {
    private String id;
    private Integer step;
    private String stepId;
    private String stepDescription;
    private Double stepAverageScore;
    @Transient
    private String questionType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public Double getStepAverageScore() {
        return stepAverageScore;
    }

    public void setStepAverageScore(Double stepAverageScore) {
        this.stepAverageScore = stepAverageScore;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
}
