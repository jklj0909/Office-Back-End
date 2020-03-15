package com.office.student.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Table(name = "student_question_info")
public class StudentQuestionInfo {
    @Id
    private String id;
    private String questionId;
    private Double score;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp practiseTime;
    private String studentUsername;
    private Integer practiseNumber;
    private String questionType;
    private Boolean state;

    public StudentQuestionInfo() {
    }

    public StudentQuestionInfo(String id, String questionId, Timestamp practiseTime, String studentUsername, Integer practiseNumber, String questionType) {
        this.id = id;
        this.questionId = questionId;
        this.practiseTime = practiseTime;
        this.studentUsername = studentUsername;
        this.practiseNumber = practiseNumber;
        this.questionType = questionType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Timestamp getPractiseTime() {
        return practiseTime;
    }

    public void setPractiseTime(Timestamp practiseTime) {
        this.practiseTime = practiseTime;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public Integer getPractiseNumber() {
        return practiseNumber;
    }

    public void setPractiseNumber(Integer practiseNumber) {
        this.practiseNumber = practiseNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "StudentQuestionInfo{" +
                "id='" + id + '\'' +
                ", questionId='" + questionId + '\'' +
                ", score=" + score +
                ", practiseTime=" + practiseTime +
                ", studentUsername='" + studentUsername + '\'' +
                ", practiseNumber=" + practiseNumber +
                ", questionType='" + questionType + '\'' +
                ", state=" + state +
                '}';
    }
}
