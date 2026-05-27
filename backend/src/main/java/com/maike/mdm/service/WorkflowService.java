package com.maike.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.WorkflowDesignDTO;
import com.maike.mdm.entity.MdmWorkflow;
import com.maike.mdm.entity.MdmWorkflowInstance;
import com.maike.mdm.entity.MdmWorkflowTask;

import java.util.List;

public interface WorkflowService {

    MdmWorkflow createWorkflow(MdmWorkflow workflow);

    MdmWorkflow getWorkflowById(String id);

    List<MdmWorkflow> getAllWorkflows();

    MdmWorkflow updateWorkflow(String id, MdmWorkflow workflow);

    void deleteWorkflow(String id);

    void activateWorkflow(String id);

    void deactivateWorkflow(String id);

    MdmWorkflowInstance startWorkflow(String workflowId, String dataId, String modelId, String initiatorId);

    void completeTask(String instanceId, String nodeId, String assigneeId, boolean approved, String comment);

    MdmWorkflowInstance getInstanceById(String id);

    List<MdmWorkflowInstance> getInstancesByDataId(String dataId);

    // 流程设计相关
    void saveDesign(String workflowId, WorkflowDesignDTO design);

    WorkflowDesignDTO getDesign(String workflowId);

    // 流程绑定
    void bindToModel(String workflowId, String modelId, String orgId);

    List<MdmWorkflow> getBindableWorkflows(String modelId);

    // 审核任务管理
    Page<MdmWorkflowTask> getMyTasks(String userId, String status, Page<MdmWorkflowTask> page);

    void approveTask(String taskId, String opinion);

    void rejectTask(String taskId, String opinion);

    void transferTask(String taskId, String transferToId, String transferToName);

    void claimTask(String taskId, String userId, String userName);

    void unclaimTask(String taskId);
}
