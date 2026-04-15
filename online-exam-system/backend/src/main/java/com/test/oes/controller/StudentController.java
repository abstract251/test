package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Student;
import com.test.oes.service.impl.StudentServiceImpl;
import com.test.oes.util.ApiResultHandler;
import org.springframework.web.bind.annotation.*;

@RestController
public class StudentController {

    private final StudentServiceImpl studentService;

    public StudentController(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/students/{page}/{size}/{studentId}/{name}/{grade}/{tel}/{institute}/{major}/{clazz}")
    public ApiResult<IPage<Student>> findAll(@PathVariable Integer page, @PathVariable Integer size,
                                             @PathVariable String studentId, @PathVariable String name,
                                             @PathVariable String grade,
                                             @PathVariable String tel, @PathVariable String institute,
                                             @PathVariable String major, @PathVariable String clazz) {
        Page<Student> studentPage = new Page<>(page, size);
        IPage<Student> res = studentService.findAll(
                studentPage, studentId, name, grade, tel, institute, major, clazz
        );
        return ApiResultHandler.buildApiResult(200, "分页查询所有学生", res);
    }

    @GetMapping("/student/{studentId}")
    public ApiResult<Student> findById(@PathVariable Integer studentId) {
        Student res = studentService.findById(studentId);
        if (res != null) {
            return ApiResultHandler.buildApiResult(200, "请求成功", res);
        }
        return ApiResultHandler.buildApiResult(404, "查询的用户不存在", null);
    }

    @DeleteMapping("/student/{studentId}")
    public ApiResult<Integer> deleteById(@PathVariable Integer studentId) {
        return ApiResultHandler.buildApiResult(200, "删除成功", studentService.deleteById(studentId));
    }

    @PutMapping("/studentPWD")
    public ApiResult<Void> updatePwd(@RequestBody Student student) {
        int updated = studentService.updatePwd(student);
        if (updated > 0) {
            return ApiResultHandler.buildApiResult(200, "密码更新成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "密码更新失败", null);
    }

    @PutMapping("/student")
    public ApiResult<Integer> update(@RequestBody Student student) {
        int res = studentService.update(student);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200, "更新成功", res);
        }
        return ApiResultHandler.buildApiResult(400, "更新失败", res);
    }

    @PostMapping("/student")
    public ApiResult<Void> add(@RequestBody Student student) {
        int res = studentService.add(student);
        if (res == 1) {
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }
}
