package com.maike.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmEsbInfoSystem;
import com.maike.mdm.entity.MdmEsbModelDist;
import com.maike.mdm.entity.MdmEsbModelDistContent;
import com.maike.mdm.entity.MdmEsbModelDistData;
import com.maike.mdm.entity.MdmEsbModelDistRecord;
import com.maike.mdm.service.EsbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/esb")
@RequiredArgsConstructor
public class EsbController {

    private final EsbService esbService;

    // ==================== 分发接口管理 ====================

    @GetMapping("/distribute/model/{modelId}")
    public ResponseEntity<ApiResponse<List<MdmEsbModelDist>>> listDistByModelId(@PathVariable String modelId) {
        List<MdmEsbModelDist> list = esbService.listDistByModelId(modelId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/distribute/{id}")
    public ResponseEntity<ApiResponse<MdmEsbModelDist>> getDistById(@PathVariable String id) {
        MdmEsbModelDist dist = esbService.getDistById(id);
        return ResponseEntity.ok(ApiResponse.success(dist));
    }

    @PostMapping("/distribute")
    public ResponseEntity<ApiResponse<Void>> createDist(@RequestBody MdmEsbModelDist dist) {
        esbService.createDist(dist);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PutMapping("/distribute/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDist(@PathVariable String id, @RequestBody MdmEsbModelDist dist) {
        dist.setId(id);
        esbService.updateDist(dist);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/distribute/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDist(@PathVariable String id) {
        esbService.deleteDist(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/distribute/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableDist(@PathVariable String id) {
        esbService.enableDist(id);
        return ResponseEntity.ok(ApiResponse.success("已启用", null));
    }

    @PutMapping("/distribute/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableDist(@PathVariable String id) {
        esbService.disableDist(id);
        return ResponseEntity.ok(ApiResponse.success("已停用", null));
    }

    // ==================== 分发内容配置 ====================

    @GetMapping("/distribute/{id}/content")
    public ResponseEntity<ApiResponse<List<MdmEsbModelDistContent>>> getDistContent(@PathVariable String id) {
        List<MdmEsbModelDistContent> contents = esbService.getDistContent(id);
        return ResponseEntity.ok(ApiResponse.success(contents));
    }

    @PutMapping("/distribute/{id}/content")
    public ResponseEntity<ApiResponse<Void>> saveDistContent(
            @PathVariable String id,
            @RequestBody List<MdmEsbModelDistContent> contents) {
        esbService.saveDistContent(id, contents);
        return ResponseEntity.ok(ApiResponse.success("保存成功", null));
    }

    // ==================== 分发执行 ====================

    @PostMapping("/distribute/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeDistribute(
            @PathVariable String id,
            @RequestBody Map<String, List<String>> body) {
        List<String> dataIds = body.get("dataIds");
        esbService.executeDistribute(id, dataIds);
        return ResponseEntity.ok(ApiResponse.success("分发执行完成", null));
    }

    // ==================== 监控 ====================

    @GetMapping("/monitor/{distId}/data")
    public ResponseEntity<ApiResponse<Page<MdmEsbModelDistData>>> getDistData(
            @PathVariable String distId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<MdmEsbModelDistData> page = new Page<>(current, size);
        Page<MdmEsbModelDistData> result = esbService.getDistData(distId, status, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/monitor/{distId}/records")
    public ResponseEntity<ApiResponse<Page<MdmEsbModelDistRecord>>> getDistRecords(
            @PathVariable String distId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<MdmEsbModelDistRecord> page = new Page<>(current, size);
        Page<MdmEsbModelDistRecord> result = esbService.getDistRecords(distId, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 信息系统 ====================

    @GetMapping("/systems")
    public ResponseEntity<ApiResponse<List<MdmEsbInfoSystem>>> listSystems() {
        List<MdmEsbInfoSystem> list = esbService.listSystems();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/systems")
    public ResponseEntity<ApiResponse<Void>> createSystem(@RequestBody MdmEsbInfoSystem system) {
        esbService.createSystem(system);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PutMapping("/systems/{id}")
    public ResponseEntity<ApiResponse<Void>> updateSystem(@PathVariable String id, @RequestBody MdmEsbInfoSystem system) {
        system.setId(id);
        esbService.updateSystem(system);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/systems/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSystem(@PathVariable String id) {
        esbService.deleteSystem(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
