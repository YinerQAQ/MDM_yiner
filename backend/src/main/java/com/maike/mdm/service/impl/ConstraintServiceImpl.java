package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.ConstraintValidationResult;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.entity.MdmModelConstraint;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.mapper.MdmModelConstraintMapper;
import com.maike.mdm.service.ConstraintService;
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
public class ConstraintServiceImpl implements ConstraintService {

    private final MdmModelConstraintMapper mdmModelConstraintMapper;
    private final MdmMainDataMapper mdmMainDataMapper;
    private final ObjectMapper objectMapper;

    // ==================== 约束规则CRUD ====================

    @Override
    public List<MdmModelConstraint> getConstraintsByModelId(String modelId) {
        LambdaQueryWrapper<MdmModelConstraint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmModelConstraint::getModelId, modelId)
                .eq(MdmModelConstraint::getIsDeleted, 0)
                .orderByAsc(MdmModelConstraint::getSortOrder);
        return mdmModelConstraintMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createConstraint(MdmModelConstraint constraint) {
        constraint.setId(UUID.randomUUID().toString().replace("-", ""));
        constraint.setStatus(constraint.getStatus() != null ? constraint.getStatus() : "启用");
        constraint.setCreateTime(LocalDateTime.now());
        constraint.setUpdateTime(LocalDateTime.now());
        constraint.setIsDeleted(0);
        mdmModelConstraintMapper.insert(constraint);
        log.info("创建约束规则成功: modelId={}, constraintName={}", constraint.getModelId(), constraint.getConstraintName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConstraint(String id, MdmModelConstraint constraint) {
        MdmModelConstraint existing = mdmModelConstraintMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("约束规则不存在");
        }

        if (constraint.getConstraintName() != null) {
            existing.setConstraintName(constraint.getConstraintName());
        }
        if (constraint.getConstraintType() != null) {
            existing.setConstraintType(constraint.getConstraintType());
        }
        if (constraint.getSeverity() != null) {
            existing.setSeverity(constraint.getSeverity());
        }
        if (constraint.getScope() != null) {
            existing.setScope(constraint.getScope());
        }
        if (constraint.getConditionExpr() != null) {
            existing.setConditionExpr(constraint.getConditionExpr());
        }
        if (constraint.getConfigJson() != null) {
            existing.setConfigJson(constraint.getConfigJson());
        }
        if (constraint.getStatus() != null) {
            existing.setStatus(constraint.getStatus());
        }
        if (constraint.getSortOrder() != null) {
            existing.setSortOrder(constraint.getSortOrder());
        }
        existing.setUpdateTime(LocalDateTime.now());

        mdmModelConstraintMapper.updateById(existing);
        log.info("更新约束规则成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConstraint(String id) {
        MdmModelConstraint existing = mdmModelConstraintMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("约束规则不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        mdmModelConstraintMapper.updateById(existing);
        log.info("删除约束规则成功: {}", id);
    }

    // ==================== 核心：校验数据 ====================

    @Override
    public List<ConstraintValidationResult> validateData(String modelId, Map<String, Object> data) {
        List<MdmModelConstraint> constraints = getConstraintsByModelId(modelId);
        // 只校验启用的约束
        List<MdmModelConstraint> activeConstraints = constraints.stream()
                .filter(c -> "启用".equals(c.getStatus()))
                .collect(Collectors.toList());

        List<ConstraintValidationResult> results = new ArrayList<>();
        for (MdmModelConstraint constraint : activeConstraints) {
            ConstraintValidationResult result = validateConstraint(constraint, modelId, data);
            results.add(result);
        }

        long errorCount = results.stream().filter(r -> !r.isPassed() && "错误".equals(r.getSeverity())).count();
        long warningCount = results.stream().filter(r -> !r.isPassed() && "警告".equals(r.getSeverity())).count();
        log.info("数据校验完成: modelId={}, 通过={}, 错误={}, 警告={}",
                modelId, results.stream().filter(ConstraintValidationResult::isPassed).count(),
                errorCount, warningCount);

        return results;
    }

    /**
     * 校验单条约束
     */
    private ConstraintValidationResult validateConstraint(MdmModelConstraint constraint, String modelId, Map<String, Object> data) {
        String type = constraint.getConstraintType();
        ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder = ConstraintValidationResult.builder()
                .constraintName(constraint.getConstraintName())
                .constraintType(constraint.getConstraintType())
                .severity(constraint.getSeverity() != null ? constraint.getSeverity() : "错误");

        try {
            Map<String, Object> config = parseConfigJson(constraint.getConfigJson());

            if ("唯一约束".equals(type)) {
                return validateUniqueConstraint(constraint, modelId, data, config, resultBuilder);
            } else if (type != null && type.startsWith("关联约束")) {
                return validateRelationConstraint(type, constraint, data, config, resultBuilder);
            } else {
                return resultBuilder.passed(true).message("未知约束类型，跳过校验").build();
            }
        } catch (Exception e) {
            log.error("约束校验异常: constraintId={}, error={}", constraint.getId(), e.getMessage());
            return resultBuilder.passed(false).message("校验执行异常: " + e.getMessage()).build();
        }
    }

    /**
     * 唯一约束校验：检查指定字段组合是否已存在
     */
    private ConstraintValidationResult validateUniqueConstraint(
            MdmModelConstraint constraint, String modelId, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        @SuppressWarnings("unchecked")
        List<String> fields = config.get("fields") != null ?
                (List<String>) config.get("fields") : Collections.singletonList(constraint.getConstraintCode());

        String scope = constraint.getScope() != null ? constraint.getScope() : "全部";

        // 构建查询条件，检查是否已存在相同字段值的数据
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, modelId)
                .eq(MdmMainData::getIsDeleted, 0);

        // 根据scope添加过滤条件
        if ("类别".equals(scope) && data.containsKey("category")) {
            // 按类别范围校验 - 需要在JSON_DATA中匹配类别
        }
        // "全部"和"当前主数据"默认在当前模型范围内校验

        // 获取当前模型下的主数据，逐条检查字段组合
        List<MdmMainData> existingData = mdmMainDataMapper.selectList(queryWrapper);
        boolean duplicate = false;
        StringBuilder duplicateFields = new StringBuilder();

        for (MdmMainData existing : existingData) {
            if (existing.getJsonData() == null) continue;
            try {
                Map<String, Object> existingFields = objectMapper.readValue(existing.getJsonData(),
                        new TypeReference<Map<String, Object>>() {});

                boolean allMatch = true;
                for (String field : fields) {
                    Object dataValue = data.get(field);
                    Object existingValue = existingFields.get(field);
                    if (dataValue == null || existingValue == null || !dataValue.toString().equals(existingValue.toString())) {
                        allMatch = false;
                        break;
                    }
                }

                if (allMatch) {
                    duplicate = true;
                    duplicateFields = new StringBuilder(String.join(",", fields));
                    break;
                }
            } catch (Exception e) {
                log.warn("解析主数据JSON失败: id={}", existing.getId());
            }
        }

        if (duplicate) {
            return resultBuilder.passed(false)
                    .message("唯一约束校验失败: 字段[" + duplicateFields + "]组合已存在")
                    .build();
        }
        return resultBuilder.passed(true).message("唯一约束校验通过").build();
    }

    /**
     * 关联约束校验
     */
    private ConstraintValidationResult validateRelationConstraint(
            String type, MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        if (type.contains("上下限")) {
            return validateRangeConstraint(constraint, data, config, resultBuilder);
        } else if (type.contains("组必填")) {
            return validateGroupRequiredConstraint(constraint, data, config, resultBuilder);
        } else if (type.contains("主从必填")) {
            return validateMasterDetailRequiredConstraint(constraint, data, config, resultBuilder);
        } else if (type.contains("关联引用")) {
            return validateReferenceConstraint(constraint, data, config, resultBuilder);
        } else if (type.contains("自定义表达式")) {
            return validateCustomExpressionConstraint(constraint, data, config, resultBuilder);
        }

        return resultBuilder.passed(true).message("未知关联约束子类型，跳过校验").build();
    }

    /**
     * 关联约束-上下限：检查字段值是否在configJson中定义的范围内
     */
    private ConstraintValidationResult validateRangeConstraint(
            MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        String field = config.get("field") != null ? config.get("field").toString() : constraint.getConstraintCode();
        Object value = data.get(field);

        if (value == null) {
            return resultBuilder.passed(true).message("字段值为空，跳过上下限校验").build();
        }

        try {
            double numValue = Double.parseDouble(value.toString());
            double min = config.get("min") != null ? Double.parseDouble(config.get("min").toString()) : Double.MIN_VALUE;
            double max = config.get("max") != null ? Double.parseDouble(config.get("max").toString()) : Double.MAX_VALUE;

            if (numValue < min || numValue > max) {
                return resultBuilder.passed(false)
                        .message("上下限校验失败: 字段[" + field + "]值" + numValue + "不在范围[" + min + "," + max + "]内")
                        .build();
            }
        } catch (NumberFormatException e) {
            return resultBuilder.passed(false)
                    .message("上下限校验失败: 字段[" + field + "]值不是有效数字")
                    .build();
        }

        return resultBuilder.passed(true).message("上下限校验通过").build();
    }

    /**
     * 关联约束-组必填：检查一组字段是否全填或全空
     */
    private ConstraintValidationResult validateGroupRequiredConstraint(
            MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        @SuppressWarnings("unchecked")
        List<String> fields = config.get("fields") != null ?
                (List<String>) config.get("fields") : Collections.singletonList(constraint.getConstraintCode());

        long filledCount = fields.stream()
                .filter(f -> data.get(f) != null && !data.get(f).toString().trim().isEmpty())
                .count();

        if (filledCount > 0 && filledCount < fields.size()) {
            // 部分填写，部分未填，违反组必填
            return resultBuilder.passed(false)
                    .message("组必填校验失败: 字段" + fields + "必须全部填写或全部为空")
                    .build();
        }

        return resultBuilder.passed(true).message("组必填校验通过").build();
    }

    /**
     * 关联约束-主从必填：主字段有值时从字段必填
     */
    private ConstraintValidationResult validateMasterDetailRequiredConstraint(
            MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        String masterField = config.get("masterField") != null ? config.get("masterField").toString() : "";
        @SuppressWarnings("unchecked")
        List<String> detailFields = config.get("detailFields") != null ?
                (List<String>) config.get("detailFields") : Collections.emptyList();

        Object masterValue = data.get(masterField);
        boolean masterHasValue = masterValue != null && !masterValue.toString().trim().isEmpty();

        if (masterHasValue) {
            for (String detailField : detailFields) {
                Object detailValue = data.get(detailField);
                if (detailValue == null || detailValue.toString().trim().isEmpty()) {
                    return resultBuilder.passed(false)
                            .message("主从必填校验失败: 主字段[" + masterField + "]有值时，从字段[" + detailField + "]必填")
                            .build();
                }
            }
        }

        return resultBuilder.passed(true).message("主从必填校验通过").build();
    }

    /**
     * 关联约束-关联引用：检查字段值是否存在于指定模型的字段中
     */
    private ConstraintValidationResult validateReferenceConstraint(
            MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        String field = config.get("field") != null ? config.get("field").toString() : constraint.getConstraintCode();
        String refModelId = config.get("refModelId") != null ? config.get("refModelId").toString() : "";
        String refField = config.get("refField") != null ? config.get("refField").toString() : "";

        Object value = data.get(field);
        if (value == null || value.toString().trim().isEmpty()) {
            return resultBuilder.passed(true).message("字段值为空，跳过关联引用校验").build();
        }

        // 查询引用模型中的数据
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, refModelId)
                .eq(MdmMainData::getIsDeleted, 0);
        List<MdmMainData> refDataList = mdmMainDataMapper.selectList(queryWrapper);

        boolean found = false;
        for (MdmMainData refData : refDataList) {
            if (refData.getJsonData() == null) continue;
            try {
                Map<String, Object> refFields = objectMapper.readValue(refData.getJsonData(),
                        new TypeReference<Map<String, Object>>() {});
                Object refValue = refFields.get(refField);
                if (refValue != null && value.toString().equals(refValue.toString())) {
                    found = true;
                    break;
                }
            } catch (Exception e) {
                log.warn("解析引用主数据JSON失败: id={}", refData.getId());
            }
        }

        if (!found) {
            return resultBuilder.passed(false)
                    .message("关联引用校验失败: 字段[" + field + "]值[" + value + "]在引用模型中不存在")
                    .build();
        }

        return resultBuilder.passed(true).message("关联引用校验通过").build();
    }

    /**
     * 关联约束-自定义表达式：简单的条件表达式评估
     * 支持格式: field1 > 0, field1 == 'value', field1 != '' AND field2 > 10
     */
    private ConstraintValidationResult validateCustomExpressionConstraint(
            MdmModelConstraint constraint, Map<String, Object> data,
            Map<String, Object> config, ConstraintValidationResult.ConstraintValidationResultBuilder resultBuilder) {

        String expression = constraint.getConditionExpr();
        if (expression == null || expression.trim().isEmpty()) {
            return resultBuilder.passed(true).message("无自定义表达式，跳过校验").build();
        }

        try {
            boolean result = evaluateExpression(expression, data);
            if (!result) {
                return resultBuilder.passed(false)
                        .message("自定义表达式校验失败: " + expression)
                        .build();
            }
        } catch (Exception e) {
            log.error("自定义表达式评估失败: {}", expression, e);
            return resultBuilder.passed(false)
                    .message("自定义表达式评估异常: " + e.getMessage())
                    .build();
        }

        return resultBuilder.passed(true).message("自定义表达式校验通过").build();
    }

    /**
     * 评估自定义表达式
     * 支持简单的比较表达式和AND连接
     */
    private boolean evaluateExpression(String expression, Map<String, Object> data) {
        String[] conditions = expression.split("(?i)\\s+AND\\s+");
        for (String condition : conditions) {
            condition = condition.trim();

            // 支持 > >= < <= == != 操作符
            if (condition.contains(">=")) {
                if (!evaluateComparison(condition, data, ">=")) return false;
            } else if (condition.contains("<=")) {
                if (!evaluateComparison(condition, data, "<=")) return false;
            } else if (condition.contains("!=")) {
                if (!evaluateComparison(condition, data, "!=")) return false;
            } else if (condition.contains(">")) {
                if (!evaluateComparison(condition, data, ">")) return false;
            } else if (condition.contains("<")) {
                if (!evaluateComparison(condition, data, "<")) return false;
            } else if (condition.contains("==")) {
                if (!evaluateComparison(condition, data, "==")) return false;
            } else if (condition.contains("=")) {
                if (!evaluateComparison(condition, data, "=")) return false;
            }
        }
        return true;
    }

    /**
     * 评估单个比较表达式
     */
    private boolean evaluateComparison(String condition, Map<String, Object> data, String operator) {
        String[] parts = condition.split(operator.replace("(", "\\(").replace(")", "\\)"));
        if (parts.length != 2) return false;

        String field = parts[0].trim();
        String expectedStr = parts[1].trim().replace("'", "").replace("\"", "");
        Object actualValue = data.get(field);

        if (actualValue == null) return false;

        try {
            // 尝试数值比较
            double actualNum = Double.parseDouble(actualValue.toString());
            double expectedNum = Double.parseDouble(expectedStr);

            return switch (operator) {
                case ">=" -> actualNum >= expectedNum;
                case "<=" -> actualNum <= expectedNum;
                case ">" -> actualNum > expectedNum;
                case "<" -> actualNum < expectedNum;
                case "==" -> actualNum == expectedNum;
                case "=" -> actualNum == expectedNum;
                case "!=" -> actualNum != expectedNum;
                default -> false;
            };
        } catch (NumberFormatException e) {
            // 字符串比较
            String actualStr = actualValue.toString();
            return switch (operator) {
                case "==" -> actualStr.equals(expectedStr);
                case "=" -> actualStr.equals(expectedStr);
                case "!=" -> !actualStr.equals(expectedStr);
                case ">" -> actualStr.compareTo(expectedStr) > 0;
                case "<" -> actualStr.compareTo(expectedStr) < 0;
                case ">=" -> actualStr.compareTo(expectedStr) >= 0;
                case "<=" -> actualStr.compareTo(expectedStr) <= 0;
                default -> false;
            };
        }
    }

    /**
     * 解析configJson为Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfigJson(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析configJson失败: {}", configJson, e);
            return new HashMap<>();
        }
    }
}
