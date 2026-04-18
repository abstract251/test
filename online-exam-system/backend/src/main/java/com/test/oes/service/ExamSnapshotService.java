package com.test.oes.service;

import java.util.List;
import java.util.Map;

/**
 * 本场共用快照：冻结时刻之后生成，全体考生一致（评审稿 §5–§6）。
 */
public interface ExamSnapshotService {

    /**
     * 若已过冻结时刻且尚无快照，则从当前 paper_manage 复制一份并写入 {@code exam_shared_snapshot*}。
     */
    void ensureSharedSnapshot(Integer examCode);

    boolean hasSharedSnapshot(Integer examCode);

    /**
     * 返回与 {@code /paper/{paperId}} 相同结构的题目分组（1/2/3），数据来源于冻结快照。
     */
    Map<Integer, List<?>> buildFrozenPaperMap(Integer examCode);

    Map<String, String> buildAnswerKeyMap(Integer examCode);

    List<Integer> summarizeFrozenQuestionTypes(Integer examCode);
}
