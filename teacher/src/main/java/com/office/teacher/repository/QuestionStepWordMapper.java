package com.office.teacher.repository;

import com.office.common.entity.QuestionStep;
import com.office.common.entity.step.QuestionStepWord;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface QuestionStepWordMapper extends Mapper<QuestionStepWord> {
}
