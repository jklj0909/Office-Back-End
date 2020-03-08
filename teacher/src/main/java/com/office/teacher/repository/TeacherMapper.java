package com.office.teacher.repository;

import com.office.teacher.entity.Teacher;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface TeacherMapper extends Mapper<Teacher> {
}
