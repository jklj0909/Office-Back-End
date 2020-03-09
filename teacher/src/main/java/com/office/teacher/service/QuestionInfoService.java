package com.office.teacher.service;

import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionMessage;
import com.office.common.entity.ReplyMessage;
import com.office.common.utils.CodecUtils;
import com.office.teacher.repository.QuestionInfoMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

@Service
@Transactional
public class QuestionInfoService {
    private QuestionInfoMapper questionInfoMapper;
    private static final String ROOT_PATH = "G:/testWord";

    @Autowired
    public void setQuestionInfoMapper(QuestionInfoMapper questionInfoMapper) {
        this.questionInfoMapper = questionInfoMapper;
    }

    /**
     * @param questionInfo 题目对象
     * @author jie
     **/
    public void createQuestion(QuestionInfo questionInfo) {
        questionInfo.setId(CodecUtils.generateUUID());
        questionInfo.setCreatedTime(new Timestamp(new Date().getTime()));
        questionInfo.setLastUpdatedTime(questionInfo.getCreatedTime());
        questionInfo.setVisitedCount(0l);
        questionInfo.setState(-1);
        try {
            questionInfoMapper.insert(questionInfo);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * @param id 题目的id
     * @return 是否存在该id
     * @author jie
     **/
    public ReplyMessage<QuestionMessage> checkIdIfExist(String id) {
        ReplyMessage<QuestionMessage> message = new ReplyMessage();
        QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
        if (questionInfo == null) {
            message.setSuccess(false);
            return message;
        }
        message.setSuccess(true);
        QuestionMessage questionMessage = new QuestionMessage();
        BeanUtils.copyProperties(questionInfo, questionMessage);
        message.setInfo(questionMessage);
        return message;
    }

    /**
     * 上传素材文件
     *
     * @param id         题目的id
     * @param type       题目的类型(word,excel,ppt)
     * @param uploadFile 上传的文件
     * @author jie
     **/
    public void uploadRawFile(MultipartFile uploadFile, String type, String id) throws Exception {
        String fileName = uploadFile.getOriginalFilename();
        String parentPath = ROOT_PATH + "/" + type + "/" + id;
        File parentDirectory = new File(parentPath);
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
        File targetFile = new File(parentPath + "/" + id + fileName.substring(fileName.lastIndexOf(".")));
        if (targetFile.exists()) {
            FileUtils.forceDelete(targetFile);
        }
        uploadFile.transferTo(targetFile);
        QuestionInfo questionInfo = new QuestionInfo();
        questionInfo.setId(id);
        questionInfo.setState(0);
        questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
    }
}
