package com.maike.mdm.service;

import com.maike.mdm.entity.MdmWorkflow;
import com.maike.mdm.entity.MdmWorkflowInstance;

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
}