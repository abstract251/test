package com.test.oes.service;

import com.test.oes.entity.Student;

import java.util.Map;

public interface StudentExamSessionService {

    Map<String, Object> startOrResumeAttempt(Integer examCode, Student student);

    void saveAnswers(Integer examCode, Student student, Map<String, String> answers);

    Map<String, Object> submitAttempt(Integer examCode, Student student);
}
