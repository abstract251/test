package com.test.oes.service;

import com.test.oes.entity.Student;

import java.util.List;
import java.util.Map;

public interface StudentExamQueryService {

    Map<String, Object> getStudentExamList(Student student);

    Map<String, Object> getStudentExamDetail(Integer examCode, Student student);

    List<Integer> summarizeQuestionTypesFromCurrentPaper(Integer paperId);
}
