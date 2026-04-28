package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.runtime.RuntimeControlService;
import com.test.oes.runtime.RuntimeFeatureKey;
import com.test.oes.runtime.RuntimeFeaturePreset;
import com.test.oes.runtime.RuntimeOperator;
import com.test.oes.runtime.RuntimePresetApplyRequest;
import com.test.oes.runtime.RuntimeToggleSnapshot;
import com.test.oes.runtime.RuntimeToggleUpdateRequest;
import com.test.oes.security.CurrentUserService;
import com.test.oes.security.LoginUser;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRuntimeControlController {

    private final RuntimeControlService runtimeControlService;
    private final CurrentUserService currentUserService;

    @GetMapping("/admin/runtime/controls")
    public ApiResult<RuntimeToggleSnapshot> current() {
        return ApiResultHandler.buildApiResult(200, "查询成功", runtimeControlService.getAuthoritativeSnapshot());
    }

    @GetMapping("/admin/runtime/controls/local")
    public ApiResult<RuntimeToggleSnapshot> local() {
        return ApiResultHandler.buildApiResult(200, "查询成功", runtimeControlService.getLocalSnapshot());
    }

    @GetMapping("/admin/runtime/controls/history")
    public ApiResult<List<?>> history() {
        return ApiResultHandler.buildApiResult(200, "查询成功", runtimeControlService.recentHistory(20));
    }

    @PutMapping("/admin/runtime/controls/{featureKey}")
    public ApiResult<RuntimeToggleSnapshot> update(@PathVariable String featureKey,
                                                   @RequestBody RuntimeToggleUpdateRequest request) {
        RuntimeFeatureKey key = RuntimeFeatureKey.from(featureKey);
        RuntimeToggleSnapshot snapshot = runtimeControlService.updateFeature(
                key,
                Boolean.TRUE.equals(request.getEnabled()),
                request.getReason(),
                operator()
        );
        return ApiResultHandler.buildApiResult(200, "更新成功", snapshot);
    }

    @PostMapping("/admin/runtime/controls/presets/{presetName}/apply")
    public ApiResult<RuntimeToggleSnapshot> applyPreset(@PathVariable String presetName,
                                                        @RequestBody RuntimePresetApplyRequest request) {
        RuntimeToggleSnapshot snapshot = runtimeControlService.applyPreset(
                RuntimeFeaturePreset.from(presetName),
                request.getReason(),
                operator()
        );
        return ApiResultHandler.buildApiResult(200, "切换成功", snapshot);
    }

    @PostMapping("/admin/runtime/controls/history/{batchId}/rollback")
    public ApiResult<RuntimeToggleSnapshot> rollback(@PathVariable String batchId,
                                                     @RequestBody RuntimePresetApplyRequest request) {
        RuntimeToggleSnapshot snapshot = runtimeControlService.rollbackBatch(batchId, request.getReason(), operator());
        return ApiResultHandler.buildApiResult(200, "回滚成功", snapshot);
    }

    private RuntimeOperator operator() {
        LoginUser user = currentUserService.requireCurrentUser();
        return new RuntimeOperator(user.getUserId(), user.getAccountRole().name(), user.getDisplayName());
    }
}
