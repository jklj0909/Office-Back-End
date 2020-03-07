package com.office.student.controller;

import com.office.common.entity.ReplyMessage;
import com.office.student.entity.Student;
import com.office.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("student")
public class StudentController {
    private StudentService studentService;

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
    public ResponseEntity<ReplyMessage> login(@RequestBody Student student) {
        ReplyMessage message = studentService.login(student);
        if (!message.isSuccess()) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
