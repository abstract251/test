package com.test.oes.service.impl;

import com.test.oes.entity.*;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.*;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamSnapshotServiceImpl implements ExamSnapshotService {

    private final ExamManageMapper examManageMapper;
    private final ExamSharedSnapshotMapper examSharedSnapshotMapper;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final MultiQuestionMapper multiQuestionMapper;
    private final FillQuestionMapper fillQuestionMapper;
    private final JudgeQuestionMapper judgeQuestionMapper;
    private final ExamTimeHelper examTimeHelper;
    private final ExamSharedSnapshotMaterializer snapshotMaterializer;

    @Override
    public boolean hasSharedSnapshot(Integer examCode) {
        return examSharedSnapshotMapper.countByExamCode(examCode) > 0;
    }

    @Override
    public void ensureSharedSnapshot(Integer examCode) {
        snapshotMaterializer.materializeIfAbsent(examCode);
    }

    @Override
    public Map<Integer, List<?>> buildFrozenPaperMap(Integer examCode) {
        List<ExamSharedSnapshotItem> items = requireSnapshotItems(examCode);
        return rebuildFrozenPaper(items);
    }

    @Override
    public Map<String, String> buildAnswerKeyMap(Integer examCode) {
        List<ExamSharedSnapshotItem> items = requireSnapshotItems(examCode);
        Map<Integer, MultiQuestion> multiMap = toMap(multiQuestionMapper.findByQuestionIds(extractQuestionIds(items, 1)), MultiQuestion::getQuestionId);
        Map<Integer, FillQuestion> fillMap = toMap(fillQuestionMapper.findByQuestionIds(extractQuestionIds(items, 2)), FillQuestion::getQuestionId);
        Map<Integer, JudgeQuestion> judgeMap = toMap(judgeQuestionMapper.findByQuestionIds(extractQuestionIds(items, 3)), JudgeQuestion::getQuestionId);

        Map<String, String> answerKey = new LinkedHashMap<>();
        for (ExamSharedSnapshotItem item : items) {
            String key = item.getQuestionType() + "_" + item.getQuestionId();
            switch (item.getQuestionType()) {
                case 1 -> answerKey.put(key, requireMultiQuestion(multiMap, item.getQuestionId()).getRightAnswer());
                case 2 -> answerKey.put(key, requireFillQuestion(fillMap, item.getQuestionId()).getAnswer());
                case 3 -> answerKey.put(key, requireJudgeQuestion(judgeMap, item.getQuestionId()).getAnswer());
                default -> throw new ExamBusinessException(500, "未知题型");
            }
        }
        return answerKey;
    }

    @Override
    public List<Integer> summarizeFrozenQuestionTypes(Integer examCode) {
        int[] counts = new int[]{0, 0, 0};
        for (Map<String, Object> row : examSharedSnapshotItemMapper.countGroupedByType(examCode)) {
            Integer questionType = toInteger(row.get("questionType"));
            Integer questionCount = toInteger(row.get("questionCount"));
            int index = questionType == null ? -1 : questionType - 1;
            if (index >= 0 && index < counts.length) {
                counts[index] = questionCount == null ? 0 : questionCount;
            }
        }
        return List.of(counts[0], counts[1], counts[2]);
    }

    private List<ExamSharedSnapshotItem> requireSnapshotItems(Integer examCode) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (examTimeHelper.isRevoked(exam)) {
            throw new ExamBusinessException(410, "本场考试已撤销");
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (examTimeHelper.shouldNotMaterializeSnapshot(exam, now)) {
            throw new ExamBusinessException(400, "尚未到达试卷冻结时刻，本场共用快照尚未就绪");
        }
        snapshotMaterializer.materializeIfAbsent(examCode);
        if (examSharedSnapshotMapper.countByExamCode(examCode) == 0) {
            throw new ExamBusinessException(500, "本场快照生成失败，请稍后重试");
        }
        List<ExamSharedSnapshotItem> items = examSharedSnapshotItemMapper.findByExamCode(examCode);
        if (items.isEmpty()) {
            throw new ExamBusinessException(500, "本场快照题目缺失，请稍后重试");
        }
        return items;
    }

    private Map<Integer, List<?>> rebuildFrozenPaper(List<ExamSharedSnapshotItem> items) {
        Map<Integer, MultiQuestion> multiMap = toMap(multiQuestionMapper.findByQuestionIds(extractQuestionIds(items, 1)), MultiQuestion::getQuestionId);
        Map<Integer, FillQuestion> fillMap = toMap(fillQuestionMapper.findByQuestionIds(extractQuestionIds(items, 2)), FillQuestion::getQuestionId);
        Map<Integer, JudgeQuestion> judgeMap = toMap(judgeQuestionMapper.findByQuestionIds(extractQuestionIds(items, 3)), JudgeQuestion::getQuestionId);

        List<MultiQuestion> multi = new ArrayList<>();
        List<FillQuestion> fill = new ArrayList<>();
        List<JudgeQuestion> judge = new ArrayList<>();
        for (ExamSharedSnapshotItem item : items) {
            switch (item.getQuestionType()) {
                case 1 -> multi.add(requireMultiQuestion(multiMap, item.getQuestionId()));
                case 2 -> fill.add(requireFillQuestion(fillMap, item.getQuestionId()));
                case 3 -> judge.add(requireJudgeQuestion(judgeMap, item.getQuestionId()));
                default -> throw new ExamBusinessException(500, "未知题型");
            }
        }
        Map<Integer, List<?>> map = new HashMap<>(3);
        map.put(1, multi);
        map.put(2, fill);
        map.put(3, judge);
        return map;
    }

    private List<Integer> extractQuestionIds(List<ExamSharedSnapshotItem> items, int questionType) {
        return items.stream()
                .filter(item -> Objects.equals(item.getQuestionType(), questionType))
                .map(ExamSharedSnapshotItem::getQuestionId)
                .distinct()
                .toList();
    }

    private <T> Map<Integer, T> toMap(List<T> rows, Function<T, Integer> idGetter) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream().collect(Collectors.toMap(idGetter, Function.identity(), (left, right) -> left));
    }

    private MultiQuestion requireMultiQuestion(Map<Integer, MultiQuestion> questions, Integer questionId) {
        MultiQuestion question = questions.get(questionId);
        if (question == null) {
            throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（选择题）");
        }
        return question;
    }

    private FillQuestion requireFillQuestion(Map<Integer, FillQuestion> questions, Integer questionId) {
        FillQuestion question = questions.get(questionId);
        if (question == null) {
            throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（填空题）");
        }
        return question;
    }

    private JudgeQuestion requireJudgeQuestion(Map<Integer, JudgeQuestion> questions, Integer questionId) {
        JudgeQuestion question = questions.get(questionId);
        if (question == null) {
            throw new ExamBusinessException(500, "快照题目缺失，请联系管理员（判断题）");
        }
        return question;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
