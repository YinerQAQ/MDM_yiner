package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.request.WorkflowDesignDTO;
import com.maike.mdm.entity.*;
import com.maike.mdm.mapper.*;
import com.maike.mdm.service.WorkflowService;
import com.maike.mdm.common.util.SafeSpelEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final MdmWorkflowMapper mdmWorkflowMapper;
    private final MdmWorkflowInstanceMapper mdmWorkflowInstanceMapper;
    private final MdmWorkflowNodeMapper mdmWorkflowNodeMapper;
    private final MdmWorkflowEdgeMapper mdmWorkflowEdgeMapper;
    private final MdmWorkflowTaskMapper mdmWorkflowTaskMapper;
    private final BaseUserMapper baseUserMapper;
    private final BaseRoleMapper baseRoleMapper;
    private final BaseUserRoleMapper baseUserRoleMapper;
    private final BaseOrgMapper baseOrgMapper;
    private final MdmMainDataMapper mdmMainDataMapper;
    private final ObjectMapper objectMapper;

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

    // ======================== 网关执行逻辑 ========================

    /**
     * 排他网关：评估出边条件表达式，走第一个满足的分支
     */
    public String executeExclusiveGateway(String nodeId, Map<String, Object> context) {
        List<MdmWorkflowEdge> outEdges = getOutEdgesOrdered(nodeId);
        if (outEdges.isEmpty()) {
            throw BusinessException.of("排他网关无出边: " + nodeId);
        }

        MdmWorkflowEdge defaultEdge = null;
        for (MdmWorkflowEdge edge : outEdges) {
            if ("1".equals(edge.getIsDefault())) {
                defaultEdge = edge;
                continue;
            }
            if (edge.getConditionExpr() != null && !edge.getConditionExpr().isEmpty()) {
                try {
                    boolean result = evaluateCondition(edge.getConditionExpr(), context);
                    if (result) {
                        log.info("排他网关 {} 走分支: {}", nodeId, edge.getTargetNodeId());
                        return edge.getTargetNodeId();
                    }
                } catch (Exception e) {
                    log.warn("排他网关条件评估失败: edgeId={}, expr={}", edge.getId(), edge.getConditionExpr(), e);
                }
            }
        }

        // 所有非默认边条件均为false，走默认边
        if (defaultEdge != null) {
            log.info("排他网关 {} 走默认分支: {}", nodeId, defaultEdge.getTargetNodeId());
            return defaultEdge.getTargetNodeId();
        }

        throw BusinessException.of("排他网关无满足条件的分支且无默认边: " + nodeId);
    }

    /**
     * 并行网关：激活所有出边对应的下游节点
     */
    public List<String> executeParallelGateway(String nodeId) {
        List<MdmWorkflowEdge> outEdges = getOutEdgesOrdered(nodeId);
        if (outEdges.isEmpty()) {
            throw BusinessException.of("并行网关无出边: " + nodeId);
        }

        List<String> targetNodeIds = outEdges.stream()
                .map(MdmWorkflowEdge::getTargetNodeId)
                .collect(Collectors.toList());

        log.info("并行网关 {} 激活分支: {}", nodeId, targetNodeIds);
        return targetNodeIds;
    }

    /**
     * 包容网关：激活所有满足条件的出边
     */
    public List<String> executeInclusiveGateway(String nodeId, Map<String, Object> context) {
        List<MdmWorkflowEdge> outEdges = getOutEdgesOrdered(nodeId);
        if (outEdges.isEmpty()) {
            throw BusinessException.of("包容网关无出边: " + nodeId);
        }

        List<String> activatedTargets = new ArrayList<>();
        MdmWorkflowEdge defaultEdge = null;

        for (MdmWorkflowEdge edge : outEdges) {
            if ("1".equals(edge.getIsDefault())) {
                defaultEdge = edge;
                continue;
            }
            if (edge.getConditionExpr() != null && !edge.getConditionExpr().isEmpty()) {
                try {
                    boolean result = evaluateCondition(edge.getConditionExpr(), context);
                    if (result) {
                        activatedTargets.add(edge.getTargetNodeId());
                    }
                } catch (Exception e) {
                    log.warn("包容网关条件评估失败: edgeId={}, expr={}", edge.getId(), edge.getConditionExpr(), e);
                }
            }
        }

        // 无条件满足时走默认边
        if (activatedTargets.isEmpty() && defaultEdge != null) {
            activatedTargets.add(defaultEdge.getTargetNodeId());
        }

        if (activatedTargets.isEmpty()) {
            throw BusinessException.of("包容网关无满足条件的分支且无默认边: " + nodeId);
        }

        log.info("包容网关 {} 激活分支: {}", nodeId, activatedTargets);
        return activatedTargets;
    }

    // ======================== 会签逻辑 ========================

    /**
     * 记录会签投票，当通过数/总数 >= signPercent时，判定通过
     * @return 会签结果: PASSED / REJECTED / PENDING
     */
    public String processCountersign(String taskId, boolean approved) {
        MdmWorkflowTask task = mdmWorkflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        // 更新当前任务状态
        if (approved) {
            task.setTaskStatus("已通过");
        } else {
            task.setTaskStatus("已拒绝");
        }
        task.setCompleteTime(LocalDateTime.now());
        mdmWorkflowTaskMapper.updateById(task);

        // 获取节点配置
        MdmWorkflowNode node = mdmWorkflowNodeMapper.selectById(task.getNodeId());
        if (node == null || !"SIGN".equals(node.getSignType())) {
            // 非会签节点，直接返回
            return approved ? "PASSED" : "REJECTED";
        }

        // 查询该节点该实例的所有任务
        LambdaQueryWrapper<MdmWorkflowTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflowTask::getInstanceId, task.getInstanceId())
                .eq(MdmWorkflowTask::getNodeId, task.getNodeId());
        List<MdmWorkflowTask> allTasks = mdmWorkflowTaskMapper.selectList(queryWrapper);

        int total = allTasks.size();
        long approvedCount = allTasks.stream()
                .filter(t -> "已通过".equals(t.getTaskStatus())).count();
        long rejectedCount = allTasks.stream()
                .filter(t -> "已拒绝".equals(t.getTaskStatus())).count();
        long completedCount = approvedCount + rejectedCount;

        int signPercent = node.getSignPercent() != null ? node.getSignPercent() : 100;

        if (signPercent <= 0) {
            // 任意一人通过即通过
            if (approvedCount > 0) {
                cancelOtherPendingTasks(allTasks);
                return "PASSED";
            }
        } else {
            // 需达到百分比
            int requiredCount = (int) Math.ceil(signPercent / 100.0 * total);
            if (approvedCount >= requiredCount) {
                cancelOtherPendingTasks(allTasks);
                return "PASSED";
            }
        }

        // 所有人审核完但未达阈值 → 拒绝
        if (completedCount >= total) {
            return "REJECTED";
        }

        return "PENDING";
    }

    private void cancelOtherPendingTasks(List<MdmWorkflowTask> allTasks) {
        for (MdmWorkflowTask t : allTasks) {
            if ("待审核".equals(t.getTaskStatus())) {
                t.setTaskStatus("已取消");
                t.setCompleteTime(LocalDateTime.now());
                mdmWorkflowTaskMapper.updateById(t);
            }
        }
    }

    // ======================== 自动审核 ========================

    /**
     * 评估SpEL表达式，满足则自动审核通过
     * @return true=自动审核通过, false=不满足条件
     */
    public boolean checkAutoApprove(String nodeId, Map<String, Object> dataContext) {
        MdmWorkflowNode node = mdmWorkflowNodeMapper.selectById(nodeId);
        if (node == null) {
            return false;
        }

        String expr = node.getAutoApproveExpr();
        if (expr == null || expr.trim().isEmpty()) {
            return false;
        }

        try {
            Boolean result = SafeSpelEvaluator.evaluateBoolean(expr, dataContext);
            if (Boolean.TRUE.equals(result)) {
                log.info("节点 {} 自动审核通过，表达式: {}", nodeId, expr);
                return true;
            }
        } catch (Exception e) {
            log.error("自动审核表达式执行失败: nodeId={}, expr={}", nodeId, expr, e);
        }
        return false;
    }

    // ======================== 超期处理 ========================

    /**
     * 扫描超时任务自动审核（每小时执行）
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutTasks() {
        LambdaQueryWrapper<MdmWorkflowTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflowTask::getTaskStatus, "待审核");
        List<MdmWorkflowTask> pendingTasks = mdmWorkflowTaskMapper.selectList(queryWrapper);

        for (MdmWorkflowTask task : pendingTasks) {
            MdmWorkflowNode node = mdmWorkflowNodeMapper.selectById(task.getNodeId());
            if (node == null || node.getTimeoutHours() == null || node.getTimeoutHours() <= 0) {
                continue;
            }

            long elapsedHours = ChronoUnit.HOURS.between(task.getCreateTime(), LocalDateTime.now());
            if (elapsedHours >= node.getTimeoutHours()) {
                String action = node.getTimeoutAction();
                if (action == null || action.isEmpty()) {
                    action = "APPROVE";
                }
                switch (action) {
                    case "APPROVE":
                        task.setTaskStatus("已通过");
                        task.setOpinion("系统自动通过(超期)");
                        task.setCompleteTime(LocalDateTime.now());
                        mdmWorkflowTaskMapper.updateById(task);
                        log.info("超期自动通过: taskId={}", task.getId());
                        break;
                    case "REJECT_TO_PREV":
                    case "REJECT_TO_START":
                        task.setTaskStatus("已拒绝");
                        task.setOpinion("系统自动拒绝(超期)");
                        task.setCompleteTime(LocalDateTime.now());
                        mdmWorkflowTaskMapper.updateById(task);
                        log.info("超期自动拒绝: taskId={}, action={}", task.getId(), action);
                        break;
                    default:
                        log.warn("未知的超期动作: {}", action);
                }
            }
        }
    }

    // ======================== 审核人策略 ========================

    /**
     * 根据策略返回审核人列表
     * @param nodeId 节点ID
     * @param processContext 流程上下文(initiatorId, prevAssigneeId等)
     * @return 审核人用户ID列表
     */
    public List<String> resolveAssignees(String nodeId, Map<String, Object> processContext) {
        MdmWorkflowNode node = mdmWorkflowNodeMapper.selectById(nodeId);
        if (node == null) {
            throw BusinessException.of("节点不存在: " + nodeId);
        }

        String strategy = node.getAssigneeStrategy();
        if (strategy == null || strategy.isEmpty()) {
            strategy = "SPECIFIED";
        }
        String configValue = node.getStrategyValue();

        List<String> candidateIds;
        switch (strategy) {
            case "SPECIFIED":
                // 指定人：从ASSIGNEE_VALUE或STRATEGY_VALUE中取，逗号分隔
                String specifiedIds = configValue != null ? configValue : node.getAssigneeValue();
                candidateIds = parseIds(specifiedIds);
                break;

            case "ROLE":
                // 指定角色下所有人
                candidateIds = resolveByRole(configValue);
                break;

            case "DEPT_LEADER":
                // 发起人部门负责人
                candidateIds = resolveByDeptLeader(processContext);
                break;

            case "INITIATOR_SUPERIOR":
                // 发起人上级
                candidateIds = resolveByInitiatorSuperior(processContext, configValue);
                break;

            case "PREV_HANDLER":
                // 上一节点处理人
                candidateIds = resolveByPrevHandler(processContext);
                break;

            case "FORM_FIELD":
                // 表单指定字段值
                candidateIds = resolveByFormField(processContext, configValue);
                break;

            case "SELF":
                // 发起人自己
                String initiatorId = (String) processContext.get("initiatorId");
                candidateIds = initiatorId != null ? Collections.singletonList(initiatorId) : Collections.emptyList();
                break;

            default:
                throw BusinessException.of("未知的审核人策略: " + strategy);
        }

        // 应用过滤SQL
        if (node.getFilterSql() != null && !node.getFilterSql().trim().isEmpty()) {
            candidateIds = applyFilterSql(candidateIds, node.getFilterSql(), processContext);
        }

        log.info("节点 {} 策略 {} 解析审核人: {}", nodeId, strategy, candidateIds);
        return candidateIds;
    }

    private List<String> parseIds(String idsStr) {
        if (idsStr == null || idsStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(idsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> resolveByRole(String configValue) {
        List<String> roleIds = parseIds(configValue);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BaseUserRole> urQuery = new LambdaQueryWrapper<>();
        urQuery.in(BaseUserRole::getRoleId, roleIds);
        List<BaseUserRole> userRoles = baseUserRoleMapper.selectList(urQuery);
        return userRoles.stream()
                .map(BaseUserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> resolveByDeptLeader(Map<String, Object> context) {
        String orgId = (String) context.get("initiatorOrgId");
        if (orgId == null) {
            String initiatorId = (String) context.get("initiatorId");
            if (initiatorId != null) {
                BaseUser initiator = baseUserMapper.selectById(initiatorId);
                if (initiator != null) {
                    orgId = initiator.getOrgId();
                }
            }
        }
        if (orgId == null) {
            return Collections.emptyList();
        }
        // 查找该部门下的用户（简化：取部门中第一个用户作为负责人）
        LambdaQueryWrapper<BaseUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(BaseUser::getOrgId, orgId)
                .eq(BaseUser::getStatus, "启用")
                .last("LIMIT 1");
        BaseUser manager = baseUserMapper.selectOne(userQuery);
        return manager != null ? Collections.singletonList(manager.getId()) : Collections.emptyList();
    }

    private List<String> resolveByInitiatorSuperior(Map<String, Object> context, String configValue) {
        int level = 1;
        if (configValue != null && !configValue.isEmpty()) {
            try {
                level = Integer.parseInt(configValue.trim());
            } catch (NumberFormatException ignored) {}
        }

        String orgId = (String) context.get("initiatorOrgId");
        if (orgId == null) {
            String initiatorId = (String) context.get("initiatorId");
            if (initiatorId != null) {
                BaseUser initiator = baseUserMapper.selectById(initiatorId);
                if (initiator != null) {
                    orgId = initiator.getOrgId();
                }
            }
        }
        if (orgId == null) {
            return Collections.emptyList();
        }

        // 沿组织树上溯N级
        String currentOrgId = orgId;
        for (int i = 0; i < level; i++) {
            BaseOrg org = baseOrgMapper.selectById(currentOrgId);
            if (org == null || org.getParentId() == null || org.getParentId().isEmpty()) {
                break;
            }
            currentOrgId = org.getParentId();
        }

        LambdaQueryWrapper<BaseUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(BaseUser::getOrgId, currentOrgId)
                .eq(BaseUser::getStatus, "启用")
                .last("LIMIT 1");
        BaseUser superior = baseUserMapper.selectOne(userQuery);
        return superior != null ? Collections.singletonList(superior.getId()) : Collections.emptyList();
    }

    private List<String> resolveByPrevHandler(Map<String, Object> context) {
        String prevAssigneeId = (String) context.get("prevAssigneeId");
        if (prevAssigneeId == null || prevAssigneeId.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(prevAssigneeId);
    }

    private List<String> resolveByFormField(Map<String, Object> context, String configValue) {
        if (configValue == null || configValue.isEmpty()) {
            return Collections.emptyList();
        }
        Object fieldValue = context.get(configValue.trim());
        if (fieldValue == null) {
            return Collections.emptyList();
        }
        if (fieldValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) fieldValue;
            return list;
        }
        return Collections.singletonList(fieldValue.toString());
    }

    private List<String> applyFilterSql(List<String> candidateIds, String filterSql, Map<String, Object> context) {
        // 替换变量
        String resolvedSql = filterSql;
        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                resolvedSql = resolvedSql.replace("#{" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }
        // 简化过滤：仅保留在candidateIds中且满足SQL子查询的用户
        // 此处做简单实现：仅日志记录，实际场景需要jdbcTemplate执行
        log.info("应用过滤SQL: {}", resolvedSql);
        return candidateIds;
    }

    // ======================== 辅助方法 ========================

    private List<MdmWorkflowEdge> getOutEdgesOrdered(String nodeId) {
        LambdaQueryWrapper<MdmWorkflowEdge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmWorkflowEdge::getSourceNodeId, nodeId)
                .orderByAsc(MdmWorkflowEdge::getSortOrder);
        return mdmWorkflowEdgeMapper.selectList(queryWrapper);
    }

    /**
     * 评估条件表达式：支持 #{field} > 10000 风格的简单表达式
     */
    private boolean evaluateCondition(String conditionExpr, Map<String, Object> context) {
        // 替换 #{field} 变量为实际值
        String resolvedExpr = conditionExpr;
        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String placeholder = "#{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                // 字符串值加引号
                if (entry.getValue() instanceof String) {
                    value = "'" + value + "'";
                }
                resolvedExpr = resolvedExpr.replace(placeholder, value);
            }
        }

        // 使用安全的SpEL评估
        try {
            return Boolean.TRUE.equals(SafeSpelEvaluator.evaluateBoolean(resolvedExpr, null));
        } catch (Exception e) {
            log.warn("条件表达式评估失败: expr={}, resolved={}", conditionExpr, resolvedExpr, e);
            return false;
        }
    }
}
