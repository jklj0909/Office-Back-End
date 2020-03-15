package com.office.student.repository;

import com.office.student.entity.StudentQuestionInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface StudentQuestionInfoMapper extends Mapper<StudentQuestionInfo> {
}
