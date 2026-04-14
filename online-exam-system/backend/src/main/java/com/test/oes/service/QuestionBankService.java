package com.test.oes.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.vo.QuestionBankItemVO;

import java.util.Map;

public interface QuestionBankService {

    Page<QuestionBankItemVO> findAll(Integer page,
                                     Integer size,
                                     Integer questionType,
                                     String subject,
                                     String keyword);

    Map<String, Object> deleteQuestion(Integer questionType, Integer questionId);
}
