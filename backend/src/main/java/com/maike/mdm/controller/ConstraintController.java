package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.ConstraintValidationResult;
import com.maike.mdm.entity.MdmModelConstraint;
import com.maike.mdm.service.ConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/models/{modelId}/constraints")
@RequiredArgsConstructor
public class ConstraintController {

    private final ConstraintService constraintService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MdmModelConstraint>>> getConstraints(@PathVariable String modelId) {
        List<MdmModelConstraint> constraints = constraintService.getConstraintsByModelId(modelId);
        return ResponseEntity.ok(ApiResponse.success(constraints));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createConstraint(
            @PathVariable String modelId, @RequestBody MdmModelConstraint constraint) {
        constraint.setModelId(modelId);
        constraintService.createConstraint(constraint);
        return ResponseEntity.ok(ApiResponse.success("创建约束规则成功", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateConstraint(
            @PathVariable String modelId, @PathVariable String id,
            @RequestBody MdmModelConstraint constraint) {
        constraintService.updateConstraint(id, constraint);
        return ResponseEntity.ok(ApiResponse.success("更新约束规则成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConstraint(
            @PathVariable String modelId, @PathVariable String id) {
        constraintService.deleteConstraint(id);
        return ResponseEntity.ok(ApiResponse.success("删除约束规则成功", null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<List<ConstraintValidationResult>>> validateData(
            @PathVariable String modelId, @RequestBody Map<String, Object> data) {
        List<ConstraintValidationResult> results = constraintService.validateData(modelId, data);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
