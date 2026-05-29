package com.maike.mdm.service;

import com.maike.mdm.dto.ConstraintValidationResult;
import com.maike.mdm.entity.MdmModelConstraint;

import java.util.List;
import java.util.Map;

public interface ConstraintService {

    // 约束规则CRUD
    List<MdmModelConstraint> getConstraintsByModelId(String modelId);

    void createConstraint(MdmModelConstraint constraint);

    void updateConstraint(String id, MdmModelConstraint constraint);

    void deleteConstraint(String id);

    // 核心：校验数据
    List<ConstraintValidationResult> validateData(String modelId, Map<String, Object> data);

    // 约束规则引擎：9种约束类型校验
    List<String> validateConstraints(String modelId, Map<String, Object> data, String oldDataId);
}
