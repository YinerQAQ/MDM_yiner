package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.MdmWorkflow;
import com.maike.mdm.entity.MdmWorkflowInstance;
import com.maike.mdm.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MdmWorkflow>> updateWorkflow(@PathVariable String id, @RequestBody MdmWorkflow workflow) {
        MdmWorkflow updatedWorkflow = workflowService.updateWorkflow(id, workflow);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedWorkflow));
    }

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
}