package com.maike.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.ArchiveApplyRequest;
import com.maike.mdm.dto.request.ArchiveReviewRequest;
import com.maike.mdm.dto.request.SubmitDataRequest;
import com.maike.mdm.dto.request.VersionCompareRequest;
import com.maike.mdm.dto.response.ConstraintWarningResponse;
import com.maike.mdm.dto.response.VersionCompareResult;
import com.maike.mdm.entity.MdmArchiveApply;
import com.maike.mdm.entity.MdmArchiveData;
import com.maike.mdm.entity.MdmMainData;

import java.util.List;
import java.util.Map;

public interface MainDataService {

    // ==================== 数据保存与约束校验 ====================

    /**
     * 保存数据（含约束校验）
     * 如果有"错误型"校验不通过，抛出BusinessException
     * 如果只有"警告型"不通过，返回警告信息但允许保存
     * 保存后状态为"暂存"
     */
    ConstraintWarningResponse saveData(MdmMainData mainData);

    // ==================== 审核流程 ====================

    /**
     * 提交审核
     * 验证数据状态为"暂存"或"审核拒绝"
     * 调用codeRuleService.generateCode()生成编码（如果有绑定编码规则）
     * 启动工作流实例
     * 更新状态为"审核中"，flowStatus为"PENDING"
     */
    MdmMainData submitData(String id, SubmitDataRequest request);

    /**
     * 审核通过
     * 更新状态为"审核通过"，flowStatus为"DONE"
     * 如果是变更数据(isModify=1)，更新版本号+1
     */
    MdmMainData approveData(String id);

    /**
     * 审核拒绝
     * 更新状态为"审核拒绝"，flowStatus为"REJECTED"
     */
    MdmMainData rejectData(String id);

    /**
     * 撤销
     * 验证状态为"审核中"
     * 更新状态为"暂存"，flowStatus回到"DRAFT"
     */
    MdmMainData withdrawData(String id);

    // ==================== 变更管理 ====================

    /**
     * 发起变更
     * 验证数据状态为"审核通过"（正常）
     * 验证没有在途变更
     * 复制当前数据创建一条新记录，状态为"变更中"，isModify=1
     * 返回新数据ID供编辑
     */
    MdmMainData initiateChange(String id);

    // ==================== 版本管理 ====================

    /**
     * 查询版本历史
     * 查询同一code的所有版本数据，按版本号倒序
     */
    List<MdmMainData> getVersionHistory(String id);

    /**
     * 版本对比
     * 获取两个版本的jsonData
     * 解析JSON对比字段差异
     * 返回差异列表
     */
    List<VersionCompareResult> compareVersions(VersionCompareRequest request);

    // ==================== 归档管理 ====================

    /**
     * 归档申请
     * 创建归档申请记录（MDM_ARCHIVE_APPLY表）
     * 设置归档原因（必填验证）
     * 状态为"待审核"
     */
    List<MdmArchiveApply> archiveApply(ArchiveApplyRequest request);

    /**
     * 归档审核通过
     * 更新归档申请状态为"已通过"
     * 将主数据复制到MDM_ARCHIVE_DATA（数据快照JSON）
     * 更新主数据状态为"已归档"
     * 标记主数据逻辑删除或设置archiveTime
     */
    void archiveApprove(String applyId, ArchiveReviewRequest request);

    /**
     * 归档审核拒绝
     * 更新归档申请状态为"已拒绝"
     */
    void archiveReject(String applyId, ArchiveReviewRequest request);

    /**
     * 归档申请列表
     */
    Page<MdmArchiveApply> getArchiveApplyList(String status, String modelCode, Page<MdmArchiveApply> page);

    /**
     * 归档数据查询
     */
    Page<MdmArchiveData> getArchiveDataList(String modelCode, String dataCode, Page<MdmArchiveData> page);

    // ==================== 基础CRUD ====================

    List<MdmMainData> getMainDataByModelId(String modelId);

    MdmMainData getMainDataById(String id);

    Page<MdmMainData> getMainDataPage(String modelId, String dataStatus, String createdById,
                                       String startTime, String endTime, Page<MdmMainData> page);

    MdmMainData updateMainData(String id, MdmMainData mainData);

    void deleteMainData(String id);

    /**
     * 直接归档主数据
     */
    MdmMainData archiveData(String id);
}
