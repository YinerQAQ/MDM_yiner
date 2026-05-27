package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmCodeRecord;
import com.maike.mdm.entity.MdmCodeRule;
import com.maike.mdm.entity.MdmCodeScheme;
import com.maike.mdm.entity.MdmCodeSegment;
import com.maike.mdm.mapper.MdmCodeRecordMapper;
import com.maike.mdm.mapper.MdmCodeRuleMapper;
import com.maike.mdm.mapper.MdmCodeSchemeMapper;
import com.maike.mdm.mapper.MdmCodeSegmentMapper;
import com.maike.mdm.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeRuleServiceImpl implements CodeRuleService {

    private final MdmCodeRuleMapper mdmCodeRuleMapper;
    private final MdmCodeSchemeMapper mdmCodeSchemeMapper;
    private final MdmCodeSegmentMapper mdmCodeSegmentMapper;
    private final MdmCodeRecordMapper mdmCodeRecordMapper;

    // ==================== 编码规则CRUD ====================

    @Override
    public List<MdmCodeRule> listRules() {
        LambdaQueryWrapper<MdmCodeRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmCodeRule::getIsDeleted, 0)
                .orderByDesc(MdmCodeRule::getCreateTime);
        return mdmCodeRuleMapper.selectList(queryWrapper);
    }

    @Override
    public MdmCodeRule getRuleById(String id) {
        MdmCodeRule rule = mdmCodeRuleMapper.selectById(id);
        if (rule == null) {
            throw BusinessException.of("编码规则不存在");
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRule(MdmCodeRule rule) {
        LambdaQueryWrapper<MdmCodeRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmCodeRule::getRuleCode, rule.getRuleCode());
        if (mdmCodeRuleMapper.exists(queryWrapper)) {
            throw BusinessException.of("规则编码已存在");
        }

        rule.setId(UUID.randomUUID().toString().replace("-", ""));
        rule.setStatus("启用");
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setIsDeleted(0);
        mdmCodeRuleMapper.insert(rule);
        log.info("创建编码规则成功: {}", rule.getRuleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(String id, MdmCodeRule rule) {
        MdmCodeRule existing = mdmCodeRuleMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码规则不存在");
        }

        if (rule.getRuleName() != null) {
            existing.setRuleName(rule.getRuleName());
        }
        if (rule.getOrgId() != null) {
            existing.setOrgId(rule.getOrgId());
        }
        if (rule.getStatus() != null) {
            existing.setStatus(rule.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());

        mdmCodeRuleMapper.updateById(existing);
        log.info("更新编码规则成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(String id) {
        MdmCodeRule existing = mdmCodeRuleMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码规则不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        mdmCodeRuleMapper.updateById(existing);
        log.info("删除编码规则成功: {}", id);
    }

    // ==================== 编码方案管理 ====================

    @Override
    public List<MdmCodeScheme> getSchemesByRuleId(String ruleId) {
        LambdaQueryWrapper<MdmCodeScheme> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmCodeScheme::getRuleId, ruleId)
                .eq(MdmCodeScheme::getIsDeleted, 0)
                .orderByAsc(MdmCodeScheme::getCreateTime);
        return mdmCodeSchemeMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createScheme(MdmCodeScheme scheme) {
        MdmCodeRule rule = mdmCodeRuleMapper.selectById(scheme.getRuleId());
        if (rule == null) {
            throw BusinessException.of("关联的编码规则不存在");
        }

        scheme.setId(UUID.randomUUID().toString().replace("-", ""));
        scheme.setCreateTime(LocalDateTime.now());
        scheme.setUpdateTime(LocalDateTime.now());
        scheme.setIsDeleted(0);
        mdmCodeSchemeMapper.insert(scheme);
        log.info("创建编码方案成功: {}", scheme.getSchemeName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScheme(String id, MdmCodeScheme scheme) {
        MdmCodeScheme existing = mdmCodeSchemeMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码方案不存在");
        }

        if (scheme.getSchemeName() != null) {
            existing.setSchemeName(scheme.getSchemeName());
        }
        if (scheme.getConditionExpression() != null) {
            existing.setConditionExpression(scheme.getConditionExpression());
        }
        existing.setUpdateTime(LocalDateTime.now());

        mdmCodeSchemeMapper.updateById(existing);
        log.info("更新编码方案成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScheme(String id) {
        MdmCodeScheme existing = mdmCodeSchemeMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码方案不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        mdmCodeSchemeMapper.updateById(existing);
        log.info("删除编码方案成功: {}", id);
    }

    // ==================== 编码段管理 ====================

    @Override
    public List<MdmCodeSegment> getSegmentsBySchemeId(String schemeId) {
        LambdaQueryWrapper<MdmCodeSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmCodeSegment::getSchemeId, schemeId)
                .eq(MdmCodeSegment::getIsDeleted, 0)
                .orderByAsc(MdmCodeSegment::getSortOrder);
        return mdmCodeSegmentMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSegment(MdmCodeSegment segment) {
        MdmCodeScheme scheme = mdmCodeSchemeMapper.selectById(segment.getSchemeId());
        if (scheme == null) {
            throw BusinessException.of("关联的编码方案不存在");
        }

        segment.setId(UUID.randomUUID().toString().replace("-", ""));
        segment.setCreateTime(LocalDateTime.now());
        segment.setUpdateTime(LocalDateTime.now());
        segment.setIsDeleted(0);
        mdmCodeSegmentMapper.insert(segment);
        log.info("创建编码段成功: {}", segment.getSegmentName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSegment(String id, MdmCodeSegment segment) {
        MdmCodeSegment existing = mdmCodeSegmentMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码段不存在");
        }

        if (segment.getSegmentName() != null) {
            existing.setSegmentName(segment.getSegmentName());
        }
        if (segment.getSegmentType() != null) {
            existing.setSegmentType(segment.getSegmentType());
        }
        if (segment.getSegmentFormat() != null) {
            existing.setSegmentFormat(segment.getSegmentFormat());
        }
        if (segment.getSegmentValue() != null) {
            existing.setSegmentValue(segment.getSegmentValue());
        }
        if (segment.getFixedValue() != null) {
            existing.setFixedValue(segment.getFixedValue());
        }
        if (segment.getSortOrder() != null) {
            existing.setSortOrder(segment.getSortOrder());
        }
        existing.setUpdateTime(LocalDateTime.now());

        mdmCodeSegmentMapper.updateById(existing);
        log.info("更新编码段成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSegment(String id) {
        MdmCodeSegment existing = mdmCodeSegmentMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.of("编码段不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        mdmCodeSegmentMapper.updateById(existing);
        log.info("删除编码段成功: {}", id);
    }

    // ==================== 核心：生成编码 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateCode(String ruleId, Map<String, Object> dataContext) {
        MdmCodeRule rule = mdmCodeRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw BusinessException.of("编码规则不存在");
        }

        // 1. 获取所有编码方案
        List<MdmCodeScheme> schemes = getSchemesByRuleId(ruleId);
        if (schemes.isEmpty()) {
            throw BusinessException.of("编码规则下没有配置编码方案");
        }

        // 2. 遍历方案，评估前置条件表达式
        MdmCodeScheme matchedScheme = null;
        for (MdmCodeScheme scheme : schemes) {
            if (scheme.getConditionExpression() == null || scheme.getConditionExpression().trim().isEmpty()) {
                // 无条件表达式，直接匹配
                matchedScheme = scheme;
                break;
            }
            if (evaluateCondition(scheme.getConditionExpression(), dataContext)) {
                matchedScheme = scheme;
                break;
            }
        }

        if (matchedScheme == null) {
            throw BusinessException.of("没有匹配的编码方案");
        }

        // 3. 获取匹配方案下所有编码段（按sortOrder排序）
        List<MdmCodeSegment> segments = getSegmentsBySchemeId(matchedScheme.getId());
        if (segments.isEmpty()) {
            throw BusinessException.of("编码方案下没有配置编码段");
        }

        // 4. 按段类型拼接编码
        StringBuilder codeBuilder = new StringBuilder();
        for (MdmCodeSegment segment : segments) {
            String segmentCode = generateSegmentCode(segment, matchedScheme.getId(), dataContext);
            codeBuilder.append(segmentCode);
        }

        String generatedCode = codeBuilder.toString();
        log.info("生成编码成功: ruleId={}, schemeId={}, code={}", ruleId, matchedScheme.getId(), generatedCode);
        return generatedCode;
    }

    /**
     * 根据段类型生成编码段
     */
    private String generateSegmentCode(MdmCodeSegment segment, String schemeId, Map<String, Object> dataContext) {
        String type = segment.getSegmentType();
        String format = segment.getSegmentFormat();

        switch (type != null ? type.toUpperCase() : "") {
            case "FIXED":
                // 固定值：直接使用fixedValue
                return segment.getFixedValue() != null ? segment.getFixedValue() : "";

            case "TIMESTAMP":
                // 时间戳：按format格式化当前时间
                String timeFormat = format != null ? format : "yyyyMMdd";
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat));

            case "SEQUENCE":
                // 序列号：获取并递增流水号
                return generateSequence(segment, schemeId);

            case "UUID":
                // UUID：生成UUID取前N位
                int uuidLength = 8;
                try {
                    uuidLength = format != null ? Integer.parseInt(format) : 8;
                } catch (NumberFormatException e) {
                    log.warn("UUID段格式不是有效数字，使用默认长度8: {}", format);
                }
                return UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(uuidLength, 32));

            case "REFERENCE":
                // 引用值：从dataContext中取指定字段值
                String refField = segment.getSegmentValue();
                if (refField != null && dataContext != null && dataContext.containsKey(refField)) {
                    Object value = dataContext.get(refField);
                    return value != null ? value.toString() : "";
                }
                return "";

            case "MANUAL":
                // 手动编码：从dataContext中取用户输入值
                String manualField = segment.getSegmentValue();
                if (manualField != null && dataContext != null && dataContext.containsKey(manualField)) {
                    Object value = dataContext.get(manualField);
                    return value != null ? value.toString() : "";
                }
                return "";

            default:
                log.warn("未知的编码段类型: {}", type);
                return "";
        }
    }

    /**
     * 生成序列号段，使用数据库行锁保证并发安全
     */
    private String generateSequence(MdmCodeSegment segment, String schemeId) {
        String prefix = buildSequencePrefix(segment, schemeId);
        String segmentId = segment.getId();
        String format = segment.getSegmentFormat();

        // 使用FOR UPDATE获取并锁定记录
        MdmCodeRecord record = mdmCodeRecordMapper.selectForUpdate(schemeId, segmentId, prefix);

        if (record == null) {
            // 首次使用，创建新记录
            record = MdmCodeRecord.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .schemeId(schemeId)
                    .segmentId(segmentId)
                    .prefix(prefix)
                    .currentValue(1L)
                    .updateTime(LocalDateTime.now())
                    .build();
            mdmCodeRecordMapper.insert(record);
        } else {
            // 递增流水号
            mdmCodeRecordMapper.incrementValue(schemeId, segmentId, prefix);
            record.setCurrentValue(record.getCurrentValue() + 1);
        }

        long seqValue = record.getCurrentValue();

        // 根据format格式化序列号
        if (format != null && !format.isEmpty()) {
            try {
                int digitCount = format.length();
                return String.format("%0" + digitCount + "d", seqValue);
            } catch (Exception e) {
                log.warn("序列号格式化失败: {}", format);
            }
        }

        return String.valueOf(seqValue);
    }

    /**
     * 构建序列号的prefix（用于区分不同前缀的流水号）
     */
    private String buildSequencePrefix(MdmCodeSegment segment, String schemeId) {
        // 收集当前方案中此SEQUENCE段之前的所有段的值作为prefix
        List<MdmCodeSegment> allSegments = getSegmentsBySchemeId(schemeId);
        StringBuilder prefixBuilder = new StringBuilder();
        for (MdmCodeSegment seg : allSegments) {
            if (seg.getId().equals(segment.getId())) {
                break;
            }
            if ("FIXED".equalsIgnoreCase(seg.getSegmentType())) {
                prefixBuilder.append(seg.getFixedValue() != null ? seg.getFixedValue() : "");
            } else if ("TIMESTAMP".equalsIgnoreCase(seg.getSegmentType())) {
                String timeFormat = seg.getSegmentFormat() != null ? seg.getSegmentFormat() : "yyyyMMdd";
                prefixBuilder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat)));
            }
        }
        return prefixBuilder.toString();
    }

    /**
     * 评估条件表达式（简单的字段值匹配）
     * 支持格式: field=value, field!=value
     * 支持多条件AND: field1=value1 AND field2=value2
     */
    private boolean evaluateCondition(String expression, Map<String, Object> dataContext) {
        if (dataContext == null || dataContext.isEmpty()) {
            return false;
        }

        String[] conditions = expression.split("(?i)\\s+AND\\s+");
        for (String condition : conditions) {
            condition = condition.trim();
            if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                if (parts.length != 2) return false;
                String field = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = dataContext.get(field);
                if (actualValue == null || !expectedValue.equals(actualValue.toString())) {
                    // 不等于条件满足
                    continue;
                }
                return false;
            } else if (condition.contains("=")) {
                String[] parts = condition.split("=");
                if (parts.length != 2) return false;
                String field = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = dataContext.get(field);
                if (actualValue == null || !expectedValue.equals(actualValue.toString())) {
                    return false;
                }
            }
        }
        return true;
    }
}
