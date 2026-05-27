package com.maike.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.ArchiveApplyRequest;
import com.maike.mdm.dto.request.ArchiveReviewRequest;
import com.maike.mdm.dto.request.SubmitDataRequest;
import com.maike.mdm.dto.request.VersionCompareRequest;
import com.maike.mdm.dto.response.ConstraintWarningResponse;
import com.maike.mdm.dto.response.VersionCompareResult;
import com.maike.mdm.entity.MdmArchiveApply;
import com.maike.mdm.entity.MdmArchiveData;
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

    // ==================== 数据保存与约束校验 ====================

    /**
     * 保存数据（含约束校验）
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<ConstraintWarningResponse>> saveData(@RequestBody MdmMainData mainData) {
        ConstraintWarningResponse warning = mainDataService.saveData(mainData);
        if (warning != null) {
            return ResponseEntity.ok(ApiResponse.success("保存成功，但存在警告", warning));
        }
        return ResponseEntity.ok(ApiResponse.success("保存成功", null));
    }

    // ==================== 审核流程 ====================

    /**
     * 提交审核（含编码生成+启动流程）
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<MdmMainData>> submitData(
            @PathVariable String id, @RequestBody(required = false) SubmitDataRequest request) {
        MdmMainData result = mainDataService.submitData(id, request);
        return ResponseEntity.ok(ApiResponse.success("已提交审核", result));
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<MdmMainData>> approveData(@PathVariable String id) {
        MdmMainData result = mainDataService.approveData(id);
        return ResponseEntity.ok(ApiResponse.success("审核通过", result));
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<MdmMainData>> rejectData(@PathVariable String id) {
        MdmMainData result = mainDataService.rejectData(id);
        return ResponseEntity.ok(ApiResponse.success("已拒绝", result));
    }

    /**
     * 撤销
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<MdmMainData>> withdrawData(@PathVariable String id) {
        MdmMainData result = mainDataService.withdrawData(id);
        return ResponseEntity.ok(ApiResponse.success("已撤销", result));
    }

    // ==================== 变更管理 ====================

    /**
     * 发起变更
     */
    @PostMapping("/{id}/change")
    public ResponseEntity<ApiResponse<MdmMainData>> initiateChange(@PathVariable String id) {
        MdmMainData result = mainDataService.initiateChange(id);
        return ResponseEntity.ok(ApiResponse.success("变更已发起", result));
    }

    // ==================== 版本管理 ====================

    /**
     * 版本历史
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<MdmMainData>>> getVersionHistory(@PathVariable String id) {
        List<MdmMainData> versions = mainDataService.getVersionHistory(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    /**
     * 版本对比
     */
    @PostMapping("/versions/compare")
    public ResponseEntity<ApiResponse<List<VersionCompareResult>>> compareVersions(
            @RequestBody VersionCompareRequest request) {
        List<VersionCompareResult> differences = mainDataService.compareVersions(request);
        return ResponseEntity.ok(ApiResponse.success(differences));
    }

    // ==================== 归档管理 ====================

    /**
     * 归档申请
     */
    @PostMapping("/archive/apply")
    public ResponseEntity<ApiResponse<List<MdmArchiveApply>>> archiveApply(
            @RequestBody ArchiveApplyRequest request) {
        List<MdmArchiveApply> applies = mainDataService.archiveApply(request);
        return ResponseEntity.ok(ApiResponse.success("归档申请已提交", applies));
    }

    /**
     * 归档审核通过
     */
    @PostMapping("/archive/{applyId}/approve")
    public ResponseEntity<ApiResponse<Void>> archiveApprove(
            @PathVariable String applyId, @RequestBody ArchiveReviewRequest request) {
        mainDataService.archiveApprove(applyId, request);
        return ResponseEntity.ok(ApiResponse.success("归档审核已通过", null));
    }

    /**
     * 归档审核拒绝
     */
    @PostMapping("/archive/{applyId}/reject")
    public ResponseEntity<ApiResponse<Void>> archiveReject(
            @PathVariable String applyId, @RequestBody ArchiveReviewRequest request) {
        mainDataService.archiveReject(applyId, request);
        return ResponseEntity.ok(ApiResponse.success("归档审核已拒绝", null));
    }

    /**
     * 归档申请列表
     */
    @GetMapping("/archive/list")
    public ResponseEntity<ApiResponse<Page<MdmArchiveApply>>> getArchiveApplyList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String modelCode,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<MdmArchiveApply> page = new Page<>(current, size);
        Page<MdmArchiveApply> result = mainDataService.getArchiveApplyList(status, modelCode, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 归档数据查询
     */
    @GetMapping("/archive/data")
    public ResponseEntity<ApiResponse<Page<MdmArchiveData>>> getArchiveDataList(
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String dataCode,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<MdmArchiveData> page = new Page<>(current, size);
        Page<MdmArchiveData> result = mainDataService.getArchiveDataList(modelCode, dataCode, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 基础CRUD ====================

    /**
     * 创建主数据
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MdmMainData>> createMainData(@RequestBody MdmMainData mainData) {
        mainDataService.saveData(mainData);
        return ResponseEntity.ok(ApiResponse.success("创建成功", mainData));
    }

    /**
     * 根据模型ID查询主数据列表
     */
    @GetMapping("/model/{modelId}")
    public ResponseEntity<ApiResponse<List<MdmMainData>>> getMainDataByModelId(@PathVariable String modelId) {
        List<MdmMainData> dataList = mainDataService.getMainDataByModelId(modelId);
        return ResponseEntity.ok(ApiResponse.success(dataList));
    }

    /**
     * 查询主数据详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmMainData>> getMainDataById(@PathVariable String id) {
        MdmMainData mainData = mainDataService.getMainDataById(id);
        return ResponseEntity.ok(ApiResponse.success(mainData));
    }

    /**
     * 分页查询主数据列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<MdmMainData>>> getMainDataPage(
            @RequestParam(required = false) String modelId,
            @RequestParam(required = false) String dataStatus,
            @RequestParam(required = false) String createdById,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<MdmMainData> page = new Page<>(current, size);
        Page<MdmMainData> result = mainDataService.getMainDataPage(modelId, dataStatus, createdById,
                startTime, endTime, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 更新主数据
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmMainData>> updateMainData(
            @PathVariable String id, @RequestBody MdmMainData mainData) {
        MdmMainData updatedData = mainDataService.updateMainData(id, mainData);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedData));
    }

    /**
     * 删除主数据
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMainData(@PathVariable String id) {
        mainDataService.deleteMainData(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    /**
     * 归档主数据（直接归档，不走归档申请流程）
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<MdmMainData>> archiveMainData(@PathVariable String id) {
        MdmMainData archived = mainDataService.archiveData(id);
        return ResponseEntity.ok(ApiResponse.success("已归档", archived));
    }

    /**
     * 发起变更（创建新版本）
     */
    @PostMapping("/{id}/version")
    public ResponseEntity<ApiResponse<MdmMainData>> createVersion(@PathVariable String id) {
        MdmMainData newData = mainDataService.initiateChange(id);
        return ResponseEntity.ok(ApiResponse.success("版本已创建", newData));
    }
}
