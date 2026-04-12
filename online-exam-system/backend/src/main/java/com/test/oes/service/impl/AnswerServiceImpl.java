package com.test.oes.service.impl;

import com.test.oes.entity.*;
import com.test.oes.service.*;
import com.test.oes.vo.AnswerSubmitVO;
import com.test.oes.vo.AnswerSubmitVO.AnswerDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final ExamManageService examManageService;
    private final PaperService paperService;
    private final MultiQuestionService multiQuestionService;
    private final FillQuestionService fillQuestionService;
    private final JudgeQuestionService judgeQuestionService;
    private final ScoreService scoreService;

    private static final int PER_QUESTION_SCORE = 2; // 每题固定2分（与PaperServiceImpl.getMaxScore保持一致）

    @Override
    @Transactional
    public Score submitAnswer(AnswerSubmitVO vo) {
        Integer examCode = vo.getExamCode();
        Integer studentId = vo.getStudentId();

        // 1. 检查是否已经提交过该考试
        List<Score> existingScores = scoreService.findByExamCode(examCode);
        boolean alreadySubmitted = existingScores.stream()
                .anyMatch(score -> score.getStudentId().equals(studentId));
        if (alreadySubmitted) {
            throw new RuntimeException("您已参加过本次考试，不能重复提交");
        }

        // 2. 获取考试信息
        ExamManage examManage = examManageService.findById(examCode);
        if (examManage == null) {
            throw new RuntimeException("考试不存在，examCode=" + examCode);
        }
        Integer paperId = examManage.getPaperId();
        String subject = examManage.getSource(); // 课程名称

        // 3. 获取试卷中所有题目（按类型分组）
        List<MultiQuestion> multiQuestions = multiQuestionService.findByIdAndType(paperId);
        List<FillQuestion> fillQuestions = fillQuestionService.findByIdAndType(paperId);
        List<JudgeQuestion> judgeQuestions = judgeQuestionService.findByIdAndType(paperId);

        // 构建正确答案映射：key = "type_questionId", value = 正确答案字符串
        Map<String, String> correctAnswerMap = new HashMap<>();
        for (MultiQuestion q : multiQuestions) {
            correctAnswerMap.put("1_" + q.getQuestionId(), q.getRightAnswer());
        }
        for (FillQuestion q : fillQuestions) {
            correctAnswerMap.put("2_" + q.getQuestionId(), q.getAnswer());
        }
        for (JudgeQuestion q : judgeQuestions) {
            correctAnswerMap.put("3_" + q.getQuestionId(), q.getAnswer());
        }

        // 4. 判分
        int totalScore = (multiQuestions.size() + fillQuestions.size() + judgeQuestions.size()) * PER_QUESTION_SCORE;
        int obtainedScore = 0;

        for (AnswerDetail answer : vo.getAnswers()) {
            String key = answer.getQuestionType() + "_" + answer.getQuestionId();
            String correctAnswer = correctAnswerMap.get(key);
            if (correctAnswer == null) {
                continue; // 题目不存在，跳过
            }
            String userAnswer = answer.getAnswer();
            if (isAnswerCorrect(answer.getQuestionType(), userAnswer, correctAnswer)) {
                obtainedScore += PER_QUESTION_SCORE;
            }
        }

        // 5. 判断是否及格（总分60%）
        boolean isPass = (double) obtainedScore / totalScore >= 0.6;
        int ptScore = isPass ? 1 : 0;

        // 6. 保存成绩
        Score score = new Score();
        score.setExamCode(examCode);
        //score.setStudentId(studentId);
        score.setStudentId(String.valueOf(studentId));
        score.setSubject(subject);
        score.setPtScore(ptScore);
        score.setEtScore(obtainedScore);
        score.setScore(totalScore);
        score.setAnswerDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        int rows = scoreService.add(score);
        if (rows == 0) {
            throw new RuntimeException("成绩保存失败");
        }
        return score;
    }

    /**
     * 判断答案是否正确
     */
    private boolean isAnswerCorrect(Integer questionType, String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        switch (questionType) {
            case 1: // 选择题：不区分大小写，去空格
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case 2: // 填空题：忽略前后空格，不区分大小写（可调整策略）
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case 3: // 判断题：统一转为"T"/"F"比较
                String userNorm = "true".equalsIgnoreCase(userAnswer) ? "T" :
                        ("false".equalsIgnoreCase(userAnswer) ? "F" : userAnswer.trim().toUpperCase());
                String correctNorm = "true".equalsIgnoreCase(correctAnswer) ? "T" :
                        ("false".equalsIgnoreCase(correctAnswer) ? "F" : correctAnswer.trim().toUpperCase());
                return userNorm.equals(correctNorm);
            default:
                return false;
        }
    }
}