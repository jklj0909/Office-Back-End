package com.office.student.service;

import com.office.common.entity.PractiseRecord;
import com.office.common.entity.QuestionInfo;
import com.office.common.entity.Selection;
import com.office.common.entity.wrap.RecordDisplay;
import com.office.student.entity.StudentQuestionInfo;
import com.office.student.repository.PractiseRecordMapper;
import com.office.student.repository.QuestionInfoMapper;
import com.office.student.repository.SelectionMapper;
import com.office.student.repository.StudentQuestionInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PractiseRecordService {
    private PractiseRecordMapper practiseRecordMapper;
    private StudentQuestionInfoMapper studentQuestionInfoMapper;
    private QuestionInfoMapper questionInfoMapper;
    private SelectionMapper selectionMapper;

    @Autowired
    public void setPractiseRecordMapper(PractiseRecordMapper practiseRecordMapper) {
        this.practiseRecordMapper = practiseRecordMapper;
    }

    @Autowired
    public void setSelectionMapper(SelectionMapper selectionMapper) {
        this.selectionMapper = selectionMapper;
    }

    @Autowired
    public void setStudentQuestionInfoMapper(StudentQuestionInfoMapper studentQuestionInfoMapper) {
        this.studentQuestionInfoMapper = studentQuestionInfoMapper;
    }

    @Autowired
    public void setQuestionInfoMapper(QuestionInfoMapper questionInfoMapper) {
        this.questionInfoMapper = questionInfoMapper;
    }

    public List<RecordDisplay> getRecordMap(String studentUsername) throws Exception {
        List<RecordDisplay> recordDisplayList = new ArrayList<>();
        StudentQuestionInfo studentQuestionInfoRecord = new StudentQuestionInfo();
        studentQuestionInfoRecord.setStudentUsername(studentUsername);
        List<StudentQuestionInfo> studentQuestionInfoList = studentQuestionInfoMapper.select(studentQuestionInfoRecord);
        if (!CollectionUtils.isEmpty(studentQuestionInfoList)) {
            studentQuestionInfoList.forEach(studentQuestionInfo -> {
                QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(studentQuestionInfo.getQuestionId());
                RecordDisplay recordDisplay = new RecordDisplay();
                BeanUtils.copyProperties(studentQuestionInfo, recordDisplay);
                recordDisplay.setRecordType(1);
                recordDisplay.setTitle(questionInfo.getTitle());
                recordDisplayList.add(recordDisplay);
            });
        }
        PractiseRecord practiseRecord = new PractiseRecord();
        practiseRecord.setStudentUsername(studentUsername);
        List<PractiseRecord> practiseRecordList = practiseRecordMapper.select(practiseRecord);
        if (!CollectionUtils.isEmpty(practiseRecordList)) {
            practiseRecordList.forEach(practiseRecord1 -> {
                RecordDisplay recordDisplay = new RecordDisplay();
                BeanUtils.copyProperties(practiseRecord1, recordDisplay);
                recordDisplay.setScore(practiseRecord1.getTotal());
                recordDisplay.setRecordType(practiseRecord1.getTest() ? 2 : 0);
                recordDisplayList.add(recordDisplay);
            });
        }
        return recordDisplayList;
    }

    public Map<String, Object> getTestResult(String id) throws Exception {
        Map<String, Object> testResult = new HashMap<>();
        PractiseRecord practiseRecord = practiseRecordMapper.selectByPrimaryKey(id);
        practiseRecord.setSelectionIdList();
        practiseRecord.setScoreList();
        practiseRecord.setStuAnswerList();
        testResult.put("selectionAnswer", practiseRecord.getStuAnswerList());
        testResult.put("selectionScore", practiseRecord.getStuAnswerList());
        List<String> selectionIdList = practiseRecord.getSelectionIdList();
        List<Selection> selectionList = new ArrayList<>();
        for (String selectionId : selectionIdList) {
            Selection selection = selectionMapper.selectByPrimaryKey(selectionId);
            selectionList.add(selection);
        }
        testResult.put("selectionList", selectionList);
        if (practiseRecord.getTest()) {
            practiseRecord.setOperationIdList();
            List<String> operationIdList = practiseRecord.getOperationIdList();
            List<Double> operationScoreList = new ArrayList<>();
            List<QuestionInfo> questionInfoList = new ArrayList<>();
            for (String operationId : operationIdList) {
                StudentQuestionInfo studentQuestionInfo = studentQuestionInfoMapper.selectByPrimaryKey(operationId);
                operationScoreList.add(studentQuestionInfo.getScore());
                QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(studentQuestionInfo.getQuestionId());
                questionInfoList.add(questionInfo);
            }
            testResult.put("operationScoreList", operationScoreList);
            testResult.put("questionInfoList", questionInfoList);
        }
        return testResult;
    }
}
