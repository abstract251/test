package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Teacher;
import com.test.oes.service.impl.TeacherServiceImpl;
import com.test.oes.util.ApiResultHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class TeacherController {

    private final TeacherServiceImpl teacherService;

    public TeacherController(TeacherServiceImpl teacherService){
        this.teacherService = teacherService;
    }

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/teacher/{teacherId}")
    public ApiResult<Teacher> findById(@PathVariable Integer teacherId){
        return ApiResultHandler.success(teacherService.findById(teacherId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/teacher/{teacherId}")
    public ApiResult<Integer> deleteById(@PathVariable Integer teacherId){
        return ApiResultHandler.success(teacherService.deleteById(teacherId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/teacher")
    public ApiResult<Integer> update(@RequestBody Teacher teacher){
        return ApiResultHandler.success(teacherService.update(teacher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teacher")
    public ApiResult<Integer> add(@RequestBody Teacher teacher){
        return ApiResultHandler.success(teacherService.add(teacher));
    }
}
