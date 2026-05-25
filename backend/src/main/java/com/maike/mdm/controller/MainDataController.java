package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.service.MainDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/main-data")
@RequiredArgsConstructor
public class MainDataController {

    private final MainDataService mainDataService;

    @PostMapping
    public ResponseEntity<ApiResponse<MdmMainData>> createMainData(@RequestBody MdmMainData mainData) {
        MdmMainData createdData = mainDataService.createMainData(mainData);
        return ResponseEntity.ok(ApiResponse.success("创建成功", createdData));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmMainData>> getMainDataById(@PathVariable String id) {
        MdmMainData mainData = mainDataService.getMainDataById(id);
        return ResponseEntity.ok(ApiResponse.success(mainData));
    }

    @GetMapping("/model/{modelId}")
    public ResponseEntity<ApiResponse<List<MdmMainData>>> getMainDataByModelId(@PathVariable String modelId) {
        List<MdmMainData> dataList = mainDataService.getMainDataByModelId(modelId);
        return ResponseEntity.ok(ApiResponse.success(dataList));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmMainData>> updateMainData(@PathVariable String id, @RequestBody MdmMainData mainData) {
        MdmMainData updatedData = mainDataService.updateMainData(id, mainData);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMainData(@PathVariable String id) {
        mainDataService.deleteMainData(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitForReview(@PathVariable String id) {
        mainDataService.submitForReview(id);
        return ResponseEntity.ok(ApiResponse.success("已提交审核", null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveMainData(@PathVariable String id) {
        mainDataService.approveMainData(id);
        return ResponseEntity.ok(ApiResponse.success("审核通过", null));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectMainData(@PathVariable String id) {
        mainDataService.rejectMainData(id);
        return ResponseEntity.ok(ApiResponse.success("已拒绝", null));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveMainData(@PathVariable String id) {
        mainDataService.archiveMainData(id);
        return ResponseEntity.ok(ApiResponse.success("已归档", null));
    }

    @PostMapping("/{id}/version")
    public ResponseEntity<ApiResponse<Void>> createVersion(@PathVariable String id) {
        mainDataService.createVersion(id);
        return ResponseEntity.ok(ApiResponse.success("版本已更新", null));
    }
}