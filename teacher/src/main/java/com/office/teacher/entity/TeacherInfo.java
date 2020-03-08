package com.office.teacher.entity;

import java.io.Serializable;

public class TeacherInfo implements Serializable {
    private String username;

    public TeacherInfo() {
    }

    public TeacherInfo(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
