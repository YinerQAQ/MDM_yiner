package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.entity.MdmModelAttribute;
import com.maike.mdm.service.DataModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-models")
@RequiredArgsConstructor
public class DataModelController {

    private final DataModelService dataModelService;

    // ==================== 模型基本CRUD ====================

    @PreAuthorize("hasAuthority('btn:model:create')")
    @PostMapping
    public ResponseEntity<ApiResponse<MdmDataModel>> createModel(@RequestBody MdmDataModel model) {
        MdmDataModel createdModel = dataModelService.createModel(model);
        return ResponseEntity.ok(ApiResponse.success("创建成功", createdModel));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmDataModel>> getModelById(@PathVariable String id) {
        MdmDataModel model = dataModelService.getModelById(id);
        return ResponseEntity.ok(ApiResponse.success(model));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MdmDataModel>>> getAllModels() {
        List<MdmDataModel> models = dataModelService.getAllModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @PreAuthorize("hasAuthority('btn:model:edit')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmDataModel>> updateModel(@PathVariable String id, @RequestBody MdmDataModel model) {
        MdmDataModel updatedModel = dataModelService.updateModel(id, model);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedModel));
    }

    @PreAuthorize("hasAuthority('btn:model:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteModel(@PathVariable String id) {
        dataModelService.deleteModel(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    // ==================== 模型状态机 ====================

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitForReview(@PathVariable String id) {
        dataModelService.submitForReview(id);
        return ResponseEntity.ok(ApiResponse.success("已提交审核", null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveModel(@PathVariable String id) {
        dataModelService.approveModel(id);
        return ResponseEntity.ok(ApiResponse.success("审核通过", null));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectModel(@PathVariable String id) {
        dataModelService.rejectModel(id);
        return ResponseEntity.ok(ApiResponse.success("已拒绝", null));
    }

    @PreAuthorize("hasAuthority('btn:model:publish')")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Void>> publishModel(@PathVariable String id) {
        dataModelService.publishModel(id);
        return ResponseEntity.ok(ApiResponse.success("发布成功", null));
    }

    @PostMapping("/{id}/change")
    public ResponseEntity<ApiResponse<MdmDataModel>> changeModel(@PathVariable String id) {
        MdmDataModel newModel = dataModelService.changeModel(id);
        return ResponseEntity.ok(ApiResponse.success("已发起模型变更", newModel));
    }

    // ==================== 模型属性管理 ====================

    @PostMapping("/{id}/attributes")
    public ResponseEntity<ApiResponse<MdmModelAttribute>> addAttribute(
            @PathVariable String id, @RequestBody MdmModelAttribute attribute) {
        MdmModelAttribute created = dataModelService.addAttribute(id, attribute);
        return ResponseEntity.ok(ApiResponse.success("添加属性成功", created));
    }

    @PutMapping("/{id}/attributes/{attrId}")
    public ResponseEntity<ApiResponse<MdmModelAttribute>> updateAttribute(
            @PathVariable String id, @PathVariable String attrId, @RequestBody MdmModelAttribute attribute) {
        MdmModelAttribute updated = dataModelService.updateAttribute(id, attrId, attribute);
        return ResponseEntity.ok(ApiResponse.success("更新属性成功", updated));
    }

    @DeleteMapping("/{id}/attributes/{attrId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(
            @PathVariable String id, @PathVariable String attrId) {
        dataModelService.deleteAttribute(id, attrId);
        return ResponseEntity.ok(ApiResponse.success("删除属性成功", null));
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<ApiResponse<List<MdmModelAttribute>>> getAttributes(@PathVariable String id) {
        List<MdmModelAttribute> attributes = dataModelService.getAttributesByModelId(id);
        return ResponseEntity.ok(ApiResponse.success(attributes));
    }
}
