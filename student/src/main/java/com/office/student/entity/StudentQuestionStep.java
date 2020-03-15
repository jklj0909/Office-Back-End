package com.office.student.entity;

import javax.persistence.Table;

@Table(name = "student_question_step")
public class StudentQuestionStep {
    private String id;
    private Integer step;
    private Double score;

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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
