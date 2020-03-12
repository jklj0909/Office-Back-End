package com.office.teacher.service;

import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionMessage;
import com.office.common.entity.QuestionStep;
import com.office.common.entity.ReplyMessage;
import com.office.common.entity.diff.QuestionDiffWord;
import com.office.common.entity.step.QuestionStepExcel;
import com.office.common.entity.step.QuestionStepPpt;
import com.office.common.entity.step.QuestionStepWord;
import com.office.common.utils.CodecUtils;
import com.office.common.utils.XmlDiffUtils;
import com.office.teacher.repository.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class QuestionInfoService {
    private QuestionInfoMapper questionInfoMapper;
    private QuestionStepWordMapper questionStepWordMapper;
    private QuestionStepExcelMapper questionStepExcelMapper;
    private QuestionStepPptMapper questionStepPptMapper;
    private QuestionDiffExcelMapper questionDiffExcelMapper;
    private QuestionDiffWordMapper questionDiffWordMapper;
    private QuestionDiffPptMapper questionDiffPptMapper;
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

    @Autowired
    public void setQuestionDiffExcelMapper(QuestionDiffExcelMapper questionDiffExcelMapper) {
        this.questionDiffExcelMapper = questionDiffExcelMapper;
    }

    @Autowired
    public void setQuestionDiffWordMapper(QuestionDiffWordMapper questionDiffWordMapper) {
        this.questionDiffWordMapper = questionDiffWordMapper;
    }

    @Autowired
    public void setQuestionDiffPptMapper(QuestionDiffPptMapper questionDiffPptMapper) {
        this.questionDiffPptMapper = questionDiffPptMapper;
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
    public ReplyMessage<QuestionMessage> checkIdIfExist(String id, String username) throws RuntimeException {
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
        if (questionInfo.getState() > 0 && questionInfo.getState() < 31) {
            String stepDescription = queryStepDescription(questionInfo.getId(), questionInfo.getQuestionType(), questionInfo.getState());
            if (stepDescription == null) {
                stepDescription = "";
            }
            message.setMessage(stepDescription);
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
        if (step > questionInfo.getState()) {
            ReplyMessage message = new ReplyMessage();
            message.setSuccess(false);
            return message;
        }
        //将后面步骤的文件删除
        if (questionInfo.getState() > step) {
            questionInfo.setState(step);
            questionInfoMapper.updateByPrimaryKeySelective(questionInfo);
            File parentDirectory = new File(parentPath);
            for (File file : parentDirectory.listFiles()) {
                if (Integer.valueOf(file.getName().substring(0, file.getName().indexOf("."))) >= step) {
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (step > 0) {
            QuestionStep questionStep = new QuestionStep();
            questionStep.setStep(step);
            questionStep.setQuestionType(type);
            questionStep.setId(id);
            addQuestionStep(questionStep);
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

    /**
     * 保存新的步骤信息
     *
     * @param questionStep 试题一个步骤的对象
     * @author jie
     **/
    public void addQuestionStep(QuestionStep questionStep) throws RuntimeException {
        if (StringUtils.equals(questionStep.getQuestionType(), "word")) {
            Example example = new Example(QuestionStepWord.class);
            List<QuestionStepWord> questionStepWords = findQuestionStepWordByStepAndId(example, questionStep);
            QuestionStepWord questionStepWord = new QuestionStepWord();
            BeanUtils.copyProperties(questionStep, questionStepWord);
            if (CollectionUtils.isEmpty(questionStepWords)) {
                questionStepWord.setStepId(CodecUtils.generateUUID());
                questionStepWordMapper.insertSelective(questionStepWord);
            } else {
                questionStepWordMapper.updateByExampleSelective(questionStepWord, example);
            }
        } else if (StringUtils.equals(questionStep.getQuestionType(), "excel")) {
            Example example = new Example(QuestionStepExcel.class);
            List<QuestionStepExcel> questionStepExcels = findQuestionStepExcelByStepAndId(example, questionStep);
            QuestionStepExcel questionStepExcel = new QuestionStepExcel();
            BeanUtils.copyProperties(questionStep, questionStepExcel);
            if (CollectionUtils.isEmpty(questionStepExcels)) {
                questionStep.setStepId(CodecUtils.generateUUID());
                questionStepExcelMapper.insertSelective(questionStepExcel);
            } else {
                questionStepExcelMapper.updateByExampleSelective(questionStepExcel, example);
            }
        } else if (StringUtils.equals(questionStep.getQuestionType(), "ppt")) {
            Example example = new Example(QuestionStep.class);
            List<QuestionStepPpt> questionStepPpts = findQuestionStepPptByStepAndId(example, questionStep);
            QuestionStepPpt questionStepPpt = new QuestionStepPpt();
            BeanUtils.copyProperties(questionStep, questionStepPpt);
            if (CollectionUtils.isEmpty(questionStepPpts)) {
                questionStep.setStepId(CodecUtils.generateUUID());
                questionStepPptMapper.insertSelective(questionStepPpt);
            } else {
                questionStepPptMapper.updateByExampleSelective(questionStepPpt, example);
            }
        }
    }

    /**
     * 根据条件查找步骤信息(word)
     *
     * @param questionStep 试题一个步骤的对象
     * @author jie
     **/
    public List<QuestionStepWord> findQuestionStepWordByStepAndId(Example example, QuestionStep questionStep) {
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("step", questionStep.getStep()).andEqualTo("id", questionStep.getId());
        return questionStepWordMapper.selectByExample(example);
    }

    /**
     * 根据条件查找步骤信息(excel)
     *
     * @param questionStep 试题一个步骤的对象
     * @author jie
     **/
    public List<QuestionStepExcel> findQuestionStepExcelByStepAndId(Example example, QuestionStep questionStep) {
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("step", questionStep.getStep()).andEqualTo("id", questionStep.getId());
        return questionStepExcelMapper.selectByExample(example);
    }

    /**
     * 根据条件查找步骤信息(ppt)
     *
     * @param questionStep 试题一个步骤的对象
     * @author jie
     **/
    public List<QuestionStepPpt> findQuestionStepPptByStepAndId(Example example, QuestionStep questionStep) {
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("step", questionStep.getStep()).andEqualTo("id", questionStep.getId());
        return questionStepPptMapper.selectByExample(example);
    }

    /**
     * 根据条件查找对应试题步骤的描述信息
     *
     * @param id   试题一个步骤的对象
     * @param step 当前的步骤
     * @param type 试题的种类
     * @author jie
     **/
    public ReplyMessage getStepDescription(String id, String type, Integer step) throws RuntimeException {
        ReplyMessage message = new ReplyMessage();
        if (StringUtils.equals(id, "") || StringUtils.equals(type, "") || step == -2) {
            message.setSuccess(false);
            return message;
        }
        String stepDescription = queryStepDescription(id, type, step);
        message.setSuccess(true);
        message.setMessage(stepDescription);
        return message;
    }

    /**
     * 根据条件查找对应试题步骤的描述信息
     *
     * @param id           试题一个步骤的对象
     * @param state        当前的步骤
     * @param questionType 试题的种类
     * @author jie
     **/
    public String queryStepDescription(String id, String questionType, Integer state) throws RuntimeException {
        if (StringUtils.equals(questionType, "word")) {
            QuestionStepWord questionStepWord = new QuestionStepWord();
            questionStepWord.setId(id);
            questionStepWord.setStep(state);
            QuestionStepWord newQuestionStepWord = questionStepWordMapper.selectOne(questionStepWord);
            if (newQuestionStepWord != null) {
                return newQuestionStepWord.getStepDescription();
            }
        } else if (StringUtils.equals(questionType, "ppt")) {
            QuestionStepPpt questionStepPpt = new QuestionStepPpt();
            questionStepPpt.setId(id);
            questionStepPpt.setStep(state);
            QuestionStepPpt newQuestionStepPpt = questionStepPptMapper.selectOne(questionStepPpt);
            if (newQuestionStepPpt != null) {
                return newQuestionStepPpt.getStepDescription();
            }
        } else if (StringUtils.equals(questionType, "excel")) {
            QuestionStepExcel questionStepExcel = new QuestionStepExcel();
            questionStepExcel.setId(id);
            questionStepExcel.setStep(state);
            QuestionStepExcel newQuestionStepExcel = questionStepExcelMapper.selectOne(questionStepExcel);
            if (newQuestionStepExcel != null) {
                return newQuestionStepExcel.getStepDescription();
            }
        }
        return "";
    }

    /**
     * 根据文件类型的不同分别进行xml文件比对
     *
     * @param questionInfo 试题对象
     * @author jie
     **/
    public ReplyMessage generateXmlDifference(QuestionInfo questionInfo) {
        ReplyMessage message = null;
        if (StringUtils.equals(questionInfo.getQuestionType(), "word")) {
            message = generateWordXmlDifference(questionInfo);
        } else if (StringUtils.equals(questionInfo.getQuestionType(), "excel")) {
            message = generateExcelXmlDifference(questionInfo);
        } else if (StringUtils.equals(questionInfo.getQuestionType(), "ppt")) {
            message = generatePptXmlDifference(questionInfo);
        } else {
            message = new ReplyMessage();
            message.setSuccess(false);
            message.setMessage("请刷新界面或者之后重试,如无法解决请联系管理员");
        }
        return message;
    }

    /**
     * 进行excel的xml文件比对
     *
     * @param questionInfo 试题对象
     * @author jie
     **/
    private ReplyMessage generateExcelXmlDifference(QuestionInfo questionInfo) {
        ReplyMessage message = new ReplyMessage();
        //todo
        message.setSuccess(true);
        return message;
    }

    /**
     * 根进行ppt的xml文件比对
     *
     * @param questionInfo 试题对象
     * @author jie
     **/
    private ReplyMessage generatePptXmlDifference(QuestionInfo questionInfo) {
        ReplyMessage message = new ReplyMessage();
        //todo
        message.setSuccess(true);
        return message;
    }

    /**
     * 进行word的xml文件比对
     *
     * @param questionInfo 试题对象
     * @author jie
     **/
    private ReplyMessage generateWordXmlDifference(QuestionInfo questionInfo) {
        ReplyMessage message = new ReplyMessage();
        String parentPath = ROOT_PATH + "/" + questionInfo.getQuestionType() + "/" + questionInfo.getId() + "/" + questionInfo.getUsername();
        ArrayList<String> list = new ArrayList<>();
        QuestionDiffWord questionDiffWord = new QuestionDiffWord();
        QuestionStepWord questionStepWord = new QuestionStepWord();
        for (int i = 1; i <= 30; i++) {
            String oldPath = parentPath + "/" + (i - 1) + ".docx";
            String newPath = parentPath + "/" + i + ".docx";
            try {
                list = XmlDiffUtils.dealXmlDiff(oldPath, newPath, parentPath);
            } catch (Exception e) {
                e.printStackTrace();
                message.setSuccess(false);
                message.setMessage("第" + i + "步比对失败,请从这一步开始重新上传(如果不是您的文件格式有误或者这一步文件损坏导致的问题,请联系管理员)");
                return message;
            }
            questionStepWord.setStep(i);
            questionStepWord.setId(questionInfo.getId());
            QuestionStepWord stepWord = questionStepWordMapper.selectOne(questionStepWord);
            if (stepWord == null) {
                message.setSuccess(false);
                message.setMessage("您似乎没有进行第" + i + "步的上传,请重试");
                return message;
            }
            questionDiffWord.setStepId(stepWord.getStepId());
            questionDiffWordMapper.delete(questionDiffWord);
            QuestionDiffWord insertDiffWord = new QuestionDiffWord();
            insertDiffWord.setStepId(stepWord.getStepId());
            for (String diff : list) {
                insertDiffWord.setDifference(diff);
                questionDiffWordMapper.insert(insertDiffWord);
            }
        }
        message.setSuccess(true);
        return message;
    }
}
