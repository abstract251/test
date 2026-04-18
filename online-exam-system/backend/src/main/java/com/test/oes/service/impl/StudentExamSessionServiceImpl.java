package com.test.oes.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.cache.CacheProperties;
import com.test.oes.cache.DraftCacheService;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.cache.StudentDraftCacheValue;
import com.test.oes.entity.*;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.*;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.PaperService;
import com.test.oes.service.StudentExamSessionService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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
    private final ExamAttemptMapper examAttemptMapper;
    private final ScoreMapper scoreMapper;
    private final PaperService paperService;
    private final ExamTimeHelper examTimeHelper;
    private final ObjectMapper objectMapper;
    private final DraftCacheService draftCacheService;
    private final CacheProperties cacheProperties;
    private final ExamCacheFacade examCacheFacade;

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
            Map<String, Object> payload = buildStartPayload(existing, exam, now, masked, resolveLatestAnswers(examCode, sid, existing, exam));
            invalidateStudentEntryCaches(sid, examCode);
            return payload;
        }

        ExamAttempt row = new ExamAttempt();
        row.setExamCode(examCode);
        row.setStudentId(sid);
        row.setStatus(STATUS_IN_PROGRESS);
        row.setAnswersJson("{}");
        row.setStartedAt(now);
        row.setSubmittedAt(null);
        try {
            examAttemptMapper.insert(row);
        } catch (DuplicateKeyException duplicateKeyException) {
            ExamAttempt concurrent = examAttemptMapper.findByExamAndStudent(examCode, sid);
            if (concurrent == null) {
                throw duplicateKeyException;
            }
            if (Objects.equals(concurrent.getStatus(), STATUS_SUBMITTED)) {
                throw new ExamBusinessException(400, "您已交卷，不能重复进入");
            }
            Map<String, Object> payload = buildStartPayload(concurrent, exam, now, masked, resolveLatestAnswers(examCode, sid, concurrent, exam));
            invalidateStudentEntryCaches(sid, examCode);
            return payload;
        }

        ExamAttempt inserted = examAttemptMapper.findByExamAndStudent(examCode, sid);
        Map<String, Object> payload = buildStartPayload(inserted, exam, now, masked, resolveLatestAnswers(examCode, sid, inserted, exam));
        invalidateStudentEntryCaches(sid, examCode);
        return payload;
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
        if (draftCacheService.isEnabled()) {
            StudentDraftCacheValue draft = loadOrBackfillDraft(examCode, sid, attempt, exam);
            Map<String, String> merged = new HashMap<>(draft.getAnswers() == null ? Map.of() : draft.getAnswers());
            if (answers != null) {
                merged.putAll(answers);
            }
            draft.setAnswers(merged);
            draft.setDirty(Boolean.TRUE);
            draft.setUpdatedAt(now);
            LocalDateTime lastPersistedAt = draft.getLastPersistedAt();
            boolean shouldPersist = lastPersistedAt == null
                    || !lastPersistedAt.plus(cacheProperties.getDraft().getPersistInterval()).isAfter(now);
            if (shouldPersist) {
                persistAnswers(attempt.getAttemptId(), merged);
                draft.setLastPersistedAt(now);
                draft.setDirty(Boolean.FALSE);
            }
            boolean redisSaved = draftCacheService.saveDraft(examCode, sid, draft, resolveDraftTtl(exam));
            if (redisSaved) {
                return;
            }
        }
        Map<String, String> merged = readAnswersMap(attempt.getAnswersJson());
        if (answers != null) {
            merged.putAll(answers);
        }
        persistAnswers(attempt.getAttemptId(), merged);
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
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudentForUpdate(examCode, sid);
        if (attempt == null) {
            throw new ExamBusinessException(400, "请先开始考试");
        }
        if (Objects.equals(attempt.getStatus(), STATUS_SUBMITTED)) {
            throw new ExamBusinessException(400, "已交卷");
        }
        if (scoreMapper.findByExamAndStudent(examCode, sid) != null) {
            throw new ExamBusinessException(400, "成绩已存在，请勿重复提交");
        }

        Map<String, String> answers = resolveLatestAnswers(examCode, sid, attempt, exam);
        persistAnswers(attempt.getAttemptId(), answers);
        Map<String, String> answerKey = examSnapshotService.buildAnswerKeyMap(examCode);
        if (answerKey.isEmpty()) {
            throw new ExamBusinessException(400, "本场考试暂无有效题目，无法判分");
        }
        int correct = 0;
        for (Map.Entry<String, String> entry : answerKey.entrySet()) {
            String user = answers.get(entry.getKey());
            if (user == null) {
                user = "";
            }
            if (isAnswerCorrect(entry.getValue(), user)) {
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
        draftCacheService.deleteDraft(examCode, sid);
        invalidateStudentEntryCaches(sid, examCode);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("etScore", etScore);
        res.put("maxScore", maxScore);
        res.put("passed", pass == 1);
        res.put("correctCount", correct);
        res.put("totalQuestions", answerKey.size());
        return res;
    }

    private ExamManage requireExamForStudent(Integer examCode, Student student) {
        ExamManage rawExam = examManageMapper.findById(examCode);
        if (rawExam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        ExamManage exam = examManageMapper.findVisibleByExamCodeForStudent(
                examCode,
                student.getGrade(),
                student.getMajor(),
                student.getInstitute());
        if (exam == null) {
            throw new ExamBusinessException(403, "您不在本场考试的参考范围内");
        }
        if (examTimeHelper.isRevoked(exam)) {
            throw new ExamBusinessException(410, "本场考试已撤销");
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
                                                  Map<Integer, List<?>> maskedPaper,
                                                  Map<String, String> answers) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("attemptId", attempt.getAttemptId());
        body.put("serverTime", now);
        body.put("windowEndAt", examTimeHelper.examWindowEnd(exam));
        body.put("paper", maskedPaper);
        body.put("exam", buildExamMeta(exam, attempt, now));
        body.put("answers", answers == null ? Collections.emptyMap() : answers);
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

    private boolean isAnswerCorrect(String rightAnswer, String userRaw) {
        String user = userRaw == null ? "" : userRaw.trim();
        if (rightAnswer == null || user.isEmpty()) {
            return false;
        }
        String right = rightAnswer.trim();
        if (right.isEmpty()) {
            return false;
        }
        String userFirst = user.substring(0, 1).toUpperCase(Locale.ROOT);
        String rightFirst = right.substring(0, 1).toUpperCase(Locale.ROOT);
        if (right.length() == 1 || user.length() == 1) {
            return rightFirst.equals(userFirst) || normalizeText(right).equals(normalizeText(user));
        }
        return normalizeText(right).equals(normalizeText(user));
    }

    private static String normalizeText(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private Map<String, String> resolveLatestAnswers(Integer examCode, Integer studentId, ExamAttempt attempt, ExamManage exam) {
        if (!draftCacheService.isEnabled()) {
            return readAnswersMap(attempt.getAnswersJson());
        }
        StudentDraftCacheValue draft = loadOrBackfillDraft(examCode, studentId, attempt, exam);
        if (draft.getAnswers() == null) {
            return new HashMap<>();
        }
        return new HashMap<>(draft.getAnswers());
    }

    private StudentDraftCacheValue loadOrBackfillDraft(Integer examCode, Integer studentId, ExamAttempt attempt, ExamManage exam) {
        StudentDraftCacheValue draft = draftCacheService.getDraft(examCode, studentId);
        if (draft != null) {
            return draft;
        }
        StudentDraftCacheValue created = new StudentDraftCacheValue();
        created.setAnswers(readAnswersMap(attempt.getAnswersJson()));
        created.setDirty(Boolean.FALSE);
        created.setUpdatedAt(examTimeHelper.nowShanghai());
        created.setLastPersistedAt(attempt.getStartedAt());
        if (!draftCacheService.saveDraft(examCode, studentId, created, resolveDraftTtl(exam))) {
            return created;
        }
        return created;
    }

    private void persistAnswers(Long attemptId, Map<String, String> answers) {
        try {
            examAttemptMapper.updateAnswers(attemptId, objectMapper.writeValueAsString(answers == null ? Map.of() : answers));
        } catch (Exception e) {
            throw new ExamBusinessException(500, "保存答案失败");
        }
    }

    private java.time.Duration resolveDraftTtl(ExamManage exam) {
        LocalDateTime windowEndAt = examTimeHelper.examWindowEnd(exam);
        if (windowEndAt == null) {
            return cacheProperties.getDraft().getPostExamTtl();
        }
        java.time.Duration ttl = java.time.Duration.between(
                examTimeHelper.nowShanghai(),
                windowEndAt.plus(cacheProperties.getDraft().getPostExamTtl()));
        return ttl.isNegative() || ttl.isZero() ? java.time.Duration.ofMinutes(1) : ttl;
    }

    private void invalidateStudentEntryCaches(Integer studentId, Integer examCode) {
        examCacheFacade.evictStudentExamList(studentId);
        examCacheFacade.evictStudentExamDetail(studentId, examCode);
    }
}
