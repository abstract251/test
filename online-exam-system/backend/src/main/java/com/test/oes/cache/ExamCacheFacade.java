package com.test.oes.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.test.oes.entity.ExamManage;
import com.test.oes.vo.QuestionBankItemVO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
public class ExamCacheFacade {

    private static final String CACHE_SCOPE_EXAMS = "scope-exams";
    private static final String CACHE_EXAM_META = "exam-meta";
    private static final String CACHE_PAPER_SCORE = "paper-score";
    private static final String CACHE_PAPER_SUMMARY = "paper-summary";
    private static final String CACHE_SNAPSHOT_VIEW = "snapshot-view";
    private static final String CACHE_ANSWER_KEY = "answer-key";
    private static final String CACHE_STUDENT_EXAM_LIST = "student-exam-list";
    private static final String CACHE_STUDENT_EXAM_DETAIL = "student-exam-detail";
    private static final String CACHE_QUESTION_BANK = "question-bank";
    private static final String CACHE_SCORE_STATISTICS = "score-statistics";

    private final CacheProperties cacheProperties;
    private final LocalCacheRegistry localCacheRegistry;
    private final JsonRedisStore jsonRedisStore;
    private final CacheMetrics cacheMetrics;
    private final AtomicLong localScopeVersion = new AtomicLong(1L);

    public ExamCacheFacade(CacheProperties cacheProperties,
                           LocalCacheRegistry localCacheRegistry,
                           JsonRedisStore jsonRedisStore,
                           CacheMetrics cacheMetrics) {
        this.cacheProperties = cacheProperties;
        this.localCacheRegistry = localCacheRegistry;
        this.jsonRedisStore = jsonRedisStore;
        this.cacheMetrics = cacheMetrics;
    }

    public List<ExamManage> getScopeExams(String grade, String major, String institute, Supplier<List<ExamManage>> loader) {
        String key = CacheKeys.scopeExams(currentScopeVersion(), grade, major, institute);
        Cache<String, List<ExamManage>> localCache = localCacheRegistry.scopeExams();
        List<ExamManage> localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_SCOPE_EXAMS, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_SCOPE_EXAMS, "local", "miss");

        List<ExamManage> redisValue = jsonRedisStore.get(CACHE_SCOPE_EXAMS, key, new TypeReference<List<ExamManage>>() {
        });
        if (redisValue != null) {
            localCache.put(key, redisValue);
            return redisValue;
        }

