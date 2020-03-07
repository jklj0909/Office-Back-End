package com.office.student.service;

import com.office.common.entity.ReplyMessage;
import com.office.common.utils.CodecUtils;
import com.office.student.entity.Student;
import com.office.student.repository.StudentMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class StudentService {
    private StudentMapper studentMapper;

    @Autowired
    public void setStudentMapper(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * @param student 注册的学生用户信息
     * @return 注册成功与否的详细信息
     * @author: jie
     */
    public ReplyMessage register(Student student) {
        ReplyMessage message = new ReplyMessage();
        Example example = new Example(Student.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.orEqualTo("username", student.getUsername()).orEqualTo("email", student.getEmail());
        List<Student> students = studentMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(students)) {
            message.setSuccess(false);
            students.forEach(stu -> {
                if (StringUtils.equals(student.getEmail(), stu.getEmail())) {
                    message.setMessage("此邮箱已被使用");
                    return;
                } else if (StringUtils.equals(student.getUsername(), stu.getUsername())) {
                    message.setMessage("此用户名已被使用");
                    return;
                }
            });
        } else {
            student.setId(CodecUtils.generateUUID());
            student.setCreateTime(new Timestamp(new Date().getTime()));
            student.setToken(CodecUtils.generateSalt());
            student.setPassword(CodecUtils.md5Hex(student.getPassword(), student.getToken()));
            studentMapper.insert(student);
            message.setSuccess(true);
        }
        return message;
    }

    /**
     * @param student 登录的详细信息
     * @return 登录成功与否的详细信息
     * @author: jie
     */
    public ReplyMessage login(Student student) {
        ReplyMessage message = new ReplyMessage();

        return message;
    }
}
