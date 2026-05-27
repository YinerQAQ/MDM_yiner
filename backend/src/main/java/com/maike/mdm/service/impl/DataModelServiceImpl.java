package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.entity.MdmModelAttribute;
import com.maike.mdm.mapper.MdmDataModelMapper;
import com.maike.mdm.mapper.MdmModelAttributeMapper;
import com.maike.mdm.service.DataModelService;
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
public class DataModelServiceImpl implements DataModelService {

    private final MdmDataModelMapper mdmDataModelMapper;
    private final MdmModelAttributeMapper mdmModelAttributeMapper;

    // ==================== 模型基本CRUD ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmDataModel createModel(MdmDataModel model) {
        LambdaQueryWrapper<MdmDataModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmDataModel::getModelCode, model.getModelCode());
        if (mdmDataModelMapper.exists(queryWrapper)) {
            throw BusinessException.of("模型编码已存在");
        }

        model.setId(UUID.randomUUID().toString().replace("-", ""));
        model.setStatus("编制中");
        model.setVersion(1);
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());
        model.setIsDeleted(0);

        mdmDataModelMapper.insert(model);
        log.info("创建数据模型成功: {}", model.getModelName());
        return model;
    }

    @Override
    public MdmDataModel getModelById(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }
        return model;
    }

    @Override
    public List<MdmDataModel> getAllModels() {
        LambdaQueryWrapper<MdmDataModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmDataModel::getIsDeleted, 0);
        return mdmDataModelMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmDataModel updateModel(String id, MdmDataModel model) {
        MdmDataModel existingModel = mdmDataModelMapper.selectById(id);
        if (existingModel == null) {
            throw BusinessException.of("模型不存在");
        }

        // 已发布模型不允许直接修改，需要通过变更流程
        if ("已发布".equals(existingModel.getStatus())) {
            throw BusinessException.of("已发布模型不允许直接修改，请发起变更流程");
        }

        if (!"编制中".equals(existingModel.getStatus()) && !"变更编制中".equals(existingModel.getStatus())) {
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

        if (!"编制中".equals(model.getStatus()) && !"变更编制中".equals(model.getStatus())) {
            throw BusinessException.of("只有编制中或变更编制中的模型可以删除");
        }

        model.setIsDeleted(1);
        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("删除数据模型成功: {}", id);
    }

    // ==================== 模型状态机 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if ("编制中".equals(model.getStatus())) {
            model.setStatus("审核中");
        } else if ("变更编制中".equals(model.getStatus())) {
            model.setStatus("变更审核中");
        } else {
            throw BusinessException.of("只有编制中或变更编制中的模型可以提交审核");
        }

        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("提交模型审核: {}, 新状态: {}", id, model.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if ("审核中".equals(model.getStatus())) {
            model.setStatus("已发布");
        } else if ("变更审核中".equals(model.getStatus())) {
            model.setStatus("已发布");
            model.setVersion(model.getVersion() + 1);
        } else {
            throw BusinessException.of("只有审核中或变更审核中的模型可以审核通过");
        }

        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("模型审核通过: {}, 新状态: {}, 版本: {}", id, model.getStatus(), model.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if ("审核中".equals(model.getStatus())) {
            model.setStatus("编制中");
        } else if ("变更审核中".equals(model.getStatus())) {
            model.setStatus("变更编制中");
        } else {
            throw BusinessException.of("只有审核中或变更审核中的模型可以拒绝");
        }

        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("模型审核拒绝: {}, 新状态: {}", id, model.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"审核中".equals(model.getStatus())) {
            throw BusinessException.of("只有审核中的模型可以发布");
        }

        model.setStatus("已发布");
        model.setUpdateTime(LocalDateTime.now());
        mdmDataModelMapper.updateById(model);
        log.info("模型发布成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmDataModel changeModel(String id) {
        MdmDataModel model = mdmDataModelMapper.selectById(id);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"已发布".equals(model.getStatus())) {
            throw BusinessException.of("只有已发布的模型可以发起变更");
        }

        // 复制当前模型为新版本记录，状态设为"变更编制中"
        MdmDataModel newModel = MdmDataModel.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .modelCode(model.getModelCode())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .status("变更编制中")
                .version(model.getVersion())
                .description(model.getDescription())
                .createBy(model.getCreateBy())
                .orgId(model.getOrgId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        mdmDataModelMapper.insert(newModel);

        // 复制原模型的属性到新版本
        List<MdmModelAttribute> originalAttributes = getAttributesByModelId(id);
        for (MdmModelAttribute attr : originalAttributes) {
            MdmModelAttribute newAttr = MdmModelAttribute.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .modelId(newModel.getId())
                    .attributeCode(attr.getAttributeCode())
                    .attributeName(attr.getAttributeName())
                    .dataType(attr.getDataType())
                    .isRequired(attr.getIsRequired())
                    .maxLength(attr.getMaxLength())
                    .defaultValue(attr.getDefaultValue())
                    .displayType(attr.getDisplayType())
                    .sortOrder(attr.getSortOrder())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .isDeleted(0)
                    .build();
            mdmModelAttributeMapper.insert(newAttr);
        }

        log.info("发起模型变更: 原模型={}, 新版本模型={}", id, newModel.getId());
        return newModel;
    }

    // ==================== 模型属性管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmModelAttribute addAttribute(String modelId, MdmModelAttribute attribute) {
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(model.getStatus()) && !"变更编制中".equals(model.getStatus())) {
            throw BusinessException.of("当前模型状态不允许添加属性");
        }

        // 检查属性编码唯一性
        LambdaQueryWrapper<MdmModelAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmModelAttribute::getModelId, modelId)
                .eq(MdmModelAttribute::getAttributeCode, attribute.getAttributeCode())
                .eq(MdmModelAttribute::getIsDeleted, 0);
        if (mdmModelAttributeMapper.exists(queryWrapper)) {
            throw BusinessException.of("属性编码已存在: " + attribute.getAttributeCode());
        }

        attribute.setId(UUID.randomUUID().toString().replace("-", ""));
        attribute.setModelId(modelId);
        attribute.setCreateTime(LocalDateTime.now());
        attribute.setUpdateTime(LocalDateTime.now());
        attribute.setIsDeleted(0);

        mdmModelAttributeMapper.insert(attribute);
        log.info("添加模型属性成功: modelId={}, attrCode={}", modelId, attribute.getAttributeCode());
        return attribute;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MdmModelAttribute updateAttribute(String modelId, String attrId, MdmModelAttribute attribute) {
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(model.getStatus()) && !"变更编制中".equals(model.getStatus())) {
            throw BusinessException.of("当前模型状态不允许修改属性");
        }

        MdmModelAttribute existing = mdmModelAttributeMapper.selectById(attrId);
        if (existing == null || !existing.getModelId().equals(modelId)) {
            throw BusinessException.of("属性不存在");
        }

        if (attribute.getAttributeName() != null) {
            existing.setAttributeName(attribute.getAttributeName());
        }
        if (attribute.getDataType() != null) {
            existing.setDataType(attribute.getDataType());
        }
        if (attribute.getIsRequired() != null) {
            existing.setIsRequired(attribute.getIsRequired());
        }
        if (attribute.getMaxLength() != null) {
            existing.setMaxLength(attribute.getMaxLength());
        }
        if (attribute.getDefaultValue() != null) {
            existing.setDefaultValue(attribute.getDefaultValue());
        }
        if (attribute.getDisplayType() != null) {
            existing.setDisplayType(attribute.getDisplayType());
        }
        if (attribute.getSortOrder() != null) {
            existing.setSortOrder(attribute.getSortOrder());
        }
        existing.setUpdateTime(LocalDateTime.now());

        mdmModelAttributeMapper.updateById(existing);
        log.info("更新模型属性成功: modelId={}, attrId={}", modelId, attrId);
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttribute(String modelId, String attrId) {
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在");
        }

        if (!"编制中".equals(model.getStatus()) && !"变更编制中".equals(model.getStatus())) {
            throw BusinessException.of("当前模型状态不允许删除属性");
        }

        MdmModelAttribute existing = mdmModelAttributeMapper.selectById(attrId);
        if (existing == null || !existing.getModelId().equals(modelId)) {
            throw BusinessException.of("属性不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        mdmModelAttributeMapper.updateById(existing);
        log.info("删除模型属性成功: modelId={}, attrId={}", modelId, attrId);
    }

    @Override
    public List<MdmModelAttribute> getAttributesByModelId(String modelId) {
        LambdaQueryWrapper<MdmModelAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmModelAttribute::getModelId, modelId)
                .eq(MdmModelAttribute::getIsDeleted, 0)
                .orderByAsc(MdmModelAttribute::getSortOrder);
        return mdmModelAttributeMapper.selectList(queryWrapper);
    }
}
