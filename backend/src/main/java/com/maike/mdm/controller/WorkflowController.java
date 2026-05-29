package com.maike.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.WorkflowDesignDTO;
import com.maike.mdm.entity.MdmWorkflow;
import com.maike.mdm.entity.MdmWorkflowInstance;
import com.maike.mdm.entity.MdmWorkflowTask;
import com.maike.mdm.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PreAuthorize("hasAuthority('btn:workflow:create')")
    @PostMapping
    public ResponseEntity<ApiResponse<MdmWorkflow>> createWorkflow(@RequestBody MdmWorkflow workflow) {
        MdmWorkflow createdWorkflow = workflowService.createWorkflow(workflow);
        return ResponseEntity.ok(ApiResponse.success("创建成功", createdWorkflow));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmWorkflow>> getWorkflowById(@PathVariable String id) {
        MdmWorkflow workflow = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(ApiResponse.success(workflow));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MdmWorkflow>>> getAllWorkflows() {
        List<MdmWorkflow> workflows = workflowService.getAllWorkflows();
        return ResponseEntity.ok(ApiResponse.success(workflows));
    }

    @PreAuthorize("hasAuthority('btn:workflow:edit')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmWorkflow>> updateWorkflow(@PathVariable String id, @RequestBody MdmWorkflow workflow) {
        MdmWorkflow updatedWorkflow = workflowService.updateWorkflow(id, workflow);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedWorkflow));
    }

    @PreAuthorize("hasAuthority('btn:workflow:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflow(@PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWorkflow(@PathVariable String id) {
        workflowService.activateWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("已启用", null));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWorkflow(@PathVariable String id) {
        workflowService.deactivateWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("已停用", null));
    }

    @PostMapping("/{workflowId}/instances")
    public ResponseEntity<ApiResponse<MdmWorkflowInstance>> startWorkflow(
            @PathVariable String workflowId,
            @RequestParam String dataId,
            @RequestParam String modelId,
            @RequestParam String initiatorId) {
        MdmWorkflowInstance instance = workflowService.startWorkflow(workflowId, dataId, modelId, initiatorId);
        return ResponseEntity.ok(ApiResponse.success("流程已启动", instance));
    }

    @PostMapping("/instances/{instanceId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @PathVariable String instanceId,
            @RequestParam String nodeId,
            @RequestParam String assigneeId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment) {
        workflowService.completeTask(instanceId, nodeId, assigneeId, approved, comment);
        return ResponseEntity.ok(ApiResponse.success("任务已完成", null));
    }

    @GetMapping("/instances/{id}")
    public ResponseEntity<ApiResponse<MdmWorkflowInstance>> getInstanceById(@PathVariable String id) {
        MdmWorkflowInstance instance = workflowService.getInstanceById(id);
        return ResponseEntity.ok(ApiResponse.success(instance));
    }

    @GetMapping("/instances/data/{dataId}")
    public ResponseEntity<ApiResponse<List<MdmWorkflowInstance>>> getInstancesByDataId(@PathVariable String dataId) {
        List<MdmWorkflowInstance> instances = workflowService.getInstancesByDataId(dataId);
        return ResponseEntity.ok(ApiResponse.success(instances));
    }

    // ==================== 流程设计 ====================

    @PostMapping("/{id}/design")
    public ResponseEntity<ApiResponse<Void>> saveDesign(@PathVariable String id, @RequestBody WorkflowDesignDTO design) {
        workflowService.saveDesign(id, design);
        return ResponseEntity.ok(ApiResponse.success("保存流程设计成功", null));
    }

    @GetMapping("/{id}/design")
    public ResponseEntity<ApiResponse<WorkflowDesignDTO>> getDesign(@PathVariable String id) {
        WorkflowDesignDTO design = workflowService.getDesign(id);
        return ResponseEntity.ok(ApiResponse.success(design));
    }

    // ==================== 流程绑定 ====================

    @PostMapping("/{id}/bind")
    public ResponseEntity<ApiResponse<Void>> bindToModel(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        workflowService.bindToModel(id, body.get("modelId"), body.get("orgId"));
        return ResponseEntity.ok(ApiResponse.success("绑定成功", null));
    }

    // ==================== 审核任务管理 ====================

    @GetMapping("/tasks/mine")
    public ResponseEntity<ApiResponse<Page<MdmWorkflowTask>>> getMyTasks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        if (userId == null || userId.isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                userId = auth.getName();
            } else {
                userId = "admin";
            }
        }
        Page<MdmWorkflowTask> page = new Page<>(current, size);
        Page<MdmWorkflowTask> result = workflowService.getMyTasks(userId, status, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/tasks/{taskId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, String> body) {
        String opinion = body != null ? body.get("opinion") : null;
        workflowService.approveTask(taskId, opinion);
        return ResponseEntity.ok(ApiResponse.success("审核通过", null));
    }

    @PostMapping("/tasks/{taskId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, String> body) {
        String opinion = body != null ? body.get("opinion") : null;
        workflowService.rejectTask(taskId, opinion);
        return ResponseEntity.ok(ApiResponse.success("审核拒绝", null));
    }

    @PostMapping("/tasks/{taskId}/transfer")
    public ResponseEntity<ApiResponse<Void>> transferTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        workflowService.transferTask(taskId, body.get("transferToId"), body.get("transferToName"));
        return ResponseEntity.ok(ApiResponse.success("转办成功", null));
    }

    @PostMapping("/tasks/{taskId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        workflowService.claimTask(taskId, body.get("userId"), body.get("userName"));
        return ResponseEntity.ok(ApiResponse.success("认领成功", null));
    }

    @PostMapping("/tasks/{taskId}/unclaim")
    public ResponseEntity<ApiResponse<Void>> unclaimTask(@PathVariable String taskId) {
        workflowService.unclaimTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("取消认领成功", null));
    }
}
