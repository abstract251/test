package com.test.oes.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.entity.*;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.*;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.PaperService;
import com.test.oes.service.StudentExamSessionService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentExamSessionServiceImpl implements StudentExamSessionService {

    private static final int STATUS_IN_PROGRESS = 0;
    private static final int STATUS_SUBMITTED = 1;
    private static final int POINTS_PER_QUESTION = 2;

    private final ExamManageMapper examManageMapper;
    private final ExamSnapshotService examSnapshotService;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final ExamAttemptMapper examAttemptMapper;
    private final ScoreMapper scoreMapper;
    private final PaperService paperService;
    private final ExamTimeHelper examTimeHelper;
    private final ObjectMapper objectMapper;
    private final MultiQuestionMapper multiQuestionMapper;
    private final FillQuestionMapper fillQuestionMapper;
    private final JudgeQuestionMapper judgeQuestionMapper;

    @Override
    public Map<String, Object> startOrResumeAttempt(Integer examCode, Student student) {
        ExamManage exam = requireExamForStudent(examCode, student);
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (!examTimeHelper.isWithinExamWindow(exam, now)) {
            throw new ExamBusinessException(400, "当前不在本场考试开放时间内");
        }
        Map<Integer, List<?>> fullPaper = examSnapshotService.buildFrozenPaperMap(examCode);
        Map<Integer, List<?>> masked = maskPaper(fullPaper);

        int sid = parseStudentId(student);
        ExamAttempt existing = examAttemptMapper.findByExamAndStudent(examCode, sid);
        if (existing != null) {
            if (Objects.equals(existing.getStatus(), STATUS_SUBMITTED)) {
                throw new ExamBusinessException(400, "您已交卷，不能重复进入");
            }
            return buildStartPayload(existing, exam, now, masked);
        }

        ExamAttempt row = new ExamAttempt();
        row.setExamCode(examCode);
        row.setStudentId(sid);
        row.setStatus(STATUS_IN_PROGRESS);
        row.setAnswersJson("{}");
        row.setStartedAt(now);
        row.setSubmittedAt(null);
        examAttemptMapper.insert(row);

        ExamAttempt inserted = examAttemptMapper.findByExamAndStudent(examCode, sid);
        return buildStartPayload(inserted, exam, now, masked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnswers(Integer examCode, Student student, Map<String, String> answers) {
        ExamManage exam = requireExamForStudent(examCode, student);
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (!examTimeHelper.isWithinExamWindow(exam, now)) {
            throw new ExamBusinessException(400, "考试已结束，无法保存作答");
        }
        int sid = parseStudentId(student);
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudent(examCode, sid);
        if (attempt == null) {
            throw new ExamBusinessException(400, "请先开始考试");
        }
        if (Objects.equals(attempt.getStatus(), STATUS_SUBMITTED)) {
            throw new ExamBusinessException(400, "已交卷，不能再保存");
        }
        Map<String, String> merged = readAnswersMap(attempt.getAnswersJson());
        if (answers != null) {
            merged.putAll(answers);
        }
        try {
            examAttemptMapper.updateAnswers(attempt.getAttemptId(), objectMapper.writeValueAsString(merged));
        } catch (Exception e) {
            throw new ExamBusinessException(500, "保存答案失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitAttempt(Integer examCode, Student student) {
        ExamManage exam = requireExamForStudent(examCode, student);
        LocalDateTime now = examTimeHelper.nowShanghai();
        if (!examTimeHelper.isWithinExamWindow(exam, now)) {
            throw new ExamBusinessException(400, "不在允许交卷的时间范围内");
        }
        int sid = parseStudentId(student);
        if (scoreMapper.findByExamAndStudent(examCode, sid) != null) {
            throw new ExamBusinessException(400, "成绩已存在，请勿重复提交");
        }
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudent(examCode, sid);
        if (attempt == null) {
            throw new ExamBusinessException(400, "请先开始考试");
        }
        if (Objects.equals(attempt.getStatus(), STATUS_SUBMITTED)) {
            throw new ExamBusinessException(400, "已交卷");
        }

        Map<String, String> answers = readAnswersMap(attempt.getAnswersJson());
        examSnapshotService.ensureSharedSnapshot(examCode);
        List<ExamSharedSnapshotItem> items = examSharedSnapshotItemMapper.findByExamCode(examCode);
        if (items.isEmpty()) {
            throw new ExamBusinessException(400, "本场考试暂无有效题目，无法判分");
        }
        int correct = 0;
        for (ExamSharedSnapshotItem it : items) {
            String key = it.getQuestionType() + "_" + it.getQuestionId();
            String user = answers.get(key);
            if (user == null) {
                user = "";
            }
            if (isAnswerCorrect(it.getQuestionType(), it.getQuestionId(), user)) {
                correct++;
            }
        }
        int maxScore = paperService.getMaxScore(exam.getPaperId());
        int etScore = correct * POINTS_PER_QUESTION;
        if (maxScore > 0 && etScore > maxScore) {
            etScore = maxScore;
        }
        int pass = maxScore > 0 && etScore * 100 / maxScore >= 60 ? 1 : 0;

        Score score = new Score();
        score.setExamCode(examCode);
        score.setStudentId(sid);
        score.setSubject(exam.getSource() == null ? "" : exam.getSource());
        score.setPtScore(pass);
        score.setEtScore(etScore);
        score.setScore(maxScore);
        score.setAnswerDate(now.format(DateTimeFormatter.ISO_LOCAL_DATE));
        scoreMapper.add(score);

        examAttemptMapper.updateStatus(attempt.getAttemptId(), STATUS_SUBMITTED, now);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("etScore", etScore);
        res.put("maxScore", maxScore);
        res.put("passed", pass == 1);
        res.put("correctCount", correct);
        res.put("totalQuestions", items.size());
        return res;
    }

    private ExamManage requireExamForStudent(Integer examCode, Student student) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (examTimeHelper.isRevoked(exam)) {
            throw new ExamBusinessException(410, "本场考试已撤销");
        }
        if (!StudentExamQueryServiceImpl.matchesScope(exam, student)) {
            throw new ExamBusinessException(403, "您不在本场考试的参考范围内");
        }
        return exam;
    }

    private int parseStudentId(Student student) {
        int studentId = student.getStudentId();
        if (studentId <= 0) {
            throw new ExamBusinessException(500, "学生账号异常");
        }
        return studentId;
    }

    private Map<String, Object> buildStartPayload(ExamAttempt attempt, ExamManage exam, LocalDateTime now,
                                                  Map<Integer, List<?>> maskedPaper) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("attemptId", attempt.getAttemptId());
        body.put("serverTime", now);
        body.put("windowEndAt", examTimeHelper.examWindowEnd(exam));
        body.put("paper", maskedPaper);
        body.put("exam", buildExamMeta(exam, attempt, now));
        try {
            body.put("answers", readAnswersMap(attempt.getAnswersJson()));
        } catch (Exception e) {
            body.put("answers", Collections.emptyMap());
        }
        return body;
    }

    private Map<String, Object> buildExamMeta(ExamManage exam, ExamAttempt attempt, LocalDateTime now) {
        Map<String, Object> meta = new LinkedHashMap<>();
        LocalDateTime startAt = examTimeHelper.effectiveExamStart(exam);
        meta.put("examCode", exam.getExamCode());
        meta.put("source", exam.getSource());
        meta.put("description", exam.getDescription());
        meta.put("type", exam.getType());
        meta.put("tips", exam.getTips());
        meta.put("examDate", startAt == null ? exam.getExamDate() : examTimeHelper.formatExamDateDisplay(startAt));
        meta.put("examStartAt", startAt);
        meta.put("totalTime", exam.getTotalTime());
        meta.put("totalScore", exam.getTotalScore() != null || exam.getPaperId() == null
                ? exam.getTotalScore()
                : paperService.getMaxScore(exam.getPaperId()));
        meta.put("grade", exam.getGrade());
        meta.put("major", exam.getMajor());
        meta.put("institute", exam.getInstitute());
        meta.put("freezeAt", examTimeHelper.freezeInstant(exam));
        meta.put("windowEndAt", examTimeHelper.examWindowEnd(exam));
        meta.put("paperLocked", examTimeHelper.isPaperLocked(exam, now));
        meta.put("revoked", examTimeHelper.isRevoked(exam));
        meta.put("revokeReason", exam.getRevokeReason());
        meta.put("inExamWindow", examTimeHelper.isWithinExamWindow(exam, now));
        meta.put("attemptStatus", Objects.equals(attempt.getStatus(), STATUS_SUBMITTED) ? "SUBMITTED" : "IN_PROGRESS");
        meta.put("startedAt", attempt.getStartedAt());
        meta.put("submittedAt", attempt.getSubmittedAt());
        meta.put("canEnter", true);
        return meta;
    }

    private Map<String, String> readAnswersMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, List<?>> maskPaper(Map<Integer, List<?>> raw) {
        Map<Integer, List<?>> out = new HashMap<>();
        try {
            List<MultiQuestion> m = (List<MultiQuestion>) (List<?>) raw.getOrDefault(1, List.of());
            List<MultiQuestion> mc = new ArrayList<>();
            for (MultiQuestion q : m) {
                MultiQuestion c = objectMapper.readValue(objectMapper.writeValueAsString(q), MultiQuestion.class);
                c.setRightAnswer(null);
                c.setAnalysis(null);
                mc.add(c);
            }
            out.put(1, mc);

            List<FillQuestion> f = (List<FillQuestion>) (List<?>) raw.getOrDefault(2, List.of());
            List<FillQuestion> fc = new ArrayList<>();
            for (FillQuestion q : f) {
                FillQuestion c = objectMapper.readValue(objectMapper.writeValueAsString(q), FillQuestion.class);
                c.setAnswer(null);
                c.setAnalysis(null);
                fc.add(c);
            }
            out.put(2, fc);

            List<JudgeQuestion> j = (List<JudgeQuestion>) (List<?>) raw.getOrDefault(3, List.of());
            List<JudgeQuestion> jc = new ArrayList<>();
            for (JudgeQuestion q : j) {
                JudgeQuestion c = objectMapper.readValue(objectMapper.writeValueAsString(q), JudgeQuestion.class);
                c.setAnswer(null);
                c.setAnalysis(null);
                jc.add(c);
            }
            out.put(3, jc);
        } catch (Exception e) {
            throw new ExamBusinessException(500, "试卷脱敏失败");
        }
        return out;
    }

    private boolean isAnswerCorrect(int type, int questionId, String userRaw) {
        String user = userRaw == null ? "" : userRaw.trim();
        return switch (type) {
            case 1 -> {
                MultiQuestion q = multiQuestionMapper.findByQuestionId(questionId);
                if (q == null || q.getRightAnswer() == null) {
                    yield false;
                }
                if (user.isEmpty()) {
                    yield false;
                }
                String right = q.getRightAnswer().trim();
                String u = user.substring(0, 1).toUpperCase(Locale.ROOT);
                String r = right.isEmpty() ? "" : right.substring(0, 1).toUpperCase(Locale.ROOT);
                yield r.equals(u) || right.equalsIgnoreCase(user.trim());
            }
            case 2 -> {
                FillQuestion q = fillQuestionMapper.findByQuestionId(questionId);
                if (q == null || q.getAnswer() == null) {
                    yield false;
                }
                yield normalizeText(q.getAnswer()).equals(normalizeText(user));
            }
            case 3 -> {
                JudgeQuestion q = judgeQuestionMapper.findByQuestionId(questionId);
                if (q == null || q.getAnswer() == null) {
                    yield false;
                }
                yield normalizeText(q.getAnswer()).equals(normalizeText(user));
            }
            default -> false;
        };
    }

    private static String normalizeText(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
