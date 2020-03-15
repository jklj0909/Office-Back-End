package com.office.student.entity;

import java.io.Serializable;

public class StudentInfo implements Serializable {
    private String username;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StudentInfo() {
    }

    public StudentInfo(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Override
    public String toString() {
        return "StudentInfo{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
