package com.office.teacher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.office.teacher.repository")
public class OfficeTeacher {
    public static void main(String[] args) {
        SpringApplication.run(OfficeTeacher.class);
    }
}
