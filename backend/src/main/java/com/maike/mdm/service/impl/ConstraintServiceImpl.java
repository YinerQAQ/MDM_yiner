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
import com.maike.mdm.common.util.SafeSpelEvaluator;
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
        if (constraint.getConstraintConfig() != null) {
            existing.setConstraintConfig(constraint.getConstraintConfig());
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

    // ==================== 约束规则引擎：9种约束类型校验 ====================

    @Override
    public List<String> validateConstraints(String modelId, Map<String, Object> data, String oldDataId) {
        List<MdmModelConstraint> constraints = getConstraintsByModelId(modelId);
        List<String> errors = new ArrayList<>();
        for (MdmModelConstraint c : constraints) {
            if (!"启用".equals(c.getStatus())) {
                continue;
            }
            // 检查前置条件CONDITION_EXPR
            if (c.getConditionExpr() != null && !c.getConditionExpr().trim().isEmpty()) {
                try {
                    boolean conditionMet = evaluateSpelExpression(c.getConditionExpr(), data);
                    if (!conditionMet) {
                        continue; // 前置条件不满足，跳过
                    }
                } catch (Exception e) {
                    log.warn("前置条件评估异常: constraintId={}, expr={}", c.getId(), c.getConditionExpr());
                    continue;
                }
            }
            // 读取CONSTRAINT_CONFIG，降级CONFIG_JSON
            Map<String, Object> config = resolveConstraintConfig(c);
            String type = c.getConstraintType();
            if (type == null) continue;

            switch (type) {
                case "RANGE_CHECK" -> errors.addAll(checkRange(c, data, config));
                case "GROUP_REQUIRED" -> errors.addAll(checkGroupRequired(c, data, config));
                case "MASTER_SLAVE_REQUIRED" -> errors.addAll(checkMasterSlaveRequired(c, data, config));
                case "CROSS_MODEL_CHECK" -> errors.addAll(checkCrossModel(c, data, config));
                case "EXPRESSION_CHECK" -> errors.addAll(checkExpression(c, data, config));
                case "CHANGE_CONTROL" -> errors.addAll(checkChangeControl(c, data, config, oldDataId));
                case "MASTER_SLAVE_MODEL" -> errors.addAll(checkMasterSlaveModel(c, data, config));
                case "ATTACHMENT_REQUIRED" -> errors.addAll(checkAttachmentRequired(c, data, config));
                case "ONE_TO_MANY_CHANGE" -> errors.addAll(checkOneToManyChange(c, data, config, oldDataId));
                default -> log.warn("未知约束类型: {}", type);
            }
        }
        return errors;
    }

    /**
     * 读取CONSTRAINT_CONFIG，降级到CONFIG_JSON
     */
    private Map<String, Object> resolveConstraintConfig(MdmModelConstraint c) {
        String configStr = c.getConstraintConfig();
        if (configStr == null || configStr.trim().isEmpty()) {
            configStr = c.getConfigJson();
        }
        return parseConfigJson(configStr);
    }

    // ---------- RANGE_CHECK: 属性上下限校验 ----------
    private List<String> checkRange(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        String field = getStr(config, "field", c.getConstraintCode());
        Object value = data.get(field);
        if (value == null || value.toString().trim().isEmpty()) {
            return errors; // 值为空跳过
        }
        String type = getStr(config, "type", "NUMBER");
        try {
            if ("NUMBER".equalsIgnoreCase(type)) {
                double numValue = Double.parseDouble(value.toString());
                double min = config.containsKey("min") ? Double.parseDouble(config.get("min").toString()) : Double.MIN_VALUE;
                double max = config.containsKey("max") ? Double.parseDouble(config.get("max").toString()) : Double.MAX_VALUE;
                if (numValue < min || numValue > max) {
                    errors.add(String.format("属性上下限校验失败: 字段[%s]值%s不在范围[%s,%s]内", field, numValue, min, max));
                }
            } else if ("DATE".equalsIgnoreCase(type)) {
                String dateStr = value.toString();
                String minDate = getStr(config, "minDate", null);
                String maxDate = getStr(config, "maxDate", null);
                if (minDate != null && dateStr.compareTo(minDate) < 0) {
                    errors.add(String.format("属性上下限校验失败: 字段[%s]日期%s早于最小日期%s", field, dateStr, minDate));
                }
                if (maxDate != null && dateStr.compareTo(maxDate) > 0) {
                    errors.add(String.format("属性上下限校验失败: 字段[%s]日期%s晚于最大日期%s", field, dateStr, maxDate));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(String.format("属性上下限校验失败: 字段[%s]值不是有效数字", field));
        }
        return errors;
    }

    // ---------- GROUP_REQUIRED: 属性组必填校验 ----------
    private List<String> checkGroupRequired(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> fields = config.get("fields") != null ?
                (List<String>) config.get("fields") : Collections.singletonList(c.getConstraintCode());
        String mode = getStr(config, "mode", "AT_LEAST_ONE");
        int minRequired = config.containsKey("minRequired") ?
                Integer.parseInt(config.get("minRequired").toString()) : 1;

        long filledCount = fields.stream()
                .filter(f -> data.get(f) != null && !data.get(f).toString().trim().isEmpty())
                .count();

        switch (mode) {
            case "ALL_REQUIRED" -> {
                if (filledCount < fields.size()) {
                    errors.add(String.format("属性组必填校验失败: 字段%s必须全部填写", fields));
                }
            }
            case "ALL_EMPTY" -> {
                if (filledCount > 0) {
                    errors.add(String.format("属性组必填校验失败: 字段%s必须全部为空", fields));
                }
            }
            default -> {
                if (filledCount < minRequired) {
                    errors.add(String.format("属性组必填校验失败: 字段%s至少需要填写%d个", fields, minRequired));
                }
            }
        }
        return errors;
    }

    // ---------- MASTER_SLAVE_REQUIRED: 主从必填校验 ----------
    private List<String> checkMasterSlaveRequired(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        String masterField = getStr(config, "masterField", "");
        @SuppressWarnings("unchecked")
        List<String> slaveFields = config.get("slaveFields") != null ?
                (List<String>) config.get("slaveFields") :
                (config.get("detailFields") != null ? (List<String>) config.get("detailFields") : Collections.emptyList());

        Object masterValue = data.get(masterField);
        boolean masterHasValue = masterValue != null && !masterValue.toString().trim().isEmpty();

        if (masterHasValue) {
            for (String slaveField : slaveFields) {
                Object slaveValue = data.get(slaveField);
                if (slaveValue == null || slaveValue.toString().trim().isEmpty()) {
                    errors.add(String.format("主从必填校验失败: 主字段[%s]有值时，从字段[%s]必填", masterField, slaveField));
                }
            }
        }
        return errors;
    }

    // ---------- CROSS_MODEL_CHECK: 跨模型关联校验 ----------
    private List<String> checkCrossModel(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        String targetModel = getStr(config, "targetModel", "");
        String targetField = getStr(config, "targetField", "");
        String condition = getStr(config, "condition", "IN");
        @SuppressWarnings("unchecked")
        List<String> values = config.get("values") != null ?
                (List<String>) config.get("values") : Collections.emptyList();

        // 查找目标模型中的数据
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, targetModel)
                .eq(MdmMainData::getIsDeleted, 0);
        List<MdmMainData> targetDataList = mdmMainDataMapper.selectList(queryWrapper);

        boolean matchFound = false;
        for (MdmMainData targetData : targetDataList) {
            if (targetData.getJsonData() == null) continue;
            try {
                Map<String, Object> targetFields = objectMapper.readValue(targetData.getJsonData(),
                        new TypeReference<Map<String, Object>>() {});
                Object targetValue = targetFields.get(targetField);
                if (targetValue == null) continue;

                switch (condition) {
                    case "IN" -> {
                        if (values.contains(targetValue.toString())) {
                            matchFound = true;
                        }
                    }
                    case "NOT_IN" -> {
                        if (!values.contains(targetValue.toString())) {
                            matchFound = true;
                        }
                    }
                    case "EXISTS" -> matchFound = true;
                    default -> matchFound = true;
                }
                if (matchFound && "IN".equals(condition)) break;
            } catch (Exception e) {
                log.warn("解析目标模型数据JSON失败: id={}", targetData.getId());
            }
        }

        // IN条件：目标模型必须存在匹配值；NOT_IN：目标模型不能存在匹配值
        if ("IN".equals(condition) && !matchFound) {
            errors.add(String.format("跨模型关联校验失败: 目标模型[%s]字段[%s]不存在满足条件的值%s", targetModel, targetField, values));
        }
        if ("NOT_IN".equals(condition) && matchFound) {
            errors.add(String.format("跨模型关联校验失败: 目标模型[%s]字段[%s]存在不允许的值%s", targetModel, targetField, values));
        }
        return errors;
    }

    // ---------- EXPRESSION_CHECK: SpEL表达式校验 ----------
    private List<String> checkExpression(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        String expression = getStr(config, "expression", c.getConditionExpr());
        String errorMsg = getStr(config, "errorMsg", "SpEL表达式校验失败");

        if (expression == null || expression.trim().isEmpty()) {
            return errors;
        }
        try {
            boolean result = evaluateSpelExpression(expression, data);
            if (!result) {
                errors.add(errorMsg);
            }
        } catch (Exception e) {
            log.error("SpEL表达式评估失败: {}", expression, e);
            errors.add("SpEL表达式评估异常: " + e.getMessage());
        }
        return errors;
    }

    // ---------- CHANGE_CONTROL: 变更字段控制 ----------
    private List<String> checkChangeControl(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config, String oldDataId) {
        List<String> errors = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> lockedFields = config.get("lockedFields") != null ?
                (List<String>) config.get("lockedFields") : Collections.emptyList();
        String afterStatus = getStr(config, "afterStatus", "");

        // 只有在变更场景下（oldDataId不为空）才校验
        if (oldDataId == null || oldDataId.trim().isEmpty()) {
            return errors;
        }

        // 获取原始数据
        MdmMainData oldMainData = mdmMainDataMapper.selectById(oldDataId);
        if (oldMainData == null) {
            return errors;
        }

        // 仅在特定状态下锁定字段
        if (!afterStatus.isEmpty() && !afterStatus.equals(oldMainData.getDataStatus())) {
            return errors;
        }

        Map<String, Object> oldData = parseJsonData(oldMainData.getJsonData());
        for (String lockedField : lockedFields) {
            Object newValue = data.get(lockedField);
            Object oldValue = oldData.get(lockedField);
            if (oldValue != null && newValue != null && !oldValue.toString().equals(newValue.toString())) {
                errors.add(String.format("变更字段控制校验失败: 字段[%s]在%s状态下不允许变更", lockedField, afterStatus));
            }
        }
        return errors;
    }

    // ---------- MASTER_SLAVE_MODEL: 模型主从控制 ----------
    private List<String> checkMasterSlaveModel(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        String masterModel = getStr(config, "masterModel", "");
        String slaveModel = getStr(config, "slaveModel", "");
        String relationField = getStr(config, "relationField", "");
        String mode = getStr(config, "mode", "AT_LEAST_N");
        int minRecords = config.containsKey("minRecords") ?
                Integer.parseInt(config.get("minRecords").toString()) : 1;
        @SuppressWarnings("unchecked")
        List<String> requiredFields = config.get("requiredFields") != null ?
                (List<String>) config.get("requiredFields") : Collections.emptyList();

        // 查询从模型数据，验证关联数据存在性
        if (!slaveModel.isEmpty()) {
            LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MdmMainData::getModelId, slaveModel)
                    .eq(MdmMainData::getIsDeleted, 0);
            List<MdmMainData> slaveDataList = mdmMainDataMapper.selectList(queryWrapper);

            // 检查关联数据条数
            long relatedCount = 0;
            for (MdmMainData slaveData : slaveDataList) {
                if (slaveData.getJsonData() == null) continue;
                Map<String, Object> slaveFields = parseJsonData(slaveData.getJsonData());
                Object relValue = slaveFields.get(relationField);
                // 检查是否与当前数据关联
                Object dataId = data.get("id");
                if (relValue != null && dataId != null && relValue.toString().equals(dataId.toString())) {
                    relatedCount++;
                    // 检查从模型必填字段
                    for (String reqField : requiredFields) {
                        Object fieldValue = slaveFields.get(reqField);
                        if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
                            errors.add(String.format("模型主从控制校验失败: 从模型[%s]字段[%s]必填", slaveModel, reqField));
                        }
                    }
                }
            }

            if ("AT_LEAST_N".equals(mode) && relatedCount < minRecords) {
                errors.add(String.format("模型主从控制校验失败: 从模型[%s]至少需要%d条关联记录，当前%d条", slaveModel, minRecords, relatedCount));
            }
            if (("ALL_REQUIRED".equals(mode) || "SPECIFIED_REQUIRED".equals(mode)) && relatedCount == 0) {
                errors.add(String.format("模型主从控制校验失败: 从模型[%s]必须存在关联记录", slaveModel));
            }
        }
        return errors;
    }

    // ---------- ATTACHMENT_REQUIRED: 附件必填校验 ----------
    private List<String> checkAttachmentRequired(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> requiredStatus = config.get("requiredStatus") != null ?
                (List<String>) config.get("requiredStatus") : Collections.emptyList();
        int minCount = config.containsKey("minCount") ?
                Integer.parseInt(config.get("minCount").toString()) : 1;

        // 检查数据状态是否在必填状态列表中
        Object status = data.get("dataStatus");
        if (status == null) status = data.get("DATA_STATUS");
        if (!requiredStatus.isEmpty() && (status == null || !requiredStatus.contains(status.toString()))) {
            return errors; // 当前状态不在必填范围内
        }

        // 检查附件数量
        Object attachmentCount = data.get("attachmentCount");
        if (attachmentCount == null) attachmentCount = data.get("ATTACHMENT_COUNT");
        int count = 0;
        if (attachmentCount != null) {
            try {
                count = Integer.parseInt(attachmentCount.toString());
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (count < minCount) {
            errors.add(String.format("附件必填校验失败: 至少需要%d个附件，当前%d个", minCount, count));
        }
        return errors;
    }

    // ---------- ONE_TO_MANY_CHANGE: 一对多变更校验 ----------
    private List<String> checkOneToManyChange(MdmModelConstraint c, Map<String, Object> data, Map<String, Object> config, String oldDataId) {
        List<String> errors = new ArrayList<>();
        String childModel = getStr(config, "childModel", "");
        String relationField = getStr(config, "relationField", "");
        boolean allowDelete = config.containsKey("allowDelete") &&
                Boolean.parseBoolean(config.get("allowDelete").toString());
        String exceptionExpression = getStr(config, "exceptionExpression", "");

        // 检查是否满足例外条件
        if (!exceptionExpression.isEmpty()) {
            try {
                boolean isException = evaluateSpelExpression(exceptionExpression, data);
                if (isException) {
                    return errors; // 满足例外条件，跳过校验
                }
            } catch (Exception e) {
                log.warn("一对多变更例外表达式评估失败: {}", exceptionExpression);
            }
        }

        // 仅在变更场景下校验
        if (oldDataId == null || oldDataId.trim().isEmpty()) {
            return errors;
        }

        if (!allowDelete && !childModel.isEmpty()) {
            // 获取变更前的从模型数据
            LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MdmMainData::getModelId, childModel)
                    .eq(MdmMainData::getIsDeleted, 0);
            List<MdmMainData> oldChildData = mdmMainDataMapper.selectList(queryWrapper);

            // 统计原有从数据中关联到当前主数据的条数
            long oldRelatedCount = 0;
            for (MdmMainData child : oldChildData) {
                if (child.getJsonData() == null) continue;
                Map<String, Object> childFields = parseJsonData(child.getJsonData());
                Object relValue = childFields.get(relationField);
                if (relValue != null && relValue.toString().equals(oldDataId)) {
                    oldRelatedCount++;
                }
            }

            // 统计新数据中关联的从数据条数
            Object newChildCount = data.get("childCount");
            long newRelatedCount = oldRelatedCount; // 默认不变
            if (newChildCount != null) {
                try {
                    newRelatedCount = Long.parseLong(newChildCount.toString());
                } catch (NumberFormatException e) {
                    // keep default
                }
            }

            if (newRelatedCount < oldRelatedCount) {
                errors.add(String.format("一对多变更校验失败: 从模型[%s]不允许删除原有记录", childModel));
            }
        }
        return errors;
    }

    // ==================== 工具方法 ====================

    /**
     * 评估SpEL表达式，#data引用数据Map
     */
    private boolean evaluateSpelExpression(String expression, Map<String, Object> data) {
        Map<String, Object> variables = new java.util.HashMap<>(data);
        variables.put("data", data);
        return Boolean.TRUE.equals(SafeSpelEvaluator.evaluateBoolean(expression, variables));
    }

    /**
     * 解析JSON数据为Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonData(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析JSON数据失败: {}", jsonData, e);
            return new HashMap<>();
        }
    }

    /**
     * 安全获取字符串配置值
     */
    private String getStr(Map<String, Object> config, String key, String defaultValue) {
        Object val = config.get(key);
        return val != null ? val.toString() : defaultValue;
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
