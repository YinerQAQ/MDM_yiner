package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.service.DataModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-models")
@RequiredArgsConstructor
public class DataModelController {

    private final DataModelService dataModelService;

    @PostMapping
    public ResponseEntity<ApiResponse<MdmDataModel>> createModel(@RequestBody MdmDataModel model) {
        MdmDataModel createdModel = dataModelService.createModel(model);
        return ResponseEntity.ok(ApiResponse.success("创建成功", createdModel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmDataModel>> getModelById(@PathVariable String id) {
        MdmDataModel model = dataModelService.getModelById(id);
        return ResponseEntity.ok(ApiResponse.success(model));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MdmDataModel>>> getAllModels() {
        List<MdmDataModel> models = dataModelService.getAllModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmDataModel>> updateModel(@PathVariable String id, @RequestBody MdmDataModel model) {
        MdmDataModel updatedModel = dataModelService.updateModel(id, model);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteModel(@PathVariable String id) {
        dataModelService.deleteModel(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

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
}