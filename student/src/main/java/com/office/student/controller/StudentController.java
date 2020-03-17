package com.office.student.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.common.entity.wrap.ReplyMessage;
import com.office.common.utils.CookieUtils;
import com.office.student.entity.Student;
import com.office.student.entity.StudentInfo;
import com.office.student.service.StudentService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("student")
public class StudentController {
    private StudentService studentService;

    private static final String USER_COOKIE = "UserCookie";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("register")
    public ResponseEntity<ReplyMessage> register(@RequestBody Student student) {
        ReplyMessage message = studentService.register(student);
        if (!message.isSuccess()) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("login")
    public ResponseEntity<ReplyMessage<StudentInfo>> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Student student) {
        ReplyMessage<StudentInfo> message = studentService.login(student);
        if (!message.isSuccess()) {
            return ResponseEntity.badRequest().body(message);
        }
        try {
            String cookieValue = MAPPER.writeValueAsString(message.getInfo());
            CookieUtils.setCookie(request, response, USER_COOKIE, cookieValue, 60 * 60);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @GetMapping("checkLogin")
    public ResponseEntity<ReplyMessage> checkLogin(HttpServletRequest request) {
        ReplyMessage message = new ReplyMessage();
        String value = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        if (StringUtils.isBlank(value)) {
            return ResponseEntity.notFound().build();
        }
        try {
            message.setInfo(MAPPER.readValue(value, StudentInfo.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    @PostMapping("logout")
    public ResponseEntity logout(HttpServletResponse response) {
        CookieUtils.removeCookie(response, USER_COOKIE);
        return ResponseEntity.noContent().build();
    }
}
