package com.office.student.service;

import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionStep;
import com.office.common.entity.ReplyMessage;
import com.office.common.entity.diff.QuestionDiffWord;
import com.office.common.entity.step.QuestionStepWord;
import com.office.common.utils.CodecUtils;
import com.office.common.utils.FileUtil;
import com.office.common.utils.XmlDiffUtils;
import com.office.student.entity.Student;
import com.office.student.entity.StudentQuestionInfo;
import com.office.student.entity.StudentQuestionStep;
import com.office.student.repository.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.diff.DiffUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OperationService {
    private double STANDARD_SCORE = 0.7;
    private static final String ROOT_PATH = "G:/testWord";
    private StudentQuestionInfoMapper studentQuestionInfoMapper;
    private StudentQuestionStepMapper studentQuestionStepMapper;
    private QuestionDiffWordMapper questionDiffWordMapper;
    private QuestionStepWordMapper questionStepWordMapper;
    private QuestionInfoMapper questionInfoMapper;

    @Autowired
    public void setStudentQuestionInfoMapper(StudentQuestionInfoMapper studentQuestionInfoMapper) {
        this.studentQuestionInfoMapper = studentQuestionInfoMapper;
    }

    @Autowired
    public void setStudentQuestionStepMapper(StudentQuestionStepMapper studentQuestionStepMapper) {
        this.studentQuestionStepMapper = studentQuestionStepMapper;
    }

    @Autowired
    public void setQuestionDiffWordMapper(QuestionDiffWordMapper questionDiffWordMapper) {
        this.questionDiffWordMapper = questionDiffWordMapper;
    }

    @Autowired
    public void setQuestionStepWordMapper(QuestionStepWordMapper questionStepWordMapper) {
        this.questionStepWordMapper = questionStepWordMapper;
    }

    @Autowired
    public void setQuestionInfoMapper(QuestionInfoMapper questionInfoMapper) {
        this.questionInfoMapper = questionInfoMapper;
    }

    public String downloadRawFile(HttpServletResponse response, String type, String id, String filename) throws Exception {
        String parentPath = ROOT_PATH + "/" + type + "/" + id;
        String contentType = new MimetypesFileTypeMap().getContentType(filename);
        response.setHeader("Content-type", contentType);
        response.setCharacterEncoding("utf-8");
        response.addHeader("Content-Disposition", "attachment;fileName=" + filename);
        FileUtil.download(filename, parentPath, response);
        return filename;
    }

    public String getFilename(String type, String id) {
        String parentPath = ROOT_PATH + "/" + type + "/" + id;
        String filename = null;
        File parentDirectory = new File(parentPath);
        for (File file : parentDirectory.listFiles()) {
            if (!file.isDirectory() && file.getName().contains(id)) {
                filename = file.getName();
                break;
            }
        }
        return filename;
    }

    /**
     * 装载并存储学生信息
     *
     * @param questionType    试题类型
     * @param id              试题id
     * @param studentUsername 学生用户名
     **/
    private Integer loadStudentQuestionInfo(String questionType, String id, String studentUsername) throws RuntimeException {
        StudentQuestionInfo record = new StudentQuestionInfo();
        record.setQuestionId(id);
        record.setStudentUsername(studentUsername);
        int num = studentQuestionInfoMapper.selectCount(record);
        record.setPractiseNumber(num);
        StudentQuestionInfo studentQuestionInfo = studentQuestionInfoMapper.selectOne(record);
        if (studentQuestionInfo == null || studentQuestionInfo.getState()) {
            StudentQuestionInfo newRecord = new StudentQuestionInfo(CodecUtils.generateUUID(), id, new Timestamp(new Date().getTime()), studentUsername, num + 1, questionType);
            studentQuestionInfoMapper.insertSelective(newRecord);
            //第一次上传
            return num + 1;
        } else {
            //第n次上传,覆盖之前上传的文件
            return num;
        }
    }

    private String getParentPath(String questionType, String studentUsername, String id) {
        return ROOT_PATH + "/" + questionType + "/" + id + "/stu_" + studentUsername;
    }

    public void uploadAnswer(MultipartFile uploadFile, String questionType, String id, String studentUsername) throws Exception {
        Integer num = loadStudentQuestionInfo(questionType, id, studentUsername);
        String parentPath = getParentPath(questionType, studentUsername, id);
        String filename = num + FileUtil.getFileSuffix(questionType);
        File parentDirectory = new File(parentPath);
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
        File file = new File(parentPath + "/" + filename);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
        uploadFile.transferTo(file);
    }

    public ReplyMessage<List<StudentQuestionStep>> submitAnswer(String studentUsername, String questionType, String id) throws Exception {
        StudentQuestionInfo record = new StudentQuestionInfo();
        record.setQuestionId(id);
        record.setStudentUsername(studentUsername);
        Integer number = studentQuestionInfoMapper.selectCount(record);
        record.setPractiseNumber(number);
        StudentQuestionInfo studentQuestionInfo = studentQuestionInfoMapper.selectOne(record);
        if (studentQuestionInfo == null) {
            throw new Exception();
        }
        String studentQuestionInfoId = studentQuestionInfo.getId();
        String parentPath = ROOT_PATH + "/" + questionType + "/" + id;
        String answerPath = parentPath + "/stu_" + studentUsername + "/" + number + FileUtil.getFileSuffix(questionType);
        if (StringUtils.equals(questionType, "word")) {
            return submitWordAnswer(parentPath, answerPath, id, studentQuestionInfoId);
        } else if (StringUtils.equals(questionType, "excel")) {
            return submitExcelAnswer(parentPath, answerPath, id, studentQuestionInfoId);
        } else if (StringUtils.equals(questionType, "ppt")) {
            return submitPptAnswer(parentPath, answerPath, id, studentQuestionInfoId);
        }
        throw new Exception();
    }

    private ReplyMessage<List<StudentQuestionStep>> submitWordAnswer(String parentPath, String answerPath, String id, String studentQuestionInfoId) throws Exception {
        ReplyMessage<List<StudentQuestionStep>> message = new ReplyMessage();
        QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
        String createdUsername = questionInfo.getUsername();
        String rawFilePath = parentPath + "/" + createdUsername + "/0.docx";
        ArrayList<String> list = XmlDiffUtils.dealXmlDiff(rawFilePath, answerPath, parentPath + "/" + createdUsername, answerPath.substring(0, answerPath.lastIndexOf("/")));
        if (CollectionUtils.isEmpty(list)) {
            message.setSuccess(false);
            message.setMessage("请勿提交源文档");
            return message;
        }
        QuestionStepWord questionStepWordRecord = new QuestionStepWord();
        questionStepWordRecord.setId(id);
        StudentQuestionStep studentQuestionStepRecord = new StudentQuestionStep();
        studentQuestionStepRecord.setId(studentQuestionInfoId);
        double score = 0;
        for (int i = 1; i <= 30; i++) {
            questionStepWordRecord.setStep(i);
            studentQuestionStepRecord.setStep(i);
            QuestionStepWord stepWord = questionStepWordMapper.selectOne(questionStepWordRecord);
            String stepId = stepWord.getStepId();
            QuestionDiffWord questionDiffWord = new QuestionDiffWord();
            questionDiffWord.setStepId(stepId);
            List<QuestionDiffWord> questionDiffWordList = questionDiffWordMapper.select(questionDiffWord);
            int count = 0;
            double result = 0;
            if (CollectionUtils.isEmpty(questionDiffWordList)) {
                result = 1;
            } else {
                for (QuestionDiffWord diffWord : questionDiffWordList) {
                    if (list.contains(diffWord.getDifference())) {
                        count++;
                    }
                }
                result = ((double) count) / questionDiffWordList.size() > STANDARD_SCORE ? 1 : 0;
            }
            //计算该步骤平均分
            stepWord.setStepAverageScore(
                    ((double) (stepWord.getStepAverageScore() * questionInfo.getVisitedCount() + result)) / (questionInfo.getVisitedCount() + 1));
            Example exampleStepWord = new Example(QuestionStepWord.class);
            Example.Criteria criteria1 = exampleStepWord.createCriteria();
            criteria1.andEqualTo("id", stepWord.getId()).andEqualTo("step", stepWord.getStep());
            questionStepWordMapper.updateByExampleSelective(stepWord, exampleStepWord);
            studentQuestionStepRecord.setScore(result);
            studentQuestionStepMapper.insertSelective(studentQuestionStepRecord);
            score += result;
        }
        StudentQuestionInfo studentQuestionInfo = new StudentQuestionInfo();
        studentQuestionInfo.setId(studentQuestionInfoId);
        studentQuestionInfo.setState(true);
        studentQuestionInfo.setScore(score);
        studentQuestionInfoMapper.updateByPrimaryKeySelective(studentQuestionInfo);
        questionInfo.setAverageScore(((double)
                (questionInfo.getAverageScore() * questionInfo.getVisitedCount() + score)) / (questionInfo.getVisitedCount() + 1));
        questionInfo.setVisitedCount(questionInfo.getVisitedCount() + 1);
        questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
        StudentQuestionStep stuStepRecord = new StudentQuestionStep();
        stuStepRecord.setId(studentQuestionInfoId);
        List<StudentQuestionStep> questionStepList = studentQuestionStepMapper.select(stuStepRecord);
        message.setSuccess(true);
        message.setMessage(Double.toString(score));
        message.setInfo(questionStepList);
        return message;
    }

    private ReplyMessage<List<StudentQuestionStep>> submitPptAnswer(String parentPath, String answerPath, String id, String studentQuestionInfoId) {
        ReplyMessage<List<StudentQuestionStep>> message = new ReplyMessage();
        message.setSuccess(false);
        return message;
    }

    private ReplyMessage<List<StudentQuestionStep>> submitExcelAnswer(String parentPath, String answerPath, String id, String studentQuestionInfoId) {
        ReplyMessage<List<StudentQuestionStep>> message = new ReplyMessage();
        message.setSuccess(false);
        return message;
    }
}
