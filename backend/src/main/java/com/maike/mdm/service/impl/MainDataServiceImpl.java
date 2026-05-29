package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.ConstraintValidationResult;
import com.maike.mdm.dto.request.ArchiveApplyRequest;
import com.maike.mdm.dto.request.ArchiveReviewRequest;
import com.maike.mdm.dto.request.SubmitDataRequest;
import com.maike.mdm.dto.request.VersionCompareRequest;
import com.maike.mdm.dto.response.ConstraintWarningResponse;
import com.maike.mdm.dto.response.VersionCompareResult;
import com.maike.mdm.entity.*;
import com.maike.mdm.mapper.MdmArchiveApplyMapper;
import com.maike.mdm.mapper.MdmArchiveDataMapper;
import com.maike.mdm.mapper.MdmDataModelMapper;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.service.CodeRuleService;
import com.maike.mdm.service.ConstraintService;
import com.maike.mdm.service.MainDataService;
import com.maike.mdm.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainDataServiceImpl implements MainDataService {

    private final MdmMainDataMapper mdmMainDataMapper;
    private final MdmDataModelMapper mdmDataModelMapper;
    private final MdmArchiveApplyMapper mdmArchiveApplyMapper;
    private final MdmArchiveDataMapper mdmArchiveDataMapper;
    private final CodeRuleService codeRuleService;
    private final ConstraintService constraintService;
    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;

    // ==================== 数据保存与约束校验 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConstraintWarningResponse saveData(MdmMainData mainData) {
        // 保存前调用约束校验
        List<ConstraintValidationResult> validationResults = new ArrayList<>();
        if (mainData.getModelId() != null && mainData.getJsonData() != null) {
            try {
                Map<String, Object> dataMap = objectMapper.readValue(mainData.getJsonData(),
                        new TypeReference<Map<String, Object>>() {});
                validationResults = constraintService.validateData(mainData.getModelId(), dataMap);
            } catch (Exception e) {
                log.warn("解析jsonData进行约束校验失败: {}", e.getMessage());
            }
        }

        // 检查"错误型"校验不通过
        List<ConstraintValidationResult> errors = validationResults.stream()
                .filter(r -> !r.isPassed() && "错误".equals(r.getSeverity()))
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            String errorMessages = errors.stream()
                    .map(ConstraintValidationResult::getMessage)
                    .collect(Collectors.joining("; "));
            throw BusinessException.of("约束校验失败: " + errorMessages);
        }

        // 收集"警告型"不通过
        List<ConstraintValidationResult> warnings = validationResults.stream()
                .filter(r -> !r.isPassed() && "警告".equals(r.getSeverity()))
                .collect(Collectors.toList());

        // 保存数据，状态为"暂存"
        String id = UUID.randomUUID().toString().replace("-", "");
        mainData.setId(id);
        mainData.setDataStatus("暂存");
        mainData.setFlowStatus("DRAFT");
        mainData.setVersion(1);
        mainData.setIsModify(0);
        mainData.setIsDeleted(0);
        mainData.setCreateTime(LocalDateTime.now());
        mainData.setModifyTime(LocalDateTime.now());

        mdmMainDataMapper.insert(mainData);
        log.info("保存主数据成功: id={}, code={}", mainData.getId(), mainData.getCode());

        // 如果有警告，返回警告信息
        if (!warnings.isEmpty()) {
            return ConstraintWarningResponse.builder()
                    .warnings(warnings)
                    .dataId(mainData.getId())
                    .build();
        }
        return null;
    }

    // ==================== 审核流程 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData submitData(String id, SubmitDataRequest request) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"暂存".equals(mainData.getDataStatus()) && !"审核拒绝".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有暂存或审核拒绝状态的数据可以提交审核");
        }

        // 保存提交说明
        if (request != null && request.getSubmitDesc() != null) {
            mainData.setSubmitDesc(request.getSubmitDesc());
        }

        // 调用编码规则生成编码（如果有绑定编码规则且当前无编码）
        if ((mainData.getCode() == null || mainData.getCode().isEmpty()) && mainData.getModelId() != null) {
            MdmDataModel model = mdmDataModelMapper.selectById(mainData.getModelId());
            if (model != null && model.getCodeRuleId() != null && !model.getCodeRuleId().isEmpty()) {
                try {
                    Map<String, Object> dataContext = new HashMap<>();
                    if (mainData.getJsonData() != null) {
                        dataContext = objectMapper.readValue(mainData.getJsonData(),
                                new TypeReference<Map<String, Object>>() {});
                    }
                    dataContext.put("modelCode", model.getModelCode());
                    String generatedCode = codeRuleService.generateCode(model.getCodeRuleId(), dataContext);
                    mainData.setCode(generatedCode);
                    log.info("提交审核自动生成编码: id={}, code={}", id, generatedCode);
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("生成编码失败: {}", e.getMessage());
                }
            }
        }

        // 启动工作流实例（查找模型绑定的流程）
        if (mainData.getModelId() != null) {
            MdmDataModel model = mdmDataModelMapper.selectById(mainData.getModelId());
            if (model != null && model.getWorkflowId() != null && !model.getWorkflowId().isEmpty()) {
                try {
                    workflowService.startWorkflow(model.getWorkflowId(), id, mainData.getModelId(),
                            mainData.getCreatedById());
                    log.info("提交审核启动工作流: id={}, workflowId={}", id, model.getWorkflowId());
                } catch (Exception e) {
                    log.warn("启动工作流失败: {}", e.getMessage());
                }
            }
        }

        // 更新状态
        mainData.setDataStatus("审核中");
        mainData.setFlowStatus("PENDING");
        mainData.setSubmittedByOrgId(mainData.getCreatedByOrgId());
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("提交主数据审核: {}", id);
        return mainData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData approveData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核中".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核中的数据可以审核通过");
        }

        mainData.setDataStatus("审核通过");
        mainData.setFlowStatus("DONE");

        // 如果是变更数据(isModify=1)，更新版本号+1
        if (mainData.getIsModify() != null && mainData.getIsModify() == 1) {
            mainData.setVersion(mainData.getVersion() + 1);
        }

        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("主数据审核通过: {}", id);
        return mainData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData rejectData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核中".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核中的数据可以拒绝");
        }

        mainData.setDataStatus("审核拒绝");
        mainData.setFlowStatus("REJECTED");
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("主数据审核拒绝: {}", id);
        return mainData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData withdrawData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核中".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核中的数据可以撤销");
        }

        mainData.setDataStatus("暂存");
        mainData.setFlowStatus("DRAFT");
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("主数据撤销: {}", id);
        return mainData;
    }

    // ==================== 变更管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData initiateChange(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核通过".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核通过的数据可以发起变更");
        }

        // 验证没有在途变更（同一code下不能有"变更中"或"审核中"的数据）
        if (mainData.getCode() != null) {
            LambdaQueryWrapper<MdmMainData> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(MdmMainData::getCode, mainData.getCode())
                    .eq(MdmMainData::getIsDeleted, 0)
                    .in(MdmMainData::getDataStatus, "变更中", "审核中");
            if (mdmMainDataMapper.exists(checkWrapper)) {
                throw BusinessException.of("该数据已有在途变更，不能重复发起");
            }
        }

        // 复制当前数据创建一条新记录
        String newId = UUID.randomUUID().toString().replace("-", "");
        MdmMainData newData = MdmMainData.builder()
                .id(newId)
                .modelId(mainData.getModelId())
                .code(mainData.getCode())
                .dataStatus("变更中")
                .flowStatus("DRAFT")
                .jsonData(mainData.getJsonData())
                .version(mainData.getVersion())
                .createdById(mainData.getCreatedById())
                .createdByName(mainData.getCreatedByName())
                .createdByOrgId(mainData.getCreatedByOrgId())
                .isModify(1)
                .isDeleted(0)
                .securityLevel(mainData.getSecurityLevel())
                .createTime(LocalDateTime.now())
                .modifyTime(LocalDateTime.now())
                .build();

        mdmMainDataMapper.insert(newData);
        log.info("发起变更: 原数据id={}, 新数据id={}", id, newId);
        return newData;
    }

    // ==================== 版本管理 ====================

    @Override
    public List<MdmMainData> getVersionHistory(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (mainData.getCode() == null || mainData.getCode().isEmpty()) {
            return Collections.singletonList(mainData);
        }

        // 查询同一code的所有版本数据，按版本号倒序
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getCode, mainData.getCode())
                .eq(MdmMainData::getIsDeleted, 0)
                .orderByDesc(MdmMainData::getVersion);
        return mdmMainDataMapper.selectList(queryWrapper);
    }

    @Override
    public List<VersionCompareResult> compareVersions(VersionCompareRequest request) {
        MdmMainData data1 = mdmMainDataMapper.selectById(request.getVersionId1());
        MdmMainData data2 = mdmMainDataMapper.selectById(request.getVersionId2());

        if (data1 == null || data2 == null) {
            throw BusinessException.of("版本数据不存在");
        }

        Map<String, Object> fields1 = parseJsonData(data1.getJsonData());
        Map<String, Object> fields2 = parseJsonData(data2.getJsonData());

        // 合并所有字段名
        Set<String> allFields = new LinkedHashSet<>();
        allFields.addAll(fields1.keySet());
        allFields.addAll(fields2.keySet());

        List<VersionCompareResult> differences = new ArrayList<>();
        for (String fieldName : allFields) {
            Object value1 = fields1.get(fieldName);
            Object value2 = fields2.get(fieldName);
            String oldStr = value1 != null ? value1.toString() : null;
            String newStr = value2 != null ? value2.toString() : null;

            if (!Objects.equals(oldStr, newStr)) {
                differences.add(VersionCompareResult.builder()
                        .fieldName(fieldName)
                        .oldValue(oldStr)
                        .newValue(newStr)
                        .build());
            }
        }

        return differences;
    }

    // ==================== 归档管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MdmArchiveApply> archiveApply(ArchiveApplyRequest request) {
        if (request.getArchiveReason() == null || request.getArchiveReason().trim().isEmpty()) {
            throw BusinessException.of("归档原因不能为空");
        }

        if (request.getDataIds() == null || request.getDataIds().isEmpty()) {
            throw BusinessException.of("请选择要归档的数据");
        }

        List<MdmArchiveApply> applies = new ArrayList<>();
        for (String dataId : request.getDataIds()) {
            MdmMainData mainData = mdmMainDataMapper.selectById(dataId);
            if (mainData == null) {
                throw BusinessException.of("数据不存在: " + dataId);
            }

            if (!"审核通过".equals(mainData.getDataStatus())) {
                throw BusinessException.of("只有审核通过的数据可以申请归档: " + dataId);
            }

            // 获取模型信息
            MdmDataModel model = mdmDataModelMapper.selectById(mainData.getModelId());
            String modelCode = model != null ? model.getModelCode() : request.getModelCode();

            MdmArchiveApply apply = MdmArchiveApply.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .modelId(mainData.getModelId())
                    .modelCode(modelCode)
                    .applyUserId(mainData.getCreatedById())
                    .applyUserName(mainData.getCreatedByName())
                    .applyOrgId(mainData.getCreatedByOrgId())
                    .archiveReason(request.getArchiveReason())
                    .status("待审核")
                    .createTime(LocalDateTime.now())
                    .build();

            mdmArchiveApplyMapper.insert(apply);
            applies.add(apply);
            log.info("创建归档申请: dataId={}, applyId={}", dataId, apply.getId());
        }

        return applies;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveApprove(String applyId, ArchiveReviewRequest request) {
        MdmArchiveApply apply = mdmArchiveApplyMapper.selectById(applyId);
        if (apply == null) {
            throw BusinessException.of("归档申请不存在");
        }

        if (!"待审核".equals(apply.getStatus())) {
            throw BusinessException.of("只有待审核的归档申请可以审核");
        }

        // 更新归档申请状态
        apply.setStatus("已通过");
        apply.setReviewOpinion(request != null ? request.getOpinion() : null);
        apply.setReviewTime(LocalDateTime.now());
        mdmArchiveApplyMapper.updateById(apply);

        // 查找该申请关联的主数据（通过modelId和申请人查找待归档数据）
        LambdaQueryWrapper<MdmMainData> dataWrapper = new LambdaQueryWrapper<>();
        dataWrapper.eq(MdmMainData::getModelId, apply.getModelId())
                .eq(MdmMainData::getCreatedById, apply.getApplyUserId())
                .eq(MdmMainData::getDataStatus, "审核通过")
                .eq(MdmMainData::getIsDeleted, 0);
        List<MdmMainData> dataList = mdmMainDataMapper.selectList(dataWrapper);

        for (MdmMainData mainData : dataList) {
            // 将主数据复制到MDM_ARCHIVE_DATA
            MdmArchiveData archiveData = MdmArchiveData.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .applyId(applyId)
                    .dataId(mainData.getId())
                    .modelCode(apply.getModelCode())
                    .dataCode(mainData.getCode())
                    .dataContent(mainData.getJsonData())
                    .version(mainData.getVersion())
                    .archiveTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .build();
            mdmArchiveDataMapper.insert(archiveData);

            // 更新主数据状态为"已归档"
            mainData.setDataStatus("已归档");
            mainData.setArchiveTime(LocalDateTime.now());
            mainData.setIsDeleted(1);
            mainData.setModifyTime(LocalDateTime.now());
            mdmMainDataMapper.updateById(mainData);

            log.info("归档审核通过: dataId={}, archiveDataId={}", mainData.getId(), archiveData.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveReject(String applyId, ArchiveReviewRequest request) {
        MdmArchiveApply apply = mdmArchiveApplyMapper.selectById(applyId);
        if (apply == null) {
            throw BusinessException.of("归档申请不存在");
        }

        if (!"待审核".equals(apply.getStatus())) {
            throw BusinessException.of("只有待审核的归档申请可以审核");
        }

        apply.setStatus("已拒绝");
        apply.setReviewOpinion(request != null ? request.getOpinion() : null);
        apply.setReviewTime(LocalDateTime.now());
        mdmArchiveApplyMapper.updateById(apply);
        log.info("归档审核拒绝: applyId={}", applyId);
    }

    @Override
    public Page<MdmArchiveApply> getArchiveApplyList(String status, String modelCode, Page<MdmArchiveApply> page) {
        LambdaQueryWrapper<MdmArchiveApply> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(MdmArchiveApply::getStatus, status);
        }
        if (modelCode != null && !modelCode.isEmpty()) {
            queryWrapper.eq(MdmArchiveApply::getModelCode, modelCode);
        }
        queryWrapper.orderByDesc(MdmArchiveApply::getCreateTime);
        return mdmArchiveApplyMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<MdmArchiveData> getArchiveDataList(String modelCode, String dataCode, Page<MdmArchiveData> page) {
        LambdaQueryWrapper<MdmArchiveData> queryWrapper = new LambdaQueryWrapper<>();
        if (modelCode != null && !modelCode.isEmpty()) {
            queryWrapper.eq(MdmArchiveData::getModelCode, modelCode);
        }
        if (dataCode != null && !dataCode.isEmpty()) {
            queryWrapper.like(MdmArchiveData::getDataCode, dataCode);
        }
        queryWrapper.orderByDesc(MdmArchiveData::getArchiveTime);
        return mdmArchiveDataMapper.selectPage(page, queryWrapper);
    }

    // ==================== 基础CRUD ====================

    @Override
    public List<MdmMainData> getMainDataByModelId(String modelId) {
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, modelId)
                .eq(MdmMainData::getIsDeleted, 0)
                .orderByDesc(MdmMainData::getCreateTime);
        return mdmMainDataMapper.selectList(queryWrapper);
    }

    @Override
    public MdmMainData getMainDataById(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }
        return mainData;
    }

    @Override
    public Page<MdmMainData> getMainDataPage(String modelId, String dataStatus, String createdById,
                                               String startTime, String endTime, Page<MdmMainData> page) {
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getIsDeleted, 0);

        if (modelId != null && !modelId.isEmpty()) {
            queryWrapper.eq(MdmMainData::getModelId, modelId);
        }
        if (dataStatus != null && !dataStatus.isEmpty()) {
            queryWrapper.eq(MdmMainData::getDataStatus, dataStatus);
        }
        if (createdById != null && !createdById.isEmpty()) {
            queryWrapper.eq(MdmMainData::getCreatedById, createdById);
        }
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge(MdmMainData::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le(MdmMainData::getCreateTime, endTime);
        }
        queryWrapper.orderByDesc(MdmMainData::getCreateTime);
        return mdmMainDataMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData updateMainData(String id, MdmMainData mainData) {
        MdmMainData existingData = mdmMainDataMapper.selectById(id);
        if (existingData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"暂存".equals(existingData.getDataStatus()) && !"审核拒绝".equals(existingData.getDataStatus())
                && !"变更中".equals(existingData.getDataStatus())) {
            throw BusinessException.of("只有暂存、审核拒绝或变更中的数据可以修改");
        }

        existingData.setModifyTime(LocalDateTime.now());

        if (mainData.getCode() != null) {
            existingData.setCode(mainData.getCode());
        }
        if (mainData.getJsonData() != null) {
            existingData.setJsonData(mainData.getJsonData());
        }
        if (mainData.getSecurityLevel() != null) {
            existingData.setSecurityLevel(mainData.getSecurityLevel());
        }
        if (mainData.getSubmitDesc() != null) {
            existingData.setSubmitDesc(mainData.getSubmitDesc());
        }

        mdmMainDataMapper.updateById(existingData);
        log.info("更新主数据成功: {}", id);
        return existingData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMainData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"暂存".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有暂存状态的数据可以删除");
        }

        mainData.setIsDeleted(1);
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("删除主数据成功(逻辑删除): {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData archiveData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核通过".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核通过的数据可以归档");
        }

        mainData.setDataStatus("已归档");
        mainData.setArchiveTime(LocalDateTime.now());
        mainData.setIsDeleted(1);
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("归档主数据成功: {}", id);
        return mainData;
    }

    // ==================== 批量编辑 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchEdit(List<String> ids, Map<String, Object> fields) {
        if (ids == null || ids.isEmpty()) {
            throw BusinessException.of("请选择要编辑的数据");
        }
        if (fields == null || fields.isEmpty()) {
            throw BusinessException.of("请填写要修改的字段");
        }

        for (String id : ids) {
            MdmMainData mainData = mdmMainDataMapper.selectById(id);
            if (mainData == null) {
                log.warn("批量编辑跳过不存在的数据: {}", id);
                continue;
            }

            if (!"暂存".equals(mainData.getDataStatus()) && !"审核拒绝".equals(mainData.getDataStatus())
                    && !"变更中".equals(mainData.getDataStatus())) {
                log.warn("批量编辑跳过不可编辑状态的数据: id={}, status={}", id, mainData.getDataStatus());
                continue;
            }

            // 合并字段到jsonData
            Map<String, Object> existingData = parseJsonData(mainData.getJsonData());
            existingData.putAll(fields);

            try {
                mainData.setJsonData(objectMapper.writeValueAsString(existingData));
            } catch (Exception e) {
                throw BusinessException.of("序列化数据失败: " + e.getMessage());
            }

            // 也处理顶层字段如securityLevel
            if (fields.containsKey("securityLevel")) {
                mainData.setSecurityLevel(fields.get("securityLevel").toString());
            }

            mainData.setModifyTime(LocalDateTime.now());
            mdmMainDataMapper.updateById(mainData);
            log.info("批量编辑数据成功: id={}", id);
        }
    }

    // ==================== 私有辅助方法 ====================

    private Map<String, Object> parseJsonData(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析JSON数据失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
