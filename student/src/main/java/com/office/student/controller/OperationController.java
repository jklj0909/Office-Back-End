package com.office.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.common.entity.QuestionInfo;
import com.office.common.entity.ReplyMessage;
import com.office.common.utils.CookieUtils;
import com.office.student.entity.QuestionStepDetail;
import com.office.student.entity.StudentInfo;
import com.office.student.entity.StudentQuestionStep;
import com.office.student.service.OperationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("operation")
public class OperationController {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String USER_COOKIE = "UserCookie";
    private OperationService operationService;

    @Autowired
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("download/{type}/{id}/{filename}")
    public ResponseEntity downloadRawFile(HttpServletResponse response, @PathVariable("type") String type,
                                          @PathVariable("id") String id, @PathVariable("filename") String filename) {
        try {
            operationService.downloadRawFile(response, type, id, filename);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("getFilename/{type}/{id}")
    public ResponseEntity getFilename(@PathVariable("type") String type, @PathVariable("id") String id) {
        String filename = operationService.getFilename(type, id);
        if (StringUtils.isBlank(filename)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(filename);
    }

    @PostMapping("uploadAnswer/{questionType}/{id}")
    public ResponseEntity uploadAnswer(@RequestParam("file") MultipartFile uploadFile, HttpServletRequest request,
                                       @PathVariable("questionType") String questionType, @PathVariable("id") String id) {
        try {
            String studentUsername = getStudentUsername(request);
            if (StringUtils.isBlank(studentUsername)) {
                return ResponseEntity.badRequest().build();
            }
            operationService.uploadAnswer(uploadFile, questionType, id, studentUsername);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("submitAnswer/{questionType}/{id}")
    public ResponseEntity<ReplyMessage> submitAnswer(
            HttpServletRequest request,
            @PathVariable("questionType") String questionType, @PathVariable("id") String id) {
        String studentUsername = null;
        try {
            studentUsername = getStudentUsername(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (studentUsername == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReplyMessage message = operationService.submitAnswer(studentUsername, questionType, id);
            if (message.isSuccess()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
            } else {
                return ResponseEntity.badRequest().body(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping("checkIfExists")
    public ResponseEntity<QuestionInfo> checkIfExists(@RequestParam("id") String id) {
        QuestionInfo questionInfo = operationService.checkIfExists(id);
        if (questionInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questionInfo);
    }

    @RequestMapping("getStudentResult/{qid}")
    public ResponseEntity<ReplyMessage<List<QuestionStepDetail>>> getStudentResult(@PathVariable("qid") String qid) {
        try {
            ReplyMessage<List<QuestionStepDetail>> message = operationService.getStudentResult(qid);
            if (message.isSuccess()) {
                return ResponseEntity.ok(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("downloadStu/{studentQid}")
    public ResponseEntity downloadStu(HttpServletResponse response, @PathVariable("studentQid") String studentQid) {
        try {
            operationService.downloadStu(response, studentQid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("downloadTea/{studentQid}/{step}")
    public ResponseEntity downloadStu(HttpServletResponse response, @PathVariable("studentQid") String studentQid, @PathVariable("step") Integer step) {
        try {
            operationService.downloadTea(response, studentQid, step);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    protected String getStudentUsername(HttpServletRequest request) throws Exception {
        String cookieValue = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        if (StringUtils.isBlank(cookieValue)) {
            return null;
        }
        StudentInfo studentInfo = MAPPER.readValue(cookieValue, StudentInfo.class);
        if (studentInfo == null) {
            return null;
        }
        return studentInfo.getUsername();
    }
}
