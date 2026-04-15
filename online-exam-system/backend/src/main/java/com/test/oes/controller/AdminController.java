package com.test.oes.controller;

import com.test.oes.entity.Admin;
import com.test.oes.entity.ApiResult;
import com.test.oes.service.impl.AdminServiceImpl;
import com.test.oes.util.ApiResultHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminServiceImpl adminService;

    public AdminController(AdminServiceImpl adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admins")
    public ApiResult<List<Admin>> findAll() {
        System.out.println("查询全部");
        return ApiResultHandler.success(adminService.findAll());
    }

    @GetMapping("/admin/{adminId}")
    public ApiResult<Admin> findById(@PathVariable Integer adminId) {
        System.out.println("根据ID查询");
        return ApiResultHandler.success(adminService.findById(adminId));
    }

    @DeleteMapping("/admin/{adminId}")
    public ApiResult<Void> deleteById(@PathVariable Integer adminId) {
        adminService.deleteById(adminId);
        return ApiResultHandler.success();
    }

    @PutMapping("/admin/{adminId}")
    public ApiResult<Integer> update(@PathVariable Integer adminId, @RequestBody Admin admin) {
        admin.setAdminId(adminId);
        return ApiResultHandler.success(adminService.update(admin));
    }

    @PostMapping("/admin")
    public ApiResult<Integer> add(@RequestBody Admin admin) {
        return ApiResultHandler.success(adminService.add(admin));
    }

    @GetMapping("/admin/resetPsw/{adminId}/{oldPsw}/{newPsw}")
    public ApiResult<Object> resetPsw(@PathVariable Integer adminId,
                                      @PathVariable String newPsw,
                                      @PathVariable String oldPsw) {
        return ApiResultHandler.success(adminService.resetPsw(adminId, newPsw, oldPsw));
    }
}
