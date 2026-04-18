package com.test.oes.cache;

import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FrozenSnapshotViewCacheValue {
    private List<MultiQuestion> multiQuestions;
    private List<FillQuestion> fillQuestions;
    private List<JudgeQuestion> judgeQuestions;

    public static FrozenSnapshotViewCacheValue fromPaperMap(Map<Integer, List<?>> paperMap) {
        FrozenSnapshotViewCacheValue value = new FrozenSnapshotViewCacheValue();
        value.setMultiQuestions(castList(paperMap.get(1)));
        value.setFillQuestions(castList(paperMap.get(2)));
        value.setJudgeQuestions(castList(paperMap.get(3)));
        return value;
    }

    public Map<Integer, List<?>> toPaperMap() {
        Map<Integer, List<?>> paper = new HashMap<>(3);
        paper.put(1, multiQuestions == null ? List.of() : multiQuestions);
        paper.put(2, fillQuestions == null ? List.of() : fillQuestions);
        paper.put(3, judgeQuestions == null ? List.of() : judgeQuestions);
        return paper;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(Object source) {
        return source == null ? List.of() : (List<T>) source;
    }
}
