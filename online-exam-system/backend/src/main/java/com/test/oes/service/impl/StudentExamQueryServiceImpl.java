package com.test.oes.service.impl;

import com.test.oes.entity.ExamAttempt;
import com.test.oes.entity.ExamManage;
import com.test.oes.entity.PaperManage;
import com.test.oes.entity.Score;
import com.test.oes.entity.Student;
import com.test.oes.exception.ExamBusinessException;
import com.test.oes.mapper.ExamAttemptMapper;
import com.test.oes.mapper.ExamSharedSnapshotItemMapper;
import com.test.oes.mapper.PaperMapper;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.service.ExamManageService;
import com.test.oes.service.ExamSnapshotService;
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

    private final ExamManageService examManageService;
    private final ExamAttemptMapper examAttemptMapper;
    private final ScoreMapper scoreMapper;
    private final PaperMapper paperMapper;
    private final ExamSharedSnapshotItemMapper examSharedSnapshotItemMapper;
    private final ExamSnapshotService examSnapshotService;
    private final ExamTimeHelper examTimeHelper;

    @Override
    public Map<String, Object> getStudentExamList(Student student) {
        int studentId = requireStudentId(student);
        LocalDateTime now = examTimeHelper.nowShanghai();
        Map<Integer, ExamAttempt> attempts = examAttemptMapper.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(ExamAttempt::getExamCode, row -> row, (left, right) -> left));
        Map<Integer, Score> scores = scoreMapper.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(Score::getExamCode, row -> row, (left, right) -> left));

        List<Map<String, Object>> records = examManageService.findAll().stream()
                .filter(exam -> matchesScope(exam, student))
                .map(exam -> buildExamPayload(exam, now, attempts.get(exam.getExamCode()), scores.get(exam.getExamCode())))
                .sorted(studentExamComparator())
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serverTime", now);
        body.put("records", records);
        return body;
    }

    @Override
    public Map<String, Object> getStudentExamDetail(Integer examCode, Student student) {
        LocalDateTime now = examTimeHelper.nowShanghai();
        ExamManage exam = requireExamForStudent(examCode, student);
        int studentId = requireStudentId(student);
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudent(examCode, studentId);
        Score score = scoreMapper.findByExamAndStudent(examCode, studentId);

        Map<String, Object> examPayload = buildExamPayload(exam, now, attempt, score);
        List<Integer> counts = resolveQuestionCounts(exam, now);
        examPayload.put("questionSummary", buildQuestionSummary(counts));
        examPayload.put("totalQuestionCount", counts.stream().mapToInt(Integer::intValue).sum());
        examPayload.put("summarySource", resolveSummarySource(exam, now));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serverTime", now);
        body.put("exam", examPayload);
        return body;
    }

    @Override
    public List<Integer> summarizeQuestionTypesFromCurrentPaper(Integer paperId) {
        int[] counts = new int[]{0, 0, 0};
        if (paperId == null) {
            return List.of(0, 0, 0);
        }
        for (PaperManage item : paperMapper.findById(paperId)) {
            int index = item.getQuestionType() == null ? -1 : item.getQuestionType() - 1;
            if (index >= 0 && index < counts.length) {
                counts[index]++;
            }
        }
        return List.of(counts[0], counts[1], counts[2]);
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

    private Map<String, Object> buildExamPayload(ExamManage exam, LocalDateTime now, ExamAttempt attempt, Score score) {
        Map<String, Object> body = new LinkedHashMap<>();
        String attemptStatus = resolveAttemptStatus(attempt, score);
        String examState = resolveExamState(exam, now);
        LocalDateTime freezeAt = examTimeHelper.freezeInstant(exam);
        LocalDateTime windowEndAt = examTimeHelper.examWindowEnd(exam);
        boolean revoked = examTimeHelper.isRevoked(exam);
        boolean inExamWindow = examTimeHelper.isWithinExamWindow(exam, now);

        body.put("examCode", exam.getExamCode());
        body.put("source", exam.getSource());
        body.put("description", exam.getDescription());
        body.put("type", exam.getType());
        body.put("tips", exam.getTips());
        body.put("examDate", exam.getExamDate());
        body.put("examStartAt", exam.getExamStartAt());
        body.put("totalTime", exam.getTotalTime());
        body.put("totalScore", exam.getTotalScore());
        body.put("grade", exam.getGrade());
        body.put("major", exam.getMajor());
        body.put("institute", exam.getInstitute());
        body.put("freezeAt", freezeAt);
        body.put("windowEndAt", windowEndAt);
        body.put("paperLocked", examTimeHelper.isPaperLocked(exam, now));
        body.put("revoked", revoked);
        body.put("revokeReason", exam.getRevokeReason());
        body.put("inExamWindow", inExamWindow);
        body.put("examState", examState);
        body.put("attemptStatus", attemptStatus);
        body.put("startedAt", attempt == null ? null : attempt.getStartedAt());
        body.put("submittedAt", attempt == null ? null : attempt.getSubmittedAt());
        body.put("canEnter", "ONGOING".equals(examState) && !"SUBMITTED".equals(attemptStatus));
        return body;
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
        ExamManage exam = examManageService.findById(examCode);
        if (exam == null) {
            throw new ExamBusinessException(404, "考试不存在");
        }
        if (!matchesScope(exam, student)) {
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
            int[] counts = new int[]{0, 0, 0};
            examSharedSnapshotItemMapper.findByExamCode(exam.getExamCode()).forEach(item -> {
                int index = item.getQuestionType() == null ? -1 : item.getQuestionType() - 1;
                if (index >= 0 && index < counts.length) {
                    counts[index]++;
                }
            });
            return List.of(counts[0], counts[1], counts[2]);
        }

        return summarizeQuestionTypesFromCurrentPaper(exam.getPaperId());
    }

    private String resolveSummarySource(ExamManage exam, LocalDateTime now) {
        return examTimeHelper.isPaperLocked(exam, now) && !examTimeHelper.isRevoked(exam)
                ? "FROZEN_SNAPSHOT"
                : "CURRENT_PAPER";
    }

    private List<Map<String, Object>> buildQuestionSummary(List<Integer> counts) {
        List<Map<String, Object>> summary = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int questionType = i + 1;
            int count = counts.size() > i ? counts.get(i) : 0;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questionType", questionType);
            item.put("label", questionTypeLabel(questionType));
            item.put("count", count);
            item.put("score", count * 2);
            summary.add(item);
        }
        return summary;
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
