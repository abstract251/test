package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Teacher;
import com.test.oes.service.impl.TeacherServiceImpl;
import com.test.oes.util.ApiResultHandler;
import org.springframework.web.bind.annotation.*;

@RestController
public class TeacherController {

    private final TeacherServiceImpl teacherService;

    public TeacherController(TeacherServiceImpl teacherService){
        this.teacherService = teacherService;
    }

    @GetMapping("/teachers/{page}/{size}/{teacherId}/{teacherName}/{institute}/{type}/{tel}/{email}")
    public ApiResult<IPage<Teacher>> findAll(@PathVariable Integer page, @PathVariable Integer size,
                                             @PathVariable String teacherId, @PathVariable String teacherName,
                                             @PathVariable String institute, @PathVariable String type,
                                             @PathVariable String tel, @PathVariable String email){
        Page<Teacher> teacherPage = new Page<>(page,size);
        IPage<Teacher> teacherIPage = teacherService.findAll(
                teacherPage, teacherId, teacherName, institute, type, tel, email
        );

        return ApiResultHandler.buildApiResult(200,"查询所有教师",teacherIPage);
    }

    @GetMapping("/teacher/{teacherId}")
    public ApiResult<Teacher> findById(@PathVariable Integer teacherId){
        return ApiResultHandler.success(teacherService.findById(teacherId));
    }

    @DeleteMapping("/teacher/{teacherId}")
    public ApiResult<Integer> deleteById(@PathVariable Integer teacherId){
        return ApiResultHandler.success(teacherService.deleteById(teacherId));
    }

    @PutMapping("/teacher")
    public ApiResult<Integer> update(@RequestBody Teacher teacher){
        return ApiResultHandler.success(teacherService.update(teacher));
    }

    @PostMapping("/teacher")
    public ApiResult<Integer> add(@RequestBody Teacher teacher){
        return ApiResultHandler.success(teacherService.add(teacher));
    }
}
