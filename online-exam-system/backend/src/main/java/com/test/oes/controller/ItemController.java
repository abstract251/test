package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.PaperManage;
import com.test.oes.service.PaperService;
import com.test.oes.service.impl.FillQuestionServiceImpl;
import com.test.oes.service.impl.JudgeQuestionServiceImpl;
import com.test.oes.service.impl.MultiQuestionServiceImpl;
import com.test.oes.util.ApiResultHandler;
import com.test.oes.vo.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ItemController {

    private final MultiQuestionServiceImpl multiQuestionService;

    private final FillQuestionServiceImpl fillQuestionService;

    private final JudgeQuestionServiceImpl judgeQuestionService;

    private final PaperService paperService;

    @PostMapping("/item")
    public ApiResult ItemController(@RequestBody Item item) {
        // 清空试卷
        paperService.deleteByPaperId(item.getPaperId());
        // 选择题
        Integer changeNumber = item.getChangeNumber();
        // 填空题
        Integer fillNumber = item.getFillNumber();
        // 判断题
        Integer judgeNumber = item.getJudgeNumber();
        //出卷id
        Integer paperId = item.getPaperId();

        // 数据库获取数据
        List<Integer> changeNumbers = multiQuestionService.findBySubject(item.getSubject(), changeNumber);
        List<Integer> fills = fillQuestionService.findBySubject(item.getSubject(), fillNumber);
        List<Integer> judges = judgeQuestionService.findBySubject(item.getSubject(), judgeNumber);

        if(changeNumbers == null || changeNumbers.size() != changeNumber){
            return ApiResultHandler.buildApiResult(400,"科目【" + item.getSubject() + "】题库【选择题】题目数量不足【" + changeNumber + "】，组卷失败",null);
        }
        if(fills == null || fills.size() != fillNumber) {
            return ApiResultHandler.buildApiResult(400,"科目【" + item.getSubject() + "】题库【填空题】题目数量不足【" + fillNumber + "】，组卷失败",null);
        }
        if(judges == null || judges.size() != judgeNumber){
            return ApiResultHandler.buildApiResult(400,"科目【" + item.getSubject() + "】题库【判断题】题目数量不足【" + judgeNumber + "】，组卷失败",null);
        }

        // 符合组题条件，执行组题
        // 选择题
        List<PaperManage> paperItems = new ArrayList<>(changeNumbers.size() + fills.size() + judges.size());
        for (Integer number : changeNumbers) {
            paperItems.add(new PaperManage(paperId, 1, number));
        }
        for (Integer fillNum : fills) {
            paperItems.add(new PaperManage(paperId, 2, fillNum));
        }
        for (Integer judge : judges) {
            paperItems.add(new PaperManage(paperId, 3, judge));
        }
        int saved = paperService.addBatch(paperItems);
        if (saved != paperItems.size()) {
            return ApiResultHandler.buildApiResult(400,"试卷组卷保存失败",null);
        }

          return ApiResultHandler.buildApiResult(200,"试卷组卷成功",null);
    }
}
