package com.test.oes.service.impl;

import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.cache.ExamMetaCacheValue;
import com.test.oes.cache.QuestionSummaryCacheItem;
import com.test.oes.entity.ExamAttempt;
import com.test.oes.entity.ExamManage;
import com.test.oes.entity.Score;
import com.test.oes.entity.Student;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamAttemptMapper;
import com.test.oes.mapper.ExamManageMapper;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.PaperService;
import com.test.oes.service.StudentExamQueryService;
import com.test.oes.service.exam.ExamTimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentExamQueryServiceImpl implements StudentExamQueryService {

    private static final int STATUS_IN_PROGRESS = 0;
    private static final int STATUS_SUBMITTED = 1;

    private final ExamManageMapper examManageMapper;
    private final ExamAttemptMapper examAttemptMapper;
    private final ScoreMapper scoreMapper;
    private final ExamSnapshotService examSnapshotService;
    private final PaperService paperService;
    private final ExamTimeHelper examTimeHelper;
    private final ExamCacheFacade examCacheFacade;

    @Override
    public Map<String, Object> getStudentExamList(Student student) {
        int studentId = requireStudentId(student);
        Map<String, Object> cached = examCacheFacade.getStudentExamList(studentId);
        if (cached != null) {
            return cached;
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        List<ExamManage> visibleExams = examCacheFacade.getScopeExams(
                student.getGrade(),
                student.getMajor(),
                student.getInstitute(),
                () -> examManageMapper.findVisibleForStudent(student.getGrade(), student.getMajor(), student.getInstitute()));
        Map<Integer, ExamAttempt> attempts = examAttemptMapper.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(ExamAttempt::getExamCode, row -> row, (left, right) -> left));
        Map<Integer, Score> scores = scoreMapper.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(Score::getExamCode, row -> row, (left, right) -> left));

        List<Map<String, Object>> records = visibleExams.stream()
                .map(exam -> buildExamPayload(resolveExamMeta(exam, now),
                        exam,
                        now,
                        attempts.get(exam.getExamCode()),
                        scores.get(exam.getExamCode())))
                .sorted(studentExamComparator())
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serverTime", now);
        body.put("records", records);
        examCacheFacade.putStudentExamList(studentId, body);
        return body;
    }

    @Override
    public Map<String, Object> getStudentExamDetail(Integer examCode, Student student) {
        int studentId = requireStudentId(student);
        Map<String, Object> cached = examCacheFacade.getStudentExamDetail(studentId, examCode);
        if (cached != null) {
            return cached;
        }
        LocalDateTime now = examTimeHelper.nowShanghai();
        ExamManage exam = requireExamForStudent(examCode, student);
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudent(examCode, studentId);
        Score score = scoreMapper.findByExamAndStudent(examCode, studentId);
        ExamMetaCacheValue examMeta = resolveExamMeta(exam, now);
        Map<String, Object> examPayload = buildExamPayload(examMeta, exam, now, attempt, score);
        examPayload.put("questionSummary", buildQuestionSummaryPayload(examMeta));
        examPayload.put("totalQuestionCount", examMeta.getTotalQuestionCount());
        examPayload.put("summarySource", examMeta.getSummarySource());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serverTime", now);
        body.put("exam", examPayload);
        examCacheFacade.putStudentExamDetail(studentId, examCode, body);
        return body;
    }

    @Override
    public List<Integer> summarizeQuestionTypesFromCurrentPaper(Integer paperId) {
        return paperService.summarizeQuestionTypes(paperId);
    }

    private Comparator<Map<String, Object>> studentExamComparator() {
        return Comparator
                .comparingInt((Map<String, Object> row) -> stateOrder(String.valueOf(row.get("examState"))))
                .thenComparing((left, right) -> {
                    LocalDateTime l = (LocalDateTime) left.get("examStartAt");
                    LocalDateTime r = (LocalDateTime) right.get("examStartAt");
                    if (l == null && r == null) {
                        return 0;
                    }
                    if (l == null) {
                        return 1;
                    }
                    if (r == null) {
                        return -1;
                    }
                    return l.compareTo(r);
                });
    }

    private int stateOrder(String state) {
        return switch (String.valueOf(state).toUpperCase(Locale.ROOT)) {
            case "ONGOING" -> 0;
            case "UPCOMING" -> 1;
            case "ENDED" -> 2;
            case "REVOKED" -> 3;
            default -> 9;
        };
    }

    private Map<String, Object> buildExamPayload(ExamMetaCacheValue examMeta,
                                                 ExamManage exam,
                                                 LocalDateTime now,
                                                 ExamAttempt attempt,
                                                 Score score) {
        Map<String, Object> body = new LinkedHashMap<>();
        String attemptStatus = resolveAttemptStatus(attempt, score);
        String examState = resolveExamState(exam, now);
        boolean revoked = Boolean.TRUE.equals(examMeta.getRevoked());
        boolean inExamWindow = examTimeHelper.isWithinExamWindow(exam, now);

        body.put("examCode", examMeta.getExamCode());
        body.put("source", examMeta.getSource());
        body.put("description", examMeta.getDescription());
        body.put("type", examMeta.getType());
        body.put("tips", examMeta.getTips());
        body.put("examDate", examMeta.getExamDate());
        body.put("examStartAt", examMeta.getExamStartAt());
        body.put("totalTime", examMeta.getTotalTime());
        body.put("totalScore", examMeta.getTotalScore());
        body.put("grade", examMeta.getGrade());
        body.put("major", examMeta.getMajor());
        body.put("institute", examMeta.getInstitute());
        body.put("freezeAt", examMeta.getFreezeAt());
        body.put("windowEndAt", examMeta.getWindowEndAt());
        body.put("paperLocked", examTimeHelper.isPaperLocked(exam, now));
        body.put("revoked", revoked);
        body.put("revokeReason", examMeta.getRevokeReason());
        body.put("inExamWindow", inExamWindow);
        body.put("examState", examState);
        body.put("attemptStatus", attemptStatus);
        body.put("startedAt", attempt == null ? null : attempt.getStartedAt());
        body.put("submittedAt", attempt == null ? null : attempt.getSubmittedAt());
        body.put("canEnter", "ONGOING".equals(examState) && !"SUBMITTED".equals(attemptStatus));
        return body;
    }

    private ExamMetaCacheValue resolveExamMeta(ExamManage exam, LocalDateTime now) {
        return examCacheFacade.getExamMeta(exam.getExamCode(), () -> buildExamMeta(exam, now));
    }

    private ExamMetaCacheValue buildExamMeta(ExamManage exam, LocalDateTime now) {
        ExamMetaCacheValue meta = new ExamMetaCacheValue();
        meta.setExamCode(exam.getExamCode());
        meta.setSource(exam.getSource());
        meta.setDescription(exam.getDescription());
        meta.setType(exam.getType());
        meta.setTips(exam.getTips());
        meta.setExamDate(exam.getExamDate());
        meta.setExamStartAt(exam.getExamStartAt());
        meta.setTotalTime(exam.getTotalTime());
        meta.setTotalScore(exam.getTotalScore() != null || exam.getPaperId() == null
                ? exam.getTotalScore()
                : paperService.getMaxScore(exam.getPaperId()));
        meta.setGrade(exam.getGrade());
        meta.setMajor(exam.getMajor());
        meta.setInstitute(exam.getInstitute());
        meta.setFreezeAt(examTimeHelper.freezeInstant(exam));
        meta.setWindowEndAt(examTimeHelper.examWindowEnd(exam));
        meta.setPaperLocked(examTimeHelper.isPaperLocked(exam, now));
        meta.setRevoked(examTimeHelper.isRevoked(exam));
        meta.setRevokeReason(exam.getRevokeReason());
        List<Integer> counts = resolveQuestionCounts(exam, now);
        meta.setQuestionSummary(buildQuestionSummary(counts));
        meta.setTotalQuestionCount(counts.stream().mapToInt(Integer::intValue).sum());
        meta.setSummarySource(resolveSummarySource(exam, now));
        return meta;
    }

    private String resolveAttemptStatus(ExamAttempt attempt, Score score) {
        if (score != null) {
            return "SUBMITTED";
        }
        if (attempt == null) {
            return "NOT_STARTED";
        }
        if (Objects.equals(attempt.getStatus(), STATUS_SUBMITTED)) {
            return "SUBMITTED";
        }
        if (Objects.equals(attempt.getStatus(), STATUS_IN_PROGRESS)) {
            return "IN_PROGRESS";
        }
        return "NOT_STARTED";
    }

    private String resolveExamState(ExamManage exam, LocalDateTime now) {
        if (examTimeHelper.isRevoked(exam)) {
            return "REVOKED";
        }
        LocalDateTime start = examTimeHelper.effectiveExamStart(exam);
        if (start == null) {
            return "UPCOMING";
        }
        LocalDateTime end = examTimeHelper.examWindowEnd(exam);
        if (now.isBefore(start)) {
            return "UPCOMING";
        }
        if (end != null && now.isAfter(end)) {
            return "ENDED";
        }
        return "ONGOING";
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
        return exam;
    }

    private List<Integer> resolveQuestionCounts(ExamManage exam, LocalDateTime now) {
        if (exam.getExamCode() == null) {
            return List.of(0, 0, 0);
        }
        if (examTimeHelper.isPaperLocked(exam, now) && !examTimeHelper.isRevoked(exam)) {
            examSnapshotService.ensureSharedSnapshot(exam.getExamCode());
            return examSnapshotService.summarizeFrozenQuestionTypes(exam.getExamCode());
        }

        return summarizeQuestionTypesFromCurrentPaper(exam.getPaperId());
    }

    private String resolveSummarySource(ExamManage exam, LocalDateTime now) {
        return examTimeHelper.isPaperLocked(exam, now) && !examTimeHelper.isRevoked(exam)
                ? "FROZEN_SNAPSHOT"
                : "CURRENT_PAPER";
    }

    private List<QuestionSummaryCacheItem> buildQuestionSummary(List<Integer> counts) {
        List<QuestionSummaryCacheItem> summary = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int questionType = i + 1;
            int count = counts.size() > i ? counts.get(i) : 0;
            QuestionSummaryCacheItem item = new QuestionSummaryCacheItem();
            item.setQuestionType(questionType);
            item.setLabel(questionTypeLabel(questionType));
            item.setCount(count);
            item.setScore(count * 2);
            summary.add(item);
        }
        return summary;
    }

    private List<Map<String, Object>> buildQuestionSummaryPayload(ExamMetaCacheValue examMeta) {
        if (examMeta.getQuestionSummary() == null || examMeta.getQuestionSummary().isEmpty()) {
            return List.of();
        }
        return examMeta.getQuestionSummary().stream().map(item -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("questionType", item.getQuestionType());
            payload.put("label", item.getLabel());
            payload.put("count", item.getCount());
            payload.put("score", item.getScore());
            return payload;
        }).toList();
    }

    private String questionTypeLabel(int questionType) {
        return switch (questionType) {
            case 1 -> "选择题";
            case 2 -> "填空题";
            case 3 -> "判断题";
            default -> "未知题型";
        };
    }

    private int requireStudentId(Student student) {
        if (student == null || student.getStudentId() <= 0) {
            throw new ExamBusinessException(401, "未找到学生账号");
        }
        return student.getStudentId();
    }

    public static boolean matchesScope(ExamManage exam, Student s) {
        if (exam.getGrade() != null && !exam.getGrade().isBlank()) {
            if (s.getGrade() == null || !exam.getGrade().trim().equals(s.getGrade().trim())) {
                return false;
            }
        }
        if (exam.getMajor() != null && !exam.getMajor().isBlank()) {
            if (s.getMajor() == null || !exam.getMajor().trim().equals(s.getMajor().trim())) {
                return false;
            }
        }
        if (exam.getInstitute() != null && !exam.getInstitute().isBlank()) {
            if (s.getInstitute() == null || !exam.getInstitute().trim().equals(s.getInstitute().trim())) {
                return false;
            }
        }
        return true;
    }
}
