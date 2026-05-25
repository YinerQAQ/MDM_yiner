package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.service.MainDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainDataServiceImpl implements MainDataService {

    private final MdmMainDataMapper mdmMainDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData createMainData(MdmMainData mainData) {
        mainData.setDataStatus("暂存");
        mainData.setFlowStatus("DRAFT");
        mainData.setVersion(1);
        mainData.setIsModify(0);
        mainData.setCreateTime(LocalDateTime.now());
        mainData.setModifyTime(LocalDateTime.now());

        mdmMainDataMapper.insert(mainData);
        log.info("创建主数据成功: {}", mainData.getCode());
        return mainData;
    }

    @Override
    public MdmMainData getMainDataById(String id) {
        return mdmMainDataMapper.selectById(id);
    }

    @Override
    public List<MdmMainData> getMainDataByModelId(String modelId) {
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, modelId);
        return mdmMainDataMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmMainData updateMainData(String id, MdmMainData mainData) {
        MdmMainData existingData = mdmMainDataMapper.selectById(id);
        if (existingData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"暂存".equals(existingData.getDataStatus())) {
            throw BusinessException.of("只有暂存状态的数据可以修改");
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

        mdmMainDataMapper.deleteById(id);
        log.info("删除主数据成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"暂存".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有暂存状态的数据可以提交审核");
        }

        mainData.setDataStatus("审核中");
        mainData.setFlowStatus("PENDING");
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("提交主数据审核: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveMainData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核中".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核中的数据可以审核通过");
        }

        mainData.setDataStatus("审核通过");
        mainData.setFlowStatus("DONE");
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("主数据审核通过: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectMainData(String id) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveMainData(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        if (!"审核通过".equals(mainData.getDataStatus())) {
            throw BusinessException.of("只有审核通过的数据可以归档");
        }

        mainData.setDataStatus("已归档");
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("主数据归档: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVersion(String id) {
        MdmMainData mainData = mdmMainDataMapper.selectById(id);
        if (mainData == null) {
            throw BusinessException.of("数据不存在");
        }

        mainData.setVersion(mainData.getVersion() + 1);
        mainData.setIsModify(1);
        mainData.setModifyTime(LocalDateTime.now());
        mdmMainDataMapper.updateById(mainData);
        log.info("创建主数据新版本: {} -> version {}", id, mainData.getVersion());
    }
}