package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ExamManage;
import com.test.oes.mapper.ExamManageMapper;
import com.test.oes.service.ExamManageService;
import com.test.oes.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamManageServiceImpl implements ExamManageService {

    private final ExamManageMapper examManageMapper;

    private final PaperService paperService;

    // 设置试卷总分
    private void setMaxScore(List<ExamManage> examManageList) {
        for (ExamManage examManage : examManageList) {
            examManage.setTotalScore(paperService.getMaxScore(examManage.getPaperId()));
        }
    }

    // 不分页查询
    @Override
    public List<ExamManage> findAll() {
        Page<ExamManage> examManage = new Page<>(0,9999);
        List<ExamManage> examManageList = examManageMapper.findAll(examManage).getRecords();
        setMaxScore(examManageList);
        return examManageList;
    }

    // 分页查询
    @Override
    public IPage<ExamManage> findAll(Page<ExamManage> page) {
        IPage<ExamManage> iPage = examManageMapper.findAll(page);
        setMaxScore(iPage.getRecords());
        return iPage;
    }

    // 根据ID查询
    @Override
    public ExamManage findById(Integer examCode) {
        ExamManage examManage = examManageMapper.findById(examCode);
        if (examManage == null) {
            return null;
        }
        examManage.setTotalScore(paperService.getMaxScore(examManage.getPaperId()));
        return examManage;
    }

    // 删除考试
    @Override
    public int delete(Integer examCode) {
        // 移除题目关联
        ExamManage examManage = examManageMapper.findById(examCode);
        if(examManage == null) {
            return 0;
        }
        paperService.deleteByPaperId(examManage.getPaperId());
        return examManageMapper.delete(examCode);
    }

    // 更新
    @Override
    public int update(ExamManage exammanage) {
        return examManageMapper.update(exammanage);
    }

    // 添加
    @Override
    public int add(ExamManage exammanage) {
        return examManageMapper.add(exammanage);
    }

    // 查询最后一条记录的试卷
    @Override
    public ExamManage findOnlyPaperId() {
        return examManageMapper.findOnlyPaperId();
    }
}
