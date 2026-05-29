package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmCodeRecord;
import com.maike.mdm.entity.MdmCodeRule;
import com.maike.mdm.entity.MdmCodeScheme;
import com.maike.mdm.entity.MdmCodeSegment;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.mapper.MdmCodeRecordMapper;
import com.maike.mdm.mapper.MdmCodeRuleMapper;
import com.maike.mdm.mapper.MdmCodeSchemeMapper;
import com.maike.mdm.mapper.MdmCodeSegmentMapper;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final MdmMainDataMapper mdmMainDataMapper;
    private final JdbcTemplate jdbcTemplate;

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
        if (segment.getSegmentLength() != null) {
            existing.setSegmentLength(segment.getSegmentLength());
        }
        if (segment.getExpression() != null) {
            existing.setExpression(segment.getExpression());
        }
        if (segment.getReferenceField() != null) {
            existing.setReferenceField(segment.getReferenceField());
        }
        if (segment.getRelatedSegmentId() != null) {
            existing.setRelatedSegmentId(segment.getRelatedSegmentId());
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

        // 4. 按段类型拼接编码，同时记录每段已生成的值（用于段间关联）
        Map<String, String> generatedSegments = new LinkedHashMap<>();
        StringBuilder codeBuilder = new StringBuilder();
        for (MdmCodeSegment segment : segments) {
            String segmentCode = generateSegmentCode(segment, matchedScheme.getId(), dataContext, generatedSegments);
            generatedSegments.put(segment.getId(), segmentCode);
            codeBuilder.append(segmentCode);
        }

        String generatedCode = codeBuilder.toString();

        // 5. 编码重复校验
        String modelId = dataContext != null ? getFieldValue(dataContext, "modelId") : null;
        String excludeDataId = dataContext != null ? getFieldValue(dataContext, "excludeDataId") : null;
        checkCodeDuplicate(modelId, generatedCode, excludeDataId);

        log.info("生成编码成功: ruleId={}, schemeId={}, code={}", ruleId, matchedScheme.getId(), generatedCode);
        return generatedCode;
    }

    /**
     * 根据段类型生成编码段（支持段间关联）
     */
    private String generateSegmentCode(MdmCodeSegment segment, String schemeId,
                                       Map<String, Object> dataContext,
                                       Map<String, String> generatedSegments) {
        String type = segment.getSegmentType();
        String format = segment.getSegmentFormat();

        switch (type != null ? type.toUpperCase() : "") {
            case "FIXED":
                return segment.getFixedValue() != null ? segment.getFixedValue() : "";

            case "TIMESTAMP":
                String timeFormat = format != null ? format : "yyyyMMdd";
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat));

            case "SEQUENCE": {
                // 段间关联：如果配置了relatedSegmentId，则使用关联段的值作为prefix
                String prefix = "";
                if (segment.getRelatedSegmentId() != null && !segment.getRelatedSegmentId().isEmpty()) {
                    prefix = generatedSegments.getOrDefault(segment.getRelatedSegmentId(), "");
                } else {
                    prefix = buildSequencePrefix(segment, schemeId);
                }
                return generateSequence(segment, schemeId, prefix);
            }

            case "UUID": {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                int length = segment.getSegmentLength() != null ? segment.getSegmentLength() : 32;
                return uuid.substring(0, Math.min(length, uuid.length()));
            }

            case "HIERARCHY": {
                String categoryPath = getFieldValue(dataContext, segment.getReferenceField());
                return generateHierarchyCode(categoryPath, segment);
            }

            case "REFERENCE": {
                String refField = segment.getReferenceField() != null ? segment.getReferenceField() : segment.getSegmentValue();
                if (refField != null && dataContext != null && dataContext.containsKey(refField)) {
                    Object value = dataContext.get(refField);
                    return value != null ? value.toString() : "";
                }
                return "";
            }

            case "MANUAL": {
                String manualCode = getFieldValue(dataContext, "manualCode_" + segment.getId());
                if (manualCode == null || manualCode.isEmpty()) {
                    throw BusinessException.of("手动编码段[" + segment.getSegmentName() + "]未填写");
                }
                return manualCode;
            }

            case "FIRST_LETTER": {
                String nameField = segment.getReferenceField() != null ? segment.getReferenceField() : segment.getSegmentValue();
                String nameValue = getFieldValue(dataContext, nameField);
                String firstLetter = getFirstLetter(nameValue);
                long seq = getNextSequence(segment.getId(), firstLetter, segment, schemeId);
                int seqDigitCount = segment.getSegmentLength() != null
                        ? Math.max(segment.getSegmentLength() - firstLetter.length(), 1) : 3;
                return firstLetter + String.format("%0" + seqDigitCount + "d", seq);
            }

            case "LETTER_SEQUENCE": {
                String prefix = "";
                if (segment.getRelatedSegmentId() != null && !segment.getRelatedSegmentId().isEmpty()) {
                    prefix = generatedSegments.getOrDefault(segment.getRelatedSegmentId(), "");
                }
                return generateLetterSequence(segment, schemeId, prefix);
            }

            case "SQL_EXPR": {
                String sqlTemplate = segment.getExpression();
                if (sqlTemplate == null || sqlTemplate.trim().isEmpty()) {
                    throw BusinessException.of("SQL表达式段[" + segment.getSegmentName() + "]未配置表达式");
                }
                // 安全校验：禁止危险SQL关键字
                String upperSql = sqlTemplate.toUpperCase();
                if (upperSql.contains("DROP ") || upperSql.contains("DELETE ") ||
                    upperSql.contains("UPDATE ") || upperSql.contains("INSERT ") ||
                    upperSql.contains("ALTER ") || upperSql.contains("TRUNCATE ")) {
                    throw BusinessException.of("SQL表达式包含不允许的操作");
                }
                // 将 ${xxx} 占位符转换为 :xxx 命名参数，防止SQL注入
                String parameterizedSql = sqlTemplate.replaceAll("\\$\\{(\\w+)\\}", ":$1");
                Map<String, Object> params = new HashMap<>();
                if (dataContext != null) {
                    // 只提取SQL中实际引用的参数
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(":(\\w+)").matcher(parameterizedSql);
                    while (matcher.find()) {
                        String paramName = matcher.group(1);
                        if (dataContext.containsKey(paramName)) {
                            params.put(paramName, dataContext.get(paramName));
                        }
                    }
                }
                try {
                    org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate namedJdbc =
                            new org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate(jdbcTemplate);
                    String result = namedJdbc.queryForObject(parameterizedSql, params, String.class);
                    return result != null ? result : "";
                } catch (Exception e) {
                    log.error("SQL表达式执行失败: {}", parameterizedSql, e);
                    throw BusinessException.of("SQL表达式执行失败: " + e.getMessage());
                }
            }

            case "RANDOM": {
                // 随机码段：UUID模式或时间戳+随机数模式
                String randomType = segment.getSegmentValue() != null ? segment.getSegmentValue() : "UUID";
                if ("TIMESTAMP_RANDOM".equalsIgnoreCase(randomType)) {
                    long timestamp = System.currentTimeMillis();
                    int random = new java.util.Random().nextInt(900000) + 100000;
                    return Long.toHexString(timestamp) + Integer.toHexString(random);
                } else {
                    // 默认UUID模式
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    int length = segment.getSegmentLength() != null ? segment.getSegmentLength() : 32;
                    return uuid.substring(0, Math.min(length, uuid.length()));
                }
            }

            default:
                log.warn("未知的编码段类型: {}", type);
                return "";
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 安全获取字段值
     */
    private String getFieldValue(Map<String, Object> data, String fieldName) {
        if (data == null || fieldName == null) {
            return null;
        }
        Object value = data.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * 生成层级码
     * 根据分类路径（如 01.02.03）和segment配置生成层级编码
     */
    private String generateHierarchyCode(String categoryPath, MdmCodeSegment segment) {
        if (categoryPath == null || categoryPath.isEmpty()) {
            // 无上级分类，生成顶级编码
            return generateNextLevelCode("", segment);
        }
        // 有上级分类，在其下生成子级编码
        return generateNextLevelCode(categoryPath, segment);
    }

    /**
     * 生成下一级层级编码
     */
    private String generateNextLevelCode(String parentCode, MdmCodeSegment segment) {
        // 从expression中解析层级配置（JSON格式: {"levels":[2,2,3],"separator":"."}）
        String expression = segment.getExpression();
        int currentLevel;
        int levelDigits;
        String separator = ".";

        if (expression != null && !expression.trim().isEmpty()) {
            try {
                // 简单解析JSON配置
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> config = mapper.readValue(expression, java.util.Map.class);
                java.util.List<Number> levels = (java.util.List<Number>) config.get("levels");
                if (config.containsKey("separator")) {
                    separator = (String) config.get("separator");
                }
                // 计算当前层级
                if (parentCode == null || parentCode.isEmpty()) {
                    currentLevel = 0;
                } else {
                    currentLevel = parentCode.split(java.util.regex.Pattern.quote(separator)).length;
                }
                if (currentLevel >= levels.size()) {
                    throw BusinessException.of("层级码超过最大层级");
                }
                levelDigits = levels.get(currentLevel).intValue();
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("层级码配置解析失败，使用默认配置: {}", expression);
                levelDigits = 2;
            }
        } else {
            levelDigits = 2;
        }

        // 查询同级已有最大编码
        String likePattern = (parentCode == null || parentCode.isEmpty())
                ? "___"
                : parentCode + separator + "___";

        // 使用jdbcTemplate查询同级最大编码
        String tableName = "MDM_MAIN_DATA";
        String sql;
        if (parentCode == null || parentCode.isEmpty()) {
            sql = "SELECT MAX(CODE) FROM " + tableName + " WHERE CODE REGEXP '^[0-9]{" + levelDigits + "}$' AND IS_DELETED = 0";
        } else {
            String escapedParent = parentCode.replace("'", "''");
            String escapedSep = separator.replace("'", "''");
            sql = "SELECT MAX(CODE) FROM " + tableName + " WHERE CODE LIKE '"
                    + escapedParent + escapedSep + "%' AND IS_DELETED = 0"
                    + " AND LENGTH(CODE) - LENGTH(REPLACE(CODE, '" + escapedSep + "', '')) = "
                    + (parentCode.split(java.util.regex.Pattern.quote(separator)).length);
        }

        try {
            String maxCode = jdbcTemplate.queryForObject(sql, String.class);
            int nextValue = 1;
            if (maxCode != null) {
                // 提取最后一段的数值
                String lastPart = maxCode;
                if (parentCode != null && !parentCode.isEmpty()) {
                    int lastSepIdx = maxCode.lastIndexOf(separator);
                    if (lastSepIdx >= 0) {
                        lastPart = maxCode.substring(lastSepIdx + separator.length());
                    }
                }
                try {
                    nextValue = Integer.parseInt(lastPart) + 1;
                } catch (NumberFormatException e) {
                    nextValue = 1;
                }
            }
            String nextPart = String.format("%0" + levelDigits + "d", nextValue);
            return (parentCode == null || parentCode.isEmpty()) ? nextPart : parentCode + separator + nextPart;
        } catch (Exception e) {
            log.warn("层级码查询失败，使用默认值: {}", e.getMessage());
            String nextPart = String.format("%0" + levelDigits + "d", 1);
            return (parentCode == null || parentCode.isEmpty()) ? nextPart : parentCode + separator + nextPart;
        }
    }

    /**
     * 获取带前缀的序列号（SELECT FOR UPDATE保证并发安全）
     */
    private long getNextSequence(String segmentId, String prefix, MdmCodeSegment segment, String schemeId) {
        // 使用FOR UPDATE获取并锁定记录
        MdmCodeRecord record = mdmCodeRecordMapper.selectForUpdate(schemeId, segmentId, prefix != null ? prefix : "");

        if (record == null) {
            record = MdmCodeRecord.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .schemeId(schemeId)
                    .segmentId(segmentId)
                    .prefix(prefix != null ? prefix : "")
                    .currentValue(1L)
                    .updateTime(LocalDateTime.now())
                    .build();
            mdmCodeRecordMapper.insert(record);
        } else {
            mdmCodeRecordMapper.incrementValue(schemeId, segmentId, prefix != null ? prefix : "");
            record.setCurrentValue(record.getCurrentValue() + 1);
        }
        return record.getCurrentValue();
    }

    /**
     * 生成序列号段，使用数据库行锁保证并发安全
     */
    private String generateSequence(MdmCodeSegment segment, String schemeId, String prefix) {
        String segmentId = segment.getId();
        String format = segment.getSegmentFormat();

        long seqValue = getNextSequence(segmentId, prefix, segment, schemeId);

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
     * 生成字母流水段
     */
    private String generateLetterSequence(MdmCodeSegment segment, String schemeId, String prefix) {
        String segmentId = segment.getId();
        long seqValue = getNextSequence(segmentId, prefix, segment, schemeId);
        return toLetters(seqValue);
    }

    /**
     * 数字转字母: 1→A, 2→B, ..., 26→Z, 27→AA, 28→AB, ...
     */
    private String toLetters(long n) {
        StringBuilder result = new StringBuilder();
        while (n > 0) {
            n -= 1;
            result.insert(0, (char) ('A' + (n % 26)));
            n = n / 26;
        }
        return result.toString();
    }

    /**
     * 获取拼音首字母
     */
    private String getFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "X";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            } else if (c >= 'a' && c <= 'z') {
                sb.append(Character.toUpperCase(c));
            } else if (c >= 0x4E00 && c <= 0x9FA5) {
                // 中文字符，尝试获取拼音首字母
                sb.append(getChinesePinyinFirstLetter(c));
            }
            if (sb.length() >= 2) break; // 取前两个首字母
        }
        return sb.length() > 0 ? sb.toString() : "X";
    }

    /**
     * 获取中文字符的拼音首字母（简化版）
     */
    private char getChinesePinyinFirstLetter(char c) {
        // GB2312拼音首字母对照表（简化版）
        int charCode = c;
        if (charCode >= 0x4E00 && charCode <= 0x9FA5) {
            try {
                byte[] bytes = String.valueOf(c).getBytes("GB2312");
                if (bytes.length < 2) return 'X';
                int code = ((bytes[0] & 0xFF) * 256) + (bytes[1] & 0xFF);
                if (code >= 45217 && code <= 45252) return 'A';
                if (code >= 45253 && code <= 45760) return 'B';
                if (code >= 45761 && code <= 46317) return 'C';
                if (code >= 46318 && code <= 46825) return 'D';
                if (code >= 46826 && code <= 47009) return 'E';
                if (code >= 47010 && code <= 47296) return 'F';
                if (code >= 47297 && code <= 47613) return 'G';
                if (code >= 47614 && code <= 48118) return 'H';
                if (code >= 48119 && code <= 49061) return 'J';
                if (code >= 49062 && code <= 49323) return 'K';
                if (code >= 49324 && code <= 49895) return 'L';
                if (code >= 49896 && code <= 50370) return 'M';
                if (code >= 50371 && code <= 50613) return 'N';
                if (code >= 50614 && code <= 50621) return 'O';
                if (code >= 50622 && code <= 50905) return 'P';
                if (code >= 50906 && code <= 51386) return 'Q';
                if (code >= 51387 && code <= 51445) return 'R';
                if (code >= 51446 && code <= 52217) return 'S';
                if (code >= 52218 && code <= 52697) return 'T';
                if (code >= 52698 && code <= 52979) return 'W';
                if (code >= 52980 && code <= 53640) return 'X';
                if (code >= 53689 && code <= 54480) return 'Y';
                if (code >= 54481 && code <= 55289) return 'Z';
            } catch (Exception e) {
                log.warn("拼音首字母获取失败: {}", c);
            }
        }
        return 'X';
    }

    /**
     * 编码重复校验 - 在生成编码后验证唯一性
     */
    private void checkCodeDuplicate(String modelId, String generatedCode, String excludeDataId) {
        if (modelId == null || modelId.isEmpty()) {
            return; // 无modelId时不做校验
        }
        QueryWrapper<MdmMainData> wrapper = new QueryWrapper<>();
        wrapper.eq("MODEL_ID", modelId)
               .eq("CODE", generatedCode)
               .ne("DATA_STATUS", "已删除");
        if (excludeDataId != null && !excludeDataId.isEmpty()) {
            wrapper.ne("ID", excludeDataId);
        }
        Long count = mdmMainDataMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw BusinessException.of("编码[" + generatedCode + "]已存在，请检查编码规则配置");
        }
    }

    /**
     * 构建序列号的prefix（用于区分不同前缀的流水号）
     */
    private String buildSequencePrefix(MdmCodeSegment segment, String schemeId) {
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
