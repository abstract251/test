package com.test.oes.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.test.oes.entity.ExamManage;
import com.test.oes.entity.Message;
import com.test.oes.entity.Replay;
import com.test.oes.vo.QuestionBankItemVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LocalCacheRegistry {

    private final Cache<String, List<ExamManage>> scopeExams;
    private final Cache<Integer, ExamMetaCacheValue> examMeta;
    private final Cache<Integer, Integer> paperScore;
    private final Cache<Integer, List<Integer>> paperSummary;
    private final Cache<Integer, FrozenSnapshotViewCacheValue> snapshotView;
    private final Cache<Integer, Map<String, String>> answerKey;
    private final Cache<String, Map<String, Object>> scoreStatistics;
    private final Cache<String, Page<Message>> messagePage;
    private final Cache<Integer, Message> messageDetail;
    private final Cache<Integer, List<Replay>> messageReplies;
    private final Cache<String, Map<String, Object>> studentExamList;
    private final Cache<String, Map<String, Object>> studentExamDetail;
    private final Cache<String, Page<QuestionBankItemVO>> questionBankPage;

    public LocalCacheRegistry(CacheProperties cacheProperties) {
        this.scopeExams = newCache(cacheProperties.getScopeExams().getLocalTtl());
        this.examMeta = newCache(cacheProperties.getExamMeta().getLocalTtl());
        this.paperScore = newCache(cacheProperties.getPaperAggregate().getLocalTtl());
        this.paperSummary = newCache(cacheProperties.getPaperAggregate().getLocalTtl());
        this.snapshotView = newCache(cacheProperties.getSnapshot().getLocalTtl());
        this.answerKey = newCache(cacheProperties.getSnapshot().getLocalTtl());
        this.scoreStatistics = newCache(cacheProperties.getScoreStatistics().getLocalTtl());
        this.messagePage = newCache(cacheProperties.getMessage().getLocalTtl());
        this.messageDetail = newCache(cacheProperties.getMessage().getLocalTtl());
        this.messageReplies = newCache(cacheProperties.getMessage().getLocalTtl());
        this.studentExamList = newCache(cacheProperties.getExamList().getLocalTtl());
        this.studentExamDetail = newCache(cacheProperties.getExamDetail().getLocalTtl());
        this.questionBankPage = newCache(cacheProperties.getQuestionBank().getLocalTtl());
    }

    public Cache<String, List<ExamManage>> scopeExams() {
        return scopeExams;
    }

    public Cache<Integer, ExamMetaCacheValue> examMeta() {
        return examMeta;
    }

    public Cache<Integer, Integer> paperScore() {
        return paperScore;
    }

    public Cache<Integer, List<Integer>> paperSummary() {
        return paperSummary;
    }

    public Cache<Integer, FrozenSnapshotViewCacheValue> snapshotView() {
        return snapshotView;
    }

    public Cache<Integer, Map<String, String>> answerKey() {
        return answerKey;
    }

    public Cache<String, Map<String, Object>> scoreStatistics() {
        return scoreStatistics;
    }

    public Cache<String, Page<Message>> messagePage() {
        return messagePage;
    }

    public Cache<Integer, Message> messageDetail() {
        return messageDetail;
    }

    public Cache<Integer, List<Replay>> messageReplies() {
        return messageReplies;
    }

    public Cache<String, Map<String, Object>> studentExamList() {
        return studentExamList;
    }

    public Cache<String, Map<String, Object>> studentExamDetail() {
        return studentExamDetail;
    }

    public Cache<String, Page<QuestionBankItemVO>> questionBankPage() {
        return questionBankPage;
    }

    private static <K, V> Cache<K, V> newCache(java.time.Duration duration) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration)
                .maximumSize(2048)
                .build();
    }
}
