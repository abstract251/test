package com.test.oes.cache;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String scopeExamVersion() {
        return "oes:version:scope-exams";
    }

    public static String scopeExams(String version, String grade, String major, String institute) {
        return "oes:student:scope-exams:" + version + ":" + normalized(grade) + ":" + normalized(major) + ":" + normalized(institute);
    }

    public static String examMeta(Integer examCode) {
        return "oes:exam:meta:" + examCode;
    }

    public static String paperScore(Integer paperId) {
        return "oes:paper:score:" + paperId;
    }

    public static String paperSummary(Integer paperId) {
        return "oes:paper:summary:" + paperId;
    }

    public static String snapshotView(Integer examCode) {
        return "oes:exam:snapshot:view:" + examCode;
    }

    public static String answerKey(Integer examCode) {
        return "oes:exam:answer-key:" + examCode;
    }

    public static String studentExamList(Integer studentId) {
        return "studentExamList::" + studentId;
    }

    public static String studentExamDetail(Integer studentId, Integer examCode) {
        return "studentExamDetail::" + studentId + ":" + examCode;
    }

    public static String questionBank(Integer questionType, String subject, String keyword, Integer page, Integer size) {
        return "questionBank::" + normalized(questionType) + ":" + normalized(subject) + ":" + normalized(keyword) + ":" + page + ":" + size;
    }

    public static String studentDraft(Integer examCode, Integer studentId) {
        return "oes:student:draft:" + examCode + ":" + studentId;
    }

    public static String refreshToken(String jti) {
        return "oes:auth:refresh:" + jti;
    }

    private static String normalized(Object value) {
        if (value == null) {
            return "_";
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "_" : text;
    }
}
