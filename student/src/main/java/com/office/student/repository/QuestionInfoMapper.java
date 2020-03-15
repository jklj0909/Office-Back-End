package com.office.student.repository;

import com.office.common.entity.QuestionInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface QuestionInfoMapper extends Mapper<QuestionInfo> {
}
