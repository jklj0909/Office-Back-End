package com.office.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.office.student.repository")
public class OfficeStudent {
    public static void main(String[] args) {
        SpringApplication.run(OfficeStudent.class);
    }
}
