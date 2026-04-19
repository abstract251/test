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

    public static String examMeta(String version, Integer examCode) {
        return "oes:exam:meta:" + version + ":" + examCode;
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

    public static String snapshotView(String version, Integer examCode) {
        return "oes:exam:snapshot:view:" + version + ":" + examCode;
    }

    public static String answerKey(Integer examCode) {
        return "oes:exam:answer-key:" + examCode;
    }

    public static String answerKey(String version, Integer examCode) {
        return "oes:exam:answer-key:" + version + ":" + examCode;
    }

    public static String examMetaVersion(Integer examCode) {
        return "oes:version:exam-meta:" + examCode;
    }

    public static String snapshotVersion(Integer examCode) {
        return "oes:version:snapshot:" + examCode;
    }

    public static String scoreStatisticsVersion(Integer examCode) {
        return "oes:version:score-statistics:" + examCode;
    }

    public static String scoreStatistics(Integer examCode) {
        return "oes:score:statistics:" + examCode;
    }

    public static String scoreStatistics(String version, Integer examCode) {
        return "oes:score:statistics:" + version + ":" + examCode;
    }

    public static String scoreProjectionDirty(Integer examCode) {
        return "oes:score:projection:dirty:" + examCode;
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

    public static String draftFlushDue() {
        return "oes:draft:flush:due";
    }

    public static String draftFlushLock(Integer examCode, Integer studentId) {
        return "oes:draft:flush:lock:" + examCode + ":" + studentId;
    }

    public static String refreshToken(String jti) {
        return "oes:auth:refresh:" + jti;
    }

    public static String messageFeedVersion() {
        return "oes:version:message-feed";
    }

    public static String messagePage(String version, Integer page, Integer size) {
        return "oes:message:page:" + version + ":" + page + ":" + size;
    }

    public static String messageDetail(Integer messageId) {
        return "oes:message:detail:" + messageId;
    }

    public static String messageReplies(Integer messageId) {
        return "oes:message:replies:" + messageId;
    }

    private static String normalized(Object value) {
        if (value == null) {
            return "_";
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "_" : text;
    }
}
