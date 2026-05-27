package com.maike.mdm.service;

import com.maike.mdm.entity.MdmCodeRule;
import com.maike.mdm.entity.MdmCodeScheme;
import com.maike.mdm.entity.MdmCodeSegment;

import java.util.List;
import java.util.Map;

public interface CodeRuleService {

    // 编码规则CRUD
    List<MdmCodeRule> listRules();

    MdmCodeRule getRuleById(String id);

    void createRule(MdmCodeRule rule);

    void updateRule(String id, MdmCodeRule rule);

    void deleteRule(String id);

    // 编码方案管理
    List<MdmCodeScheme> getSchemesByRuleId(String ruleId);

    void createScheme(MdmCodeScheme scheme);

    void updateScheme(String id, MdmCodeScheme scheme);

    void deleteScheme(String id);

    // 编码段管理
    List<MdmCodeSegment> getSegmentsBySchemeId(String schemeId);

    void createSegment(MdmCodeSegment segment);

    void updateSegment(String id, MdmCodeSegment segment);

    void deleteSegment(String id);

    // 核心：生成编码
    String generateCode(String ruleId, Map<String, Object> dataContext);
}
