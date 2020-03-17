package com.office.common.entity.wrap;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class RecordDisplay {
    private String id;
    private String questionType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp practiseTime;
    private Double Score;
    private Integer recordType;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Timestamp getPractiseTime() {
        return practiseTime;
    }

    public void setPractiseTime(Timestamp practiseTime) {
        this.practiseTime = practiseTime;
    }

    public Double getScore() {
        return Score;
    }

    public void setScore(Double score) {
        Score = score;
    }

    public Integer getRecordType() {
        return recordType;
    }

    public void setRecordType(Integer recordType) {
        this.recordType = recordType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
