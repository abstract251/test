package com.test.oes.entity;

import lombok.Data;

@Data
public class ExamSharedSnapshotItem {
    private Long id;
    private Integer examCode;
    private Integer questionType;
    private Integer questionId;
    private Integer displayOrder;
}
