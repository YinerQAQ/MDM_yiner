package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.request.WorkflowDesignDTO;
import com.maike.mdm.entity.*;
import com.maike.mdm.mapper.MdmWorkflowEdgeMapper;
import com.maike.mdm.mapper.MdmWorkflowInstanceMapper;
import com.maike.mdm.mapper.MdmWorkflowMapper;
import com.maike.mdm.mapper.MdmWorkflowNodeMapper;
import com.maike.mdm.mapper.MdmWorkflowTaskMapper;
import com.maike.mdm.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final MdmWorkflowMapper mdmWorkflowMapper;
    private final MdmWorkflowInstanceMapper mdmWorkflowInstanceMapper;
    private final MdmWorkflowNodeMapper mdmWorkflowNodeMapper;
    private final MdmWorkflowEdgeMapper mdmWorkflowEdgeMapper;
    private final MdmWorkflowTaskMapper mdmWorkflowTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmWorkflow createWorkflow(MdmWorkflow workflow) {
        LambdaQueryWrapper<MdmWorkflow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflow::getWorkflowCode, workflow.getWorkflowCode());
        if (mdmWorkflowMapper.exists(queryWrapper)) {
            throw BusinessException.of("流程编码已存在");
        }

        workflow.setStatus("草稿");
        workflow.setCreateTime(LocalDateTime.now());
        mdmWorkflowMapper.insert(workflow);
        log.info("创建工作流成功: {}", workflow.getWorkflowName());
        return workflow;
    }

    @Override
    public MdmWorkflow getWorkflowById(String id) {
        return mdmWorkflowMapper.selectById(id);
    }

    @Override
    public List<MdmWorkflow> getAllWorkflows() {
        return mdmWorkflowMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmWorkflow updateWorkflow(String id, MdmWorkflow workflow) {
        MdmWorkflow existingWorkflow = mdmWorkflowMapper.selectById(id);
        if (existingWorkflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        if (workflow.getWorkflowName() != null) {
            existingWorkflow.setWorkflowName(workflow.getWorkflowName());
        }
        if (workflow.getDefinition() != null) {
            existingWorkflow.setDefinition(workflow.getDefinition());
        }

        mdmWorkflowMapper.updateById(existingWorkflow);
        log.info("更新工作流成功: {}", id);
        return existingWorkflow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkflow(String id) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(id);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        if (!"草稿".equals(workflow.getStatus())) {
            throw BusinessException.of("只有草稿状态的工作流可以删除");
        }

        mdmWorkflowMapper.deleteById(id);
        log.info("删除工作流成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateWorkflow(String id) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(id);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        workflow.setStatus("启用");
        mdmWorkflowMapper.updateById(workflow);
        log.info("启用工作流: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateWorkflow(String id) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(id);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        workflow.setStatus("停用");
        mdmWorkflowMapper.updateById(workflow);
        log.info("停用工作流: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmWorkflowInstance startWorkflow(String workflowId, String dataId, String modelId, String initiatorId) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        if (!"启用".equals(workflow.getStatus())) {
            throw BusinessException.of("工作流未启用");
        }

        MdmWorkflowInstance instance = MdmWorkflowInstance.builder()
                .workflowId(workflowId)
                .businessId(dataId)
                .businessType("MAIN_DATA")
                .currentNode("START")
                .status("RUNNING")
                .initiatorId(initiatorId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        mdmWorkflowInstanceMapper.insert(instance);
        log.info("启动工作流实例: {}, 数据ID: {}", instance.getId(), dataId);
        return instance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String instanceId, String nodeId, String assigneeId, boolean approved, String comment) {
        MdmWorkflowInstance instance = mdmWorkflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw BusinessException.of("流程实例不存在");
        }

        if ("COMPLETED".equals(instance.getStatus())) {
            throw BusinessException.of("流程已完成");
        }

        if (approved) {
            instance.setCurrentNode("END");
            instance.setStatus("COMPLETED");
        } else {
            instance.setStatus("REJECTED");
        }
        instance.setUpdateTime(LocalDateTime.now());

        mdmWorkflowInstanceMapper.updateById(instance);
        log.info("完成任务: {}, 节点: {}, 结果: {}", instanceId, nodeId, approved ? "通过" : "拒绝");
    }

    @Override
    public MdmWorkflowInstance getInstanceById(String id) {
        return mdmWorkflowInstanceMapper.selectById(id);
    }

    @Override
    public List<MdmWorkflowInstance> getInstancesByDataId(String dataId) {
        LambdaQueryWrapper<MdmWorkflowInstance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflowInstance::getBusinessId, dataId);
        return mdmWorkflowInstanceMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDesign(String workflowId, WorkflowDesignDTO design) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        // 删除旧的节点和连线
        LambdaQueryWrapper<MdmWorkflowNode> nodeDeleteWrapper = new LambdaQueryWrapper<>();
        nodeDeleteWrapper.eq(MdmWorkflowNode::getWorkflowId, workflowId);
        mdmWorkflowNodeMapper.delete(nodeDeleteWrapper);

        LambdaQueryWrapper<MdmWorkflowEdge> edgeDeleteWrapper = new LambdaQueryWrapper<>();
        edgeDeleteWrapper.eq(MdmWorkflowEdge::getWorkflowId, workflowId);
        mdmWorkflowEdgeMapper.delete(edgeDeleteWrapper);

        // 保存新节点
        if (design.getNodes() != null) {
            for (MdmWorkflowNode node : design.getNodes()) {
                if (node.getId() == null || node.getId().isEmpty()) {
                    node.setId(UUID.randomUUID().toString().replace("-", ""));
                }
                node.setWorkflowId(workflowId);
                node.setCreateTime(LocalDateTime.now());
                mdmWorkflowNodeMapper.insert(node);
            }
        }

        // 保存新连线
        if (design.getEdges() != null) {
            for (MdmWorkflowEdge edge : design.getEdges()) {
                if (edge.getId() == null || edge.getId().isEmpty()) {
                    edge.setId(UUID.randomUUID().toString().replace("-", ""));
                }
                edge.setWorkflowId(workflowId);
                edge.setCreateTime(LocalDateTime.now());
                mdmWorkflowEdgeMapper.insert(edge);
            }
        }

        log.info("保存流程设计成功: workflowId={}, 节点数={}, 连线数={}", workflowId,
                design.getNodes() != null ? design.getNodes().size() : 0,
                design.getEdges() != null ? design.getEdges().size() : 0);
    }

    @Override
    public WorkflowDesignDTO getDesign(String workflowId) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        LambdaQueryWrapper<MdmWorkflowNode> nodeQueryWrapper = new LambdaQueryWrapper<>();
        nodeQueryWrapper.eq(MdmWorkflowNode::getWorkflowId, workflowId)
                .orderByAsc(MdmWorkflowNode::getSortOrder);
        List<MdmWorkflowNode> nodes = mdmWorkflowNodeMapper.selectList(nodeQueryWrapper);

        LambdaQueryWrapper<MdmWorkflowEdge> edgeQueryWrapper = new LambdaQueryWrapper<>();
        edgeQueryWrapper.eq(MdmWorkflowEdge::getWorkflowId, workflowId)
                .orderByAsc(MdmWorkflowEdge::getSortOrder);
        List<MdmWorkflowEdge> edges = mdmWorkflowEdgeMapper.selectList(edgeQueryWrapper);

        return WorkflowDesignDTO.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindToModel(String workflowId, String modelId, String orgId) {
        MdmWorkflow workflow = mdmWorkflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw BusinessException.of("工作流不存在");
        }

        workflow.setOrgId(orgId);
        workflow.setUpdateTime(LocalDateTime.now());
        mdmWorkflowMapper.updateById(workflow);
        log.info("绑定工作流到模型: workflowId={}, modelId={}, orgId={}", workflowId, modelId, orgId);
    }

    @Override
    public List<MdmWorkflow> getBindableWorkflows(String modelId) {
        LambdaQueryWrapper<MdmWorkflow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflow::getStatus, "启用")
                .eq(MdmWorkflow::getIsDeleted, 0);
        return mdmWorkflowMapper.selectList(queryWrapper);
    }

    @Override
    public Page<MdmWorkflowTask> getMyTasks(String userId, String status, Page<MdmWorkflowTask> page) {
        LambdaQueryWrapper<MdmWorkflowTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflowTask::getAssigneeId, userId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(MdmWorkflowTask::getTaskStatus, status);
        }
        queryWrapper.orderByDesc(MdmWorkflowTask::getCreateTime);
        return mdmWorkflowTaskMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(String taskId, String opinion) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        if (!"待审核".equals(task.getTaskStatus())) {
            throw BusinessException.of("只有待审核状态的任务可以审核通过");
        }

        task.setTaskStatus("已通过");
        task.setOpinion(opinion);
        task.setCompleteTime(LocalDateTime.now());
        mdmWorkflowTaskMapper.updateById(task);
        log.info("审核通过: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(String taskId, String opinion) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        if (!"待审核".equals(task.getTaskStatus())) {
            throw BusinessException.of("只有待审核状态的任务可以拒绝");
        }

        task.setTaskStatus("已拒绝");
        task.setOpinion(opinion);
        task.setCompleteTime(LocalDateTime.now());
        mdmWorkflowTaskMapper.updateById(task);
        log.info("审核拒绝: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(String taskId, String transferToId, String transferToName) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        if (!"待审核".equals(task.getTaskStatus())) {
            throw BusinessException.of("只有待审核状态的任务可以转办");
        }

        task.setTaskStatus("已转办");
        task.setTransferToId(transferToId);
        task.setTransferToName(transferToName);
        task.setCompleteTime(LocalDateTime.now());

        // 创建新的待审核任务给转办人
        MdmWorkflowTask newTask = MdmWorkflowTask.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .instanceId(task.getInstanceId())
                .nodeId(task.getNodeId())
                .nodeName(task.getNodeName())
                .assigneeId(transferToId)
                .assigneeName(transferToName)
                .taskStatus("待审核")
                .createTime(LocalDateTime.now())
                .build();

        mdmWorkflowTaskMapper.updateById(task);
        mdmWorkflowTaskMapper.insert(newTask);
        log.info("转办任务: taskId={}, 转办给: {}({})", taskId, transferToName, transferToId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(String taskId, String userId, String userName) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        if (!"待审核".equals(task.getTaskStatus())) {
            throw BusinessException.of("只有待审核状态的任务可以认领");
        }

        if (task.getAssigneeId() != null && !task.getAssigneeId().isEmpty()) {
            throw BusinessException.of("该任务已被认领");
        }

        task.setAssigneeId(userId);
        task.setAssigneeName(userName);
        task.setClaimTime(LocalDateTime.now());
        mdmWorkflowTaskMapper.updateById(task);
        log.info("认领任务: taskId={}, 认领人: {}({})", taskId, userName, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unclaimTask(String taskId) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        if (!"待审核".equals(task.getTaskStatus())) {
            throw BusinessException.of("只有待审核状态的任务可以取消认领");
        }

        task.setAssigneeId(null);
        task.setAssigneeName(null);
        task.setClaimTime(null);
        mdmWorkflowTaskMapper.updateById(task);
        log.info("取消认领任务: taskId={}", taskId);
    }
}
