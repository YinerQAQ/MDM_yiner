package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.mapper.MdmDataModelMapper;
import com.maike.mdm.service.DataModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataModelServiceImpl implements DataModelService {

    private final MdmDataModelMapper mdmDataModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmDataModel createModel(MdmDataModel model) {
        LambdaQueryWrapper<MdmDataModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmDataModel::getModelCode, model.getModelCode());
        if (mdmDataModelMapper.exists(queryWrapper)) {
            throw BusinessException.of("模型编码已存在");
        }

        model.setStatus("编制中");
        model.setVersion(1);
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());

        mdmDataModelMapper.insert(model);
        log.info("创建数据模型成功: {}", model.getModelName());
        return model;
    }

    @Override
    public MdmDataModel getModelById(String id) {
        return mdmDataModelMapper.selectById(id);
    }

    @Override
    public List<MdmDataModel> getAllModels() {
        return mdmDataModelMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmDataModel updateModel(String id, MdmDataModel model) {
        MdmDataModel existingModel = mdmDataModelMapper.selectById(id);
        if (existingModel == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(existingModel.getStatus()) && !"已发布".equals(existingModel.getStatus())) {
            throw BusinessException.of("当前状态不允许修改");
        }

        if (model.getModelName() != null) {
            existingModel.setModelName(model.getModelName());
        }
        if (model.getModelType() != null) {
            existingModel.setModelType(model.getModelType());
        }
        if (model.getDescription() != null) {
            existingModel.setDescription(model.getDescription());
        }
        existingModel.setUpdateTime(LocalDateTime.now());

        mdmDataModelMapper.updateById(existingModel);
        log.info("更新数据模型成功: {}", id);
        return existingModel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(model.getStatus())) {
            throw BusinessException.of("只有编制中的模型可以删除");
        }

        mdmDataModelMapper.deleteById(id);
        log.info("删除数据模型成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(model.getStatus())) {
            throw BusinessException.of("只有编制中的模型可以提交审核");
        }

        model.setStatus("审核中");
        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("提交模型审核: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"审核中".equals(model.getStatus())) {
            throw BusinessException.of("只有审核中的模型可以审核通过");
        }

        model.setStatus("已发布");
        model.setVersion(model.getVersion() + 1);
        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("模型审核通过: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"审核中".equals(model.getStatus())) {
            throw BusinessException.of("只有审核中的模型可以拒绝");
        }

        model.setStatus("编制中");
        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("模型审核拒绝: {}", id);
    }
}