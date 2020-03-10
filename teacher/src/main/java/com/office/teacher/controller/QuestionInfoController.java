package com.office.teacher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionMessage;
import com.office.common.entity.QuestionStep;
import com.office.common.entity.ReplyMessage;
import com.office.common.entity.step.QuestionStepWord;
import com.office.common.utils.CookieUtils;
import com.office.teacher.entity.Teacher;
import com.office.teacher.entity.TeacherInfo;
import com.office.teacher.service.QuestionInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("mark")
public class QuestionInfoController {
    private QuestionInfoService questionInfoService;
    private static final String USER_COOKIE = "UserCookie";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public void setQuestionInfoService(QuestionInfoService questionInfoService) {
        this.questionInfoService = questionInfoService;
    }

    /**
     * 创建试题
     *
     * @param questionInfo 题目对象
     * @author jie
     **/
    @PostMapping("createQuestion")
    public ResponseEntity<ReplyMessage> createQuestion(@RequestBody QuestionInfo questionInfo) {
        ReplyMessage message = new ReplyMessage();
        try {
            questionInfoService.createQuestion(questionInfo);
        } catch (Exception e) {
            message.setSuccess(false);
            message.setMessage("标题或描述信息过长");
            return ResponseEntity.badRequest().body(message);
        }
        message.setSuccess(true);
        message.setMessage(questionInfo.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * 检查试题id是否存在
     *
     * @param id 题目的id
     * @author jie
     **/
    @GetMapping("checkId/{id}")
    public ResponseEntity<ReplyMessage<QuestionMessage>> checkIdIfExist(HttpServletRequest request, @PathVariable String id) {
        String value = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        String username = "";
        try {
            username = MAPPER.readValue(value, TeacherInfo.class).getUsername();
        } catch (Exception e) {
            username = null;
        }
        ReplyMessage<QuestionMessage> message = questionInfoService.checkIdIfExist(id, username);
        if (!message.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
        }
        return ResponseEntity.ok(message);
    }

    /**
     * 上传素材word
     *
     * @param id         题目的id
     * @param uploadFile 上传的文件
     * @author jie
     **/
    @PostMapping("upload/raw/{type}/{id}")
    public ResponseEntity<ReplyMessage> uploadRawFile(@RequestParam("file") MultipartFile uploadFile, @PathVariable("type") String type, @PathVariable("id") String id) {
        try {
            ReplyMessage message = questionInfoService.uploadRawFile(uploadFile, type, id);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("upload/step/{type}/{id}/{step}")
    public ResponseEntity<ReplyMessage> uploadStepFile(@RequestParam("file") MultipartFile uploadFile, @PathVariable("type") String type,
                                                       @PathVariable("id") String id, @PathVariable("step") Integer step) {
        try {
            ReplyMessage message = questionInfoService.uploadStepFile(uploadFile, type, id, step);
            if (message.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("stepDescription")
    public ResponseEntity submitStepDescription(@RequestBody QuestionStep questionStep) {
        try {
            questionInfoService.addQuestionStep(questionStep);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
