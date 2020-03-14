package com.office.teacher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.common.entity.ReplyMessage;
import com.office.common.utils.CookieUtils;
import com.office.teacher.entity.Teacher;
import com.office.teacher.entity.TeacherInfo;
import com.office.teacher.service.TeacherService;
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
@RequestMapping("teacher")
public class TeacherController {
    private TeacherService teacherService;

    private static final String USER_COOKIE = "UserCookie";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public void setTeacherService(TeacherService TeacherService) {
        this.teacherService = TeacherService;
    }

    /**
     * 用户登录检验
     *
     * @param teacher 用户对象
     * @author jie
     **/
    @PostMapping("login")
    public ResponseEntity<ReplyMessage<TeacherInfo>> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Teacher teacher) {
        ReplyMessage<TeacherInfo> message = teacherService.login(teacher);
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

    /**
     * 检验用户登录状况
     *
     * @author jie
     **/
    @GetMapping("checkLogin")
    public ResponseEntity<ReplyMessage> checkLogin(HttpServletRequest request) {
        ReplyMessage message = new ReplyMessage();
        String value = CookieUtils.getCookieValue(request, USER_COOKIE, true);
        if (StringUtils.isBlank(value)) {
            return ResponseEntity.notFound().build();
        }
        try {
            message.setInfo(MAPPER.readValue(value, Object.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    /**
     * 登出
     *
     * @author jie
     **/
    @PostMapping("logout")
    public ResponseEntity logout(HttpServletResponse response) {
        CookieUtils.removeCookie(response, USER_COOKIE);
        return ResponseEntity.noContent().build();
    }
}