        List<ExamManage> loaded = cacheMetrics.recordLoad(CACHE_SCOPE_EXAMS, "db", loader::get);
        localCache.put(key, loaded);
        jsonRedisStore.set(CACHE_SCOPE_EXAMS, key, loaded, cacheProperties.getScopeExams().getRedisTtl());
        return loaded;
    }

    public ExamMetaCacheValue getExamMeta(Integer examCode, Supplier<ExamMetaCacheValue> loader) {
        String version = currentVersion(CacheKeys.examMetaVersion(examCode));
        String key = CacheKeys.examMeta(version, examCode);
        Integer localKey = key.hashCode();
        ExamMetaCacheValue localValue = localCacheRegistry.examMeta().getIfPresent(localKey);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_EXAM_META, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_EXAM_META, "local", "miss");

        ExamMetaCacheValue redisValue = jsonRedisStore.get(CACHE_EXAM_META, key, ExamMetaCacheValue.class);
        if (redisValue != null) {
            localCacheRegistry.examMeta().put(localKey, redisValue);
            return redisValue;
        }

        ExamMetaCacheValue loaded = cacheMetrics.recordLoad(CACHE_EXAM_META, "db", loader::get);
        if (loaded != null) {
            localCacheRegistry.examMeta().put(localKey, loaded);
            jsonRedisStore.set(CACHE_EXAM_META, key, loaded, cacheProperties.getExamMeta().getRedisTtl());
        }
        return loaded;
    }

    public Integer getPaperScore(Integer paperId, Supplier<Integer> loader) {
        Integer localValue = localCacheRegistry.paperScore().getIfPresent(paperId);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_PAPER_SCORE, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_PAPER_SCORE, "local", "miss");

        Integer redisValue = jsonRedisStore.get(CACHE_PAPER_SCORE, CacheKeys.paperScore(paperId), Integer.class);
        if (redisValue != null) {
            localCacheRegistry.paperScore().put(paperId, redisValue);
            return redisValue;
        }

        Integer loaded = cacheMetrics.recordLoad(CACHE_PAPER_SCORE, "db", loader::get);
        localCacheRegistry.paperScore().put(paperId, loaded);
        jsonRedisStore.set(CACHE_PAPER_SCORE, CacheKeys.paperScore(paperId), loaded, cacheProperties.getPaperAggregate().getRedisTtl());
        return loaded;
    }

    public List<Integer> getPaperSummary(Integer paperId, Supplier<List<Integer>> loader) {
        List<Integer> localValue = localCacheRegistry.paperSummary().getIfPresent(paperId);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_PAPER_SUMMARY, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_PAPER_SUMMARY, "local", "miss");

        List<Integer> redisValue = jsonRedisStore.get(CACHE_PAPER_SUMMARY, CacheKeys.paperSummary(paperId), new TypeReference<List<Integer>>() {
        });
        if (redisValue != null) {
            localCacheRegistry.paperSummary().put(paperId, redisValue);
            return redisValue;
        }

        List<Integer> loaded = cacheMetrics.recordLoad(CACHE_PAPER_SUMMARY, "db", loader::get);
        localCacheRegistry.paperSummary().put(paperId, loaded);
        jsonRedisStore.set(CACHE_PAPER_SUMMARY, CacheKeys.paperSummary(paperId), loaded, cacheProperties.getPaperAggregate().getRedisTtl());
        return loaded;
    }

    public FrozenSnapshotViewCacheValue getSnapshotView(Integer examCode, Supplier<FrozenSnapshotViewCacheValue> loader, Duration redisTtl) {
        String version = currentVersion(CacheKeys.snapshotVersion(examCode));
        String key = CacheKeys.snapshotView(version, examCode);
        Integer localKey = key.hashCode();
        FrozenSnapshotViewCacheValue localValue = localCacheRegistry.snapshotView().getIfPresent(localKey);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_SNAPSHOT_VIEW, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_SNAPSHOT_VIEW, "local", "miss");

        FrozenSnapshotViewCacheValue redisValue = jsonRedisStore.get(CACHE_SNAPSHOT_VIEW, key, FrozenSnapshotViewCacheValue.class);
        if (redisValue != null) {
            localCacheRegistry.snapshotView().put(localKey, redisValue);
            return redisValue;
        }

        FrozenSnapshotViewCacheValue loaded = cacheMetrics.recordLoad(CACHE_SNAPSHOT_VIEW, "db", loader::get);
        if (loaded != null) {
            localCacheRegistry.snapshotView().put(localKey, loaded);
            jsonRedisStore.set(CACHE_SNAPSHOT_VIEW, key, loaded, redisTtl);
        }
        return loaded;
    }

    public Map<String, String> getAnswerKey(Integer examCode, Supplier<Map<String, String>> loader, Duration redisTtl) {
        String version = currentVersion(CacheKeys.snapshotVersion(examCode));
        String key = CacheKeys.answerKey(version, examCode);
        Integer localKey = key.hashCode();
        Map<String, String> localValue = localCacheRegistry.answerKey().getIfPresent(localKey);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_ANSWER_KEY, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_ANSWER_KEY, "local", "miss");

        Map<String, String> redisValue = jsonRedisStore.get(CACHE_ANSWER_KEY, key, new TypeReference<Map<String, String>>() {
        });
        if (redisValue != null) {
            localCacheRegistry.answerKey().put(localKey, redisValue);
            return redisValue;
        }

        Map<String, String> loaded = cacheMetrics.recordLoad(CACHE_ANSWER_KEY, "db", loader::get);
        if (loaded != null) {
            localCacheRegistry.answerKey().put(localKey, loaded);
            jsonRedisStore.set(CACHE_ANSWER_KEY, key, loaded, redisTtl);
        }
        return loaded;
    }

    public Map<String, Object> getScoreStatistics(Integer examCode) {
        String key = CacheKeys.scoreStatistics(currentVersion(CacheKeys.scoreStatisticsVersion(examCode)), examCode);
        Map<String, Object> localValue = localCacheRegistry.scoreStatistics().getIfPresent(key);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_SCORE_STATISTICS, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_SCORE_STATISTICS, "local", "miss");
        Map<String, Object> redisValue = jsonRedisStore.get(CACHE_SCORE_STATISTICS, key, new TypeReference<Map<String, Object>>() {
        });
        if (redisValue != null) {
            localCacheRegistry.scoreStatistics().put(key, redisValue);
            return redisValue;
        }
        return null;
    }

    public void putScoreStatistics(Integer examCode, Map<String, Object> value) {
        String key = CacheKeys.scoreStatistics(currentVersion(CacheKeys.scoreStatisticsVersion(examCode)), examCode);
        localCacheRegistry.scoreStatistics().put(key, value);
        jsonRedisStore.set(CACHE_SCORE_STATISTICS, key, value, cacheProperties.getScoreStatistics().getRedisTtl());
        cacheMetrics.recordRequest(CACHE_SCORE_STATISTICS, "local", "write");
    }

    public void evictScoreStatistics(Integer examCode) {
        localCacheRegistry.scoreStatistics().invalidateAll();
        String version = currentVersion(CacheKeys.scoreStatisticsVersion(examCode));
        jsonRedisStore.delete(CACHE_SCORE_STATISTICS, CacheKeys.scoreStatistics(version, examCode));
    }

    public Map<String, Object> getStudentExamList(Integer studentId) {
        String key = CacheKeys.studentExamList(studentId);
        Map<String, Object> value = localCacheRegistry.studentExamList().getIfPresent(key);
        cacheMetrics.recordRequest(CACHE_STUDENT_EXAM_LIST, "local", value == null ? "miss" : "hit");
        return value;
    }

    public void putStudentExamList(Integer studentId, Map<String, Object> value) {
        localCacheRegistry.studentExamList().put(CacheKeys.studentExamList(studentId), value);
        cacheMetrics.recordRequest(CACHE_STUDENT_EXAM_LIST, "local", "write");
    }

    public void evictStudentExamList(Integer studentId) {
        localCacheRegistry.studentExamList().invalidate(CacheKeys.studentExamList(studentId));
    }

    public Map<String, Object> getStudentExamDetail(Integer studentId, Integer examCode) {
        String key = CacheKeys.studentExamDetail(studentId, examCode);
        Map<String, Object> value = localCacheRegistry.studentExamDetail().getIfPresent(key);
        cacheMetrics.recordRequest(CACHE_STUDENT_EXAM_DETAIL, "local", value == null ? "miss" : "hit");
        return value;
    }

    public void putStudentExamDetail(Integer studentId, Integer examCode, Map<String, Object> value) {
        localCacheRegistry.studentExamDetail().put(CacheKeys.studentExamDetail(studentId, examCode), value);
        cacheMetrics.recordRequest(CACHE_STUDENT_EXAM_DETAIL, "local", "write");
    }

    public void evictStudentExamDetail(Integer studentId, Integer examCode) {
        localCacheRegistry.studentExamDetail().invalidate(CacheKeys.studentExamDetail(studentId, examCode));
    }

    public Page<QuestionBankItemVO> getQuestionBankPage(String key) {
        Page<QuestionBankItemVO> value = localCacheRegistry.questionBankPage().getIfPresent(key);
        cacheMetrics.recordRequest(CACHE_QUESTION_BANK, "local", value == null ? "miss" : "hit");
        return value;
    }

    public void putQuestionBankPage(String key, Page<QuestionBankItemVO> page) {
        localCacheRegistry.questionBankPage().put(key, page);
        cacheMetrics.recordRequest(CACHE_QUESTION_BANK, "local", "write");
    }

    public void evictQuestionBankPages() {
        localCacheRegistry.questionBankPage().invalidateAll();
    }

    public void evictExamMeta(Integer examCode) {
        localCacheRegistry.examMeta().invalidateAll();
        String version = currentVersion(CacheKeys.examMetaVersion(examCode));
        jsonRedisStore.delete(CACHE_EXAM_META, CacheKeys.examMeta(version, examCode));
    }

    public void evictPaperAggregates(Integer paperId) {
        localCacheRegistry.paperScore().invalidate(paperId);
        localCacheRegistry.paperSummary().invalidate(paperId);
        jsonRedisStore.delete(CACHE_PAPER_SCORE, CacheKeys.paperScore(paperId));
        jsonRedisStore.delete(CACHE_PAPER_SUMMARY, CacheKeys.paperSummary(paperId));
    }

    public void evictSnapshotCaches(Integer examCode) {
        localCacheRegistry.snapshotView().invalidateAll();
        localCacheRegistry.answerKey().invalidateAll();
        String version = currentVersion(CacheKeys.snapshotVersion(examCode));
        jsonRedisStore.delete(CACHE_SNAPSHOT_VIEW, CacheKeys.snapshotView(version, examCode));
        jsonRedisStore.delete(CACHE_ANSWER_KEY, CacheKeys.answerKey(version, examCode));
    }

    public void bumpScopeExamVersion() {
        localScopeVersion.incrementAndGet();
        jsonRedisStore.increment(CACHE_SCOPE_EXAMS, CacheKeys.scopeExamVersion());
        localCacheRegistry.scopeExams().invalidateAll();
    }

    public void bumpExamMetaVersion(Integer examCode) {
        jsonRedisStore.increment(CACHE_EXAM_META, CacheKeys.examMetaVersion(examCode));
        localCacheRegistry.examMeta().invalidateAll();
    }

    public void bumpSnapshotVersion(Integer examCode) {
        jsonRedisStore.increment(CACHE_SNAPSHOT_VIEW, CacheKeys.snapshotVersion(examCode));
        localCacheRegistry.snapshotView().invalidateAll();
        localCacheRegistry.answerKey().invalidateAll();
    }

    public void bumpScoreStatisticsVersion(Integer examCode) {
        jsonRedisStore.increment(CACHE_SCORE_STATISTICS, CacheKeys.scoreStatisticsVersion(examCode));
        localCacheRegistry.scoreStatistics().invalidateAll();
    }

    public boolean isScoreProjectionDirty(Integer examCode) {
        String dirty = jsonRedisStore.getRaw(CACHE_SCORE_STATISTICS, CacheKeys.scoreProjectionDirty(examCode));
        return dirty != null && Boolean.parseBoolean(dirty);
    }

    public void markScoreProjectionDirty(Integer examCode) {
        jsonRedisStore.set(CACHE_SCORE_STATISTICS, CacheKeys.scoreProjectionDirty(examCode), Boolean.TRUE, Duration.ofHours(1));
    }

    public void clearScoreProjectionDirty(Integer examCode) {
        jsonRedisStore.delete(CACHE_SCORE_STATISTICS, CacheKeys.scoreProjectionDirty(examCode));
    }

    private String currentScopeVersion() {
        String version = jsonRedisStore.getRaw(CACHE_SCOPE_EXAMS, CacheKeys.scopeExamVersion());
        if (version != null && !version.isBlank()) {
            return version;
        }
        return String.valueOf(localScopeVersion.get());
    }

    private String currentVersion(String key) {
        String version = jsonRedisStore.getRaw("version", key);
        return version == null || version.isBlank() ? "1" : version;
    }
}
