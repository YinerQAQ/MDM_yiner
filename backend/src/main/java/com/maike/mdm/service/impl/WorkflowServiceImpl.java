package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmWorkflow;
import com.maike.mdm.entity.MdmWorkflowInstance;
import com.maike.mdm.mapper.MdmWorkflowInstanceMapper;
import com.maike.mdm.mapper.MdmWorkflowMapper;
import com.maike.mdm.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final MdmWorkflowMapper mdmWorkflowMapper;
    private final MdmWorkflowInstanceMapper mdmWorkflowInstanceMapper;

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
}
