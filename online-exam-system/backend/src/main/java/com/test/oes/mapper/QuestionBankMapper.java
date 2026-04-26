package com.test.oes.mapper;

import com.test.oes.vo.QuestionBankItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionBankMapper {

    long countMulti(@Param("subject") String subject, @Param("keyword") String keyword);

    long countFill(@Param("subject") String subject, @Param("keyword") String keyword);

    long countJudge(@Param("subject") String subject, @Param("keyword") String keyword);

    List<QuestionBankItemVO> selectMultiPage(@Param("offset") long offset,
                                             @Param("size") long size,
                                             @Param("subject") String subject,
                                             @Param("keyword") String keyword);

    List<QuestionBankItemVO> selectFillPage(@Param("offset") long offset,
                                            @Param("size") long size,
                                            @Param("subject") String subject,
                                            @Param("keyword") String keyword);

    List<QuestionBankItemVO> selectJudgePage(@Param("offset") long offset,
                                             @Param("size") long size,
                                             @Param("subject") String subject,
                                             @Param("keyword") String keyword);
}
