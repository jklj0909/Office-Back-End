package com.office.teacher.controller;

import com.office.common.entity.QuestionInfo;
import com.office.common.entity.QuestionMessage;
import com.office.common.entity.ReplyMessage;
import com.office.teacher.service.QuestionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("mark")
public class QuestionInfoController {
    private QuestionInfoService questionInfoService;

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
    public ResponseEntity<ReplyMessage<QuestionMessage>> checkIdIfExist(@PathVariable String id) {
        ReplyMessage<QuestionMessage> message = questionInfoService.checkIdIfExist(id);
        if (!message.isSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    /**
     * 上传素材word
     *
     * @param id   题目的id
     * @param file 上传的文件
     * @author jie
     **/
    @PostMapping("upload/raw/{type}/{id}")
    public ResponseEntity uploadRawFile(@RequestParam MultipartFile file, @PathVariable("type") String type, @PathVariable("id") String id) {
        try {
            questionInfoService.uploadRawFile(file, type, id);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
