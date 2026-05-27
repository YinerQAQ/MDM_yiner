package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmCodeRule;
import com.maike.mdm.entity.MdmCodeScheme;
import com.maike.mdm.entity.MdmCodeSegment;
import com.maike.mdm.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/code-rules")
@RequiredArgsConstructor
public class CodeRuleController {

    private final CodeRuleService codeRuleService;

    // ==================== 编码规则 ====================

    @GetMapping
    public ResponseEntity<ApiResponse<List<MdmCodeRule>>> listRules() {
        List<MdmCodeRule> rules = codeRuleService.listRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmCodeRule>> getRuleById(@PathVariable String id) {
        MdmCodeRule rule = codeRuleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRule(@RequestBody MdmCodeRule rule) {
        codeRuleService.createRule(rule);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRule(@PathVariable String id, @RequestBody MdmCodeRule rule) {
        codeRuleService.updateRule(id, rule);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable String id) {
        codeRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    // ==================== 编码方案 ====================

    @GetMapping("/{id}/schemes")
    public ResponseEntity<ApiResponse<List<MdmCodeScheme>>> getSchemesByRuleId(@PathVariable String id) {
        List<MdmCodeScheme> schemes = codeRuleService.getSchemesByRuleId(id);
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }

    @PostMapping("/{id}/schemes")
    public ResponseEntity<ApiResponse<Void>> createScheme(@PathVariable String id, @RequestBody MdmCodeScheme scheme) {
        scheme.setRuleId(id);
        codeRuleService.createScheme(scheme);
        return ResponseEntity.ok(ApiResponse.success("创建方案成功", null));
    }

    @PutMapping("/schemes/{schemeId}")
    public ResponseEntity<ApiResponse<Void>> updateScheme(@PathVariable String schemeId, @RequestBody MdmCodeScheme scheme) {
        codeRuleService.updateScheme(schemeId, scheme);
        return ResponseEntity.ok(ApiResponse.success("更新方案成功", null));
    }

    @DeleteMapping("/schemes/{schemeId}")
    public ResponseEntity<ApiResponse<Void>> deleteScheme(@PathVariable String schemeId) {
        codeRuleService.deleteScheme(schemeId);
        return ResponseEntity.ok(ApiResponse.success("删除方案成功", null));
    }

    // ==================== 编码段 ====================

    @GetMapping("/schemes/{schemeId}/segments")
    public ResponseEntity<ApiResponse<List<MdmCodeSegment>>> getSegmentsBySchemeId(@PathVariable String schemeId) {
        List<MdmCodeSegment> segments = codeRuleService.getSegmentsBySchemeId(schemeId);
        return ResponseEntity.ok(ApiResponse.success(segments));
    }

    @PostMapping("/schemes/{schemeId}/segments")
    public ResponseEntity<ApiResponse<Void>> createSegment(@PathVariable String schemeId, @RequestBody MdmCodeSegment segment) {
        segment.setSchemeId(schemeId);
        codeRuleService.createSegment(segment);
        return ResponseEntity.ok(ApiResponse.success("创建编码段成功", null));
    }

    @PutMapping("/segments/{segmentId}")
    public ResponseEntity<ApiResponse<Void>> updateSegment(@PathVariable String segmentId, @RequestBody MdmCodeSegment segment) {
        codeRuleService.updateSegment(segmentId, segment);
        return ResponseEntity.ok(ApiResponse.success("更新编码段成功", null));
    }

    @DeleteMapping("/segments/{segmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteSegment(@PathVariable String segmentId) {
        codeRuleService.deleteSegment(segmentId);
        return ResponseEntity.ok(ApiResponse.success("删除编码段成功", null));
    }

    // ==================== 生成编码 ====================

    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<String>> generateCode(@PathVariable String id, @RequestBody Map<String, Object> dataContext) {
        String code = codeRuleService.generateCode(id, dataContext);
        return ResponseEntity.ok(ApiResponse.success("生成编码成功", code));
    }
}
