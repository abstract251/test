package com.test.oes.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.FillQuestionMapper;
import com.test.oes.mapper.JudgeQuestionMapper;
import com.test.oes.mapper.MultiQuestionMapper;
import com.test.oes.mapper.PaperMapper;
import com.test.oes.mapper.QuestionBankMapper;
import com.test.oes.service.QuestionBankService;
import com.test.oes.service.exam.ExamPaperEditPolicy;
import com.test.oes.vo.QuestionBankItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;
    private final PaperMapper paperMapper;
    private final MultiQuestionMapper multiQuestionMapper;
    private final FillQuestionMapper fillQuestionMapper;
    private final JudgeQuestionMapper judgeQuestionMapper;
    private final ExamPaperEditPolicy examPaperEditPolicy;

    @Override
    public Page<QuestionBankItemVO> findAll(Integer page,
                                            Integer size,
                                            Integer questionType,
                                            String subject,
                                            String keyword) {
        int current = normalizePage(page);
        int pageSize = normalizeSize(size);
        Integer normalizedType = normalizeQuestionType(questionType, false);
        String normalizedSubject = trimToNull(subject);
        String normalizedKeyword = trimToNull(keyword);

        long total = countQuestions(normalizedType, normalizedSubject, normalizedKeyword);
        List<QuestionBankItemVO> records = List.of();
        if (total > 0) {
            long offset = (long) (current - 1) * pageSize;
            records = selectPage(offset, pageSize, normalizedType, normalizedSubject, normalizedKeyword);
            for (QuestionBankItemVO item : records) {
                item.setQuestionTypeName(typeName(item.getQuestionType()));
            }
        }

        Page<QuestionBankItemVO> result = new Page<>(current, pageSize);
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    private long countQuestions(Integer questionType, String subject, String keyword) {
        if (questionType == null) {
            return questionBankMapper.countMulti(subject, keyword)
                    + questionBankMapper.countFill(subject, keyword)
                    + questionBankMapper.countJudge(subject, keyword);
        }
        return switch (questionType) {
            case 1 -> questionBankMapper.countMulti(subject, keyword);
            case 2 -> questionBankMapper.countFill(subject, keyword);
            case 3 -> questionBankMapper.countJudge(subject, keyword);
            default -> 0L;
        };
    }

    private List<QuestionBankItemVO> selectPage(long offset,
                                                long pageSize,
                                                Integer questionType,
                                                String subject,
                                                String keyword) {
        if (questionType != null) {
            return switch (questionType) {
                case 1 -> questionBankMapper.selectMultiPage(offset, pageSize, subject, keyword);
                case 2 -> questionBankMapper.selectFillPage(offset, pageSize, subject, keyword);
                case 3 -> questionBankMapper.selectJudgePage(offset, pageSize, subject, keyword);
                default -> List.of();
            };
        }

        long fetchSize = offset + pageSize;
        List<QuestionBankItemVO> merged = new java.util.ArrayList<>();
        merged.addAll(questionBankMapper.selectMultiPage(0, fetchSize, subject, keyword));
        merged.addAll(questionBankMapper.selectFillPage(0, fetchSize, subject, keyword));
        merged.addAll(questionBankMapper.selectJudgePage(0, fetchSize, subject, keyword));
        merged.sort(Comparator
                .comparing(QuestionBankItemVO::getQuestionId, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(QuestionBankItemVO::getQuestionType, Comparator.nullsLast(Comparator.naturalOrder())));
        return merged.stream()
                .skip(offset)
                .limit(pageSize)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteQuestion(Integer questionType, Integer questionId) {
        Integer normalizedType = normalizeQuestionType(questionType, true);
        if (questionId == null || questionId <= 0) {
            throw new ExamBusinessException(400, "题目编号不合法");
        }
        assertQuestionExists(normalizedType, questionId);

        List<Integer> paperIds = paperMapper.findPaperIdsByQuestion(normalizedType, questionId);
        for (Integer paperId : paperIds) {
            examPaperEditPolicy.assertPaperEditable(paperId);
        }

        int relationRows = paperMapper.deleteByQuestion(normalizedType, questionId);
        int questionRows = deleteQuestionRow(normalizedType, questionId);
        if (questionRows == 0) {
            throw new ExamBusinessException(500, "题库删除失败");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionType", normalizedType);
        result.put("questionTypeName", typeName(normalizedType));
        result.put("questionId", questionId);
        result.put("deletedPaperRelations", relationRows);
        result.put("deletedQuestion", questionRows);
        return result;
    }

    private void assertQuestionExists(Integer questionType, Integer questionId) {
        boolean exists = switch (questionType) {
            case 1 -> {
                MultiQuestion question = multiQuestionMapper.findByQuestionId(questionId);
                yield question != null;
            }
            case 2 -> {
                FillQuestion question = fillQuestionMapper.findByQuestionId(questionId);
                yield question != null;
            }
            case 3 -> {
                JudgeQuestion question = judgeQuestionMapper.findByQuestionId(questionId);
                yield question != null;
            }
            default -> false;
        };
        if (!exists) {
            throw new ExamBusinessException(404, "题目不存在");
        }
    }

    private int deleteQuestionRow(Integer questionType, Integer questionId) {
        return switch (questionType) {
            case 1 -> multiQuestionMapper.deleteByQuestionId(questionId);
            case 2 -> fillQuestionMapper.deleteByQuestionId(questionId);
            case 3 -> judgeQuestionMapper.deleteByQuestionId(questionId);
            default -> 0;
        };
    }

    private static int normalizePage(Integer page) {
        if (page == null || page <= 0) {
            throw new ExamBusinessException(400, "页码必须大于 0");
        }
        return page;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            throw new ExamBusinessException(400, "分页大小必须大于 0");
        }
        return size;
    }

    private static Integer normalizeQuestionType(Integer questionType, boolean required) {
        if (questionType == null) {
            if (required) {
                throw new ExamBusinessException(400, "题型不能为空");
            }
            return null;
        }
        if (questionType < 1 || questionType > 3) {
            throw new ExamBusinessException(400, "题型不合法，仅支持 1/2/3");
        }
        return questionType;
    }

    private static String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String typeName(Integer questionType) {
        if (questionType == null) {
            return "";
        }
        return switch (questionType) {
            case 1 -> "选择题";
            case 2 -> "填空题";
            case 3 -> "判断题";
            default -> "";
        };
    }
}
