package com.office.teacher.service;

import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionMessage;
import com.office.common.entity.QuestionStep;
import com.office.common.entity.ReplyMessage;
import com.office.common.entity.step.QuestionStepExcel;
import com.office.common.entity.step.QuestionStepPpt;
import com.office.common.entity.step.QuestionStepWord;
import com.office.common.utils.CodecUtils;
import com.office.teacher.repository.QuestionInfoMapper;
import com.office.teacher.repository.QuestionStepExcelMapper;
import com.office.teacher.repository.QuestionStepPptMapper;
import com.office.teacher.repository.QuestionStepWordMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

@Service
@Transactional
public class QuestionInfoService {
    private QuestionInfoMapper questionInfoMapper;
    private QuestionStepWordMapper questionStepWordMapper;
    private QuestionStepExcelMapper questionStepExcelMapper;
    private QuestionStepPptMapper questionStepPptMapper;
    private static final String ROOT_PATH = "G:/testWord";

    @Autowired
    public void setQuestionInfoMapper(QuestionInfoMapper questionInfoMapper) {
        this.questionInfoMapper = questionInfoMapper;
    }

    @Autowired
    public void setQuestionStepWordMapper(QuestionStepWordMapper questionStepWordMapper) {
        this.questionStepWordMapper = questionStepWordMapper;
    }

    @Autowired
    public void setQuestionStepExcelMapper(QuestionStepExcelMapper questionStepExcelMapper) {
        this.questionStepExcelMapper = questionStepExcelMapper;
    }

    @Autowired
    public void setQuestionStepPptMapper(QuestionStepPptMapper questionStepPptMapper) {
        this.questionStepPptMapper = questionStepPptMapper;
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
    public ReplyMessage<QuestionMessage> checkIdIfExist(String id, String username) {
        ReplyMessage<QuestionMessage> message = new ReplyMessage();
        QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
        if (username == null) {
            message.setSuccess(false);
            message.setMessage("not_login");
            return message;
        }
        if (questionInfo == null) {
            message.setSuccess(false);
            message.setMessage("试题不存在,请创建");
            return message;
        }
        if (!StringUtils.equals(questionInfo.getUsername(), username)) {
            message.setSuccess(false);
            message.setMessage("您没有操作该试题的权限");
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
    public ReplyMessage uploadRawFile(MultipartFile uploadFile, String type, String id) throws RuntimeException {
        String fileName = uploadFile.getOriginalFilename();
        String parentPath = ROOT_PATH + "/" + type + "/" + id;
        String targetFilePath = parentPath + "/" + id + fileName.substring(fileName.lastIndexOf("."));
        QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
        //将所有步骤文件删除
        if (questionInfo.getState() > -1) {
            questionInfo.setState(-1);
            questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
            File file = new File(parentPath + "/" + questionInfo.getUsername());
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uploadFile(uploadFile, parentPath, targetFilePath, id);
    }

    /**
     * 上传每一步操作文件
     *
     * @param id         题目的id
     * @param type       题目的类型(word,excel,ppt)
     * @param uploadFile 上传的文件
     * @param step       对应的步骤
     * @author jie
     **/
    public ReplyMessage uploadStepFile(MultipartFile uploadFile, String type, String id, Integer step) throws RuntimeException {
        String fileName = uploadFile.getOriginalFilename();
        QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
        String parentPath = ROOT_PATH + "/" + type + "/" + id + "/" + questionInfo.getUsername();
        String targetFilePath = parentPath + "/" + step + fileName.substring(fileName.lastIndexOf("."));
        //将后面步骤的文件删除
        if (step > questionInfo.getState()) {
            ReplyMessage message = new ReplyMessage();
            message.setSuccess(false);
            return message;
        }
        if (questionInfo.getState() > step) {
            questionInfo.setState(step);
            questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
            File parentDirectory = new File(parentPath);
            for (File file : parentDirectory.listFiles()) {
                if (Integer.valueOf(file.getName().substring(file.getName().indexOf("."))) >= step) {
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return uploadFile(uploadFile, parentPath, targetFilePath, id);
    }

    /**
     * 抽离出来的上传文件逻辑
     *
     * @param id             题目的id
     * @param uploadFile     上传的文件
     * @param parentPath     父文件夹路径
     * @param targetFilePath 目标文件路径
     * @author jie
     **/
    public ReplyMessage uploadFile(MultipartFile uploadFile, String parentPath, String targetFilePath, String id) throws RuntimeException {
        try {
            File parentDirectory = new File(parentPath);
            if (!parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }
            File targetFile = new File(targetFilePath);
            if (targetFile.exists()) {
                FileUtils.forceDelete(targetFile);
            }
            uploadFile.transferTo(targetFile);
            QuestionInfo questionInfo = questionInfoMapper.selectByPrimaryKey(id);
            questionInfo.setState(questionInfo.getState() + 1);
            questionInfo.setLastUpdatedTime(new Timestamp(new Date().getTime()));
            Integer step = questionInfo.getState();
            questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
            ReplyMessage message = new ReplyMessage();
            message.setSuccess(true);
            message.setInfo(step);
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void addQuestionStep(QuestionStep questionStep) throws Exception {
        Example example = new Example(QuestionStep.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("step", questionStep.getStep()).andEqualTo("id", questionStep.getId());
        //先查找，再插入

    }
}
