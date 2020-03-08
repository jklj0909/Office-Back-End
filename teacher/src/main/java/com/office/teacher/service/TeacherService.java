package com.office.teacher.service;

import com.office.common.entity.ReplyMessage;
import com.office.common.utils.CodecUtils;
import com.office.teacher.entity.Teacher;
import com.office.teacher.entity.TeacherInfo;
import com.office.teacher.repository.TeacherMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;

@Service
public class TeacherService {
    private TeacherMapper teacherMapper;

    @Autowired
    public void setTeacherMapper(TeacherMapper TeacherMapper) {
        this.teacherMapper = TeacherMapper;
    }

    /**
     * @param Teacher 登录的详细信息
     * @return 登录成功与否的详细信息
     * @author: jie
     */
    public ReplyMessage<TeacherInfo> login(Teacher Teacher) {
        ReplyMessage<TeacherInfo> message = new ReplyMessage();
        Teacher record = new Teacher();
        record.setUsername(Teacher.getUsername());
        List<Teacher> Teachers = teacherMapper.select(record);
        if (CollectionUtils.isEmpty(Teachers)) {
            message.setSuccess(false);
            message.setMessage("用户名或密码错误");
        } else {
            Teacher stu = Teachers.get(0);
            String password = CodecUtils.md5Hex(Teacher.getPassword(), stu.getToken());
            if (StringUtils.equals(password, stu.getPassword())) {
                message.setSuccess(true);
                TeacherInfo TeacherInfo = new TeacherInfo(stu.getUsername());
                message.setInfo(TeacherInfo);
            } else {
                message.setSuccess(false);
                message.setMessage("用户名或密码错误");
            }
        }
        return message;
    }
}
