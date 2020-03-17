package com.office.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Table(name = "practise_record")
public class PractiseRecord {
    @Id
    private String id;
    private String studentUsername;
    private String questionType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp practiseTime;
    private String selectionIds;
    private Boolean isTest;
    private String scores;
    private Double total;
    private String title;
    private String stuAnswer;
    private String operationIds;
    @Transient
    private List<String> selectionIdList;
    @Transient
    private List<String> scoreList;
    @Transient
    private List<String> stuAnswerList;
    @Transient
    private List<String> operationIdList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
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

    public String getSelectionIds() {
        return selectionIds;
    }

    public void setSelectionIds(String selectionIds) {
        this.selectionIds = selectionIds;
    }

    public Boolean getTest() {
        return isTest;
    }

    public void setTest(Boolean test) {
        isTest = test;
    }

    public String getScores() {
        return scores;
    }

    public void setScores(String scores) {
        this.scores = scores;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setSelectionIdList() throws Exception {
        if (StringUtils.isBlank(selectionIds)) {
            throw new Exception("selectionIds cannot be null or empty");
        }
        this.selectionIdList = Arrays.asList(selectionIds.split("&"));
    }

    public List<String> getSelectionIdList() {
        return selectionIdList;
    }

    public void setScoreList() throws Exception {
        if (StringUtils.isBlank(scores)) {
            throw new Exception("scores cannot ben null or empty");
        }
        scoreList = Arrays.asList(scores.split("&"));
    }

    public List<String> getScoreList() {
        return scoreList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStuAnswer() {
        return stuAnswer;
    }

    public void setStuAnswer(String stuAnswer) {
        this.stuAnswer = stuAnswer;
    }

    public String getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(String operationIds) {
        this.operationIds = operationIds;
    }

    public void setStuAnswerList() throws Exception {
        if (StringUtils.isBlank(stuAnswer)) {
            throw new Exception("stuAnswer cannot be null or empty");
        }
        this.stuAnswerList = Arrays.asList(stuAnswer.split("&"));
    }

    public List<String> getStuAnswerList() {
        return stuAnswerList;
    }

    public void setOperationIdList() throws Exception {
        if (StringUtils.isBlank(operationIds)) {
            throw new Exception();
        }
        this.operationIdList = Arrays.asList(operationIds.split("&"));
    }

    public List<String> getOperationIdList() {
        return operationIdList;
    }
}
