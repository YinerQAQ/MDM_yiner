package com.maike.mdm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.response.ImportResultResponse;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.entity.MdmModelAttribute;
import com.maike.mdm.mapper.MdmDataModelMapper;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.mapper.MdmModelAttributeMapper;
import com.maike.mdm.service.CodeRuleService;
import com.maike.mdm.service.ConstraintService;
import com.maike.mdm.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportServiceImpl implements DataImportService {

    private final MdmDataModelMapper mdmDataModelMapper;
    private final MdmModelAttributeMapper mdmModelAttributeMapper;
    private final MdmMainDataMapper mdmMainDataMapper;
    private final ConstraintService constraintService;
    private final CodeRuleService codeRuleService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse importData(String modelId, MultipartFile file, String importType) {
        // 验证模型存在
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在: " + modelId);
        }

        // 获取模型属性列表，按sortOrder排序
        List<MdmModelAttribute> attributes = getActiveAttributes(modelId);
        if (attributes.isEmpty()) {
            throw BusinessException.of("该模型没有定义属性，无法导入");
        }

        // 构建属性编码到属性的映射
        Map<String, MdmModelAttribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(MdmModelAttribute::getAttributeCode, a -> a, (a, b) -> a));

        // 属性编码列表（对应Excel列顺序）
        List<String> attrCodes = attributes.stream()
                .map(MdmModelAttribute::getAttributeCode)
                .collect(Collectors.toList());

        // 解析Excel
        List<Map<Integer, String>> dataList = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 读取完毕
                }
            }).sheet().doRead();
        } catch (IOException e) {
            throw BusinessException.of("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            throw BusinessException.of("Excel解析失败: " + e.getMessage());
        }

        // 处理每一行数据
        int successCount = 0;
        List<ImportResultResponse.ImportFailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            Map<Integer, String> row = dataList.get(i);
            int rowIndex = i + 2; // 第1行是表头，数据从第2行开始

            try {
                // 将行数据映射为属性编码-值
                Map<String, Object> dataMap = new LinkedHashMap<>();
                for (int col = 0; col < attrCodes.size(); col++) {
                    String value = row.get(col);
                    if (value != null && !value.trim().isEmpty()) {
                        dataMap.put(attrCodes.get(col), value);
                    }
                }

                // 填充默认值
                for (MdmModelAttribute attr : attributes) {
                    if (!dataMap.containsKey(attr.getAttributeCode())
                            && attr.getDefaultValue() != null
                            && !attr.getDefaultValue().isEmpty()) {
                        dataMap.put(attr.getAttributeCode(), attr.getDefaultValue());
                    }
                }

                // 约束校验
                List<String> errors = constraintService.validateConstraints(modelId, dataMap, null);
                if (errors != null && !errors.isEmpty()) {
                    failures.add(ImportResultResponse.ImportFailureDetail.builder()
                            .rowIndex(rowIndex)
                            .reason(String.join("; ", errors))
                            .build());
                    continue;
                }

                // 构建主数据对象
                String jsonDataStr = objectMapper.writeValueAsString(dataMap);
                String id = UUID.randomUUID().toString().replace("-", "");

                MdmMainData mainData = MdmMainData.builder()
                        .id(id)
                        .modelId(modelId)
                        .jsonData(jsonDataStr)
                        .version(1)
                        .isModify(0)
                        .isDeleted(0)
                        .createTime(LocalDateTime.now())
                        .modifyTime(LocalDateTime.now())
                        .build();

                if ("INIT".equalsIgnoreCase(importType)) {
                    // 初始化导入：直接生效
                    // 生成编码
                    if (model.getCodeRuleId() != null && !model.getCodeRuleId().isEmpty()) {
                        try {
                            Map<String, Object> dataContext = new HashMap<>(dataMap);
                            dataContext.put("modelCode", model.getModelCode());
                            String generatedCode = codeRuleService.generateCode(model.getCodeRuleId(), dataContext);
                            mainData.setCode(generatedCode);
                        } catch (Exception e) {
                            log.warn("初始化导入生成编码失败: {}", e.getMessage());
                        }
                    }
                    mainData.setDataStatus("审核通过");
                    mainData.setFlowStatus("DONE");
                } else {
                    // 普通导入：暂存状态
                    mainData.setDataStatus("暂存");
                    mainData.setFlowStatus("DRAFT");
                }

                mdmMainDataMapper.insert(mainData);
                successCount++;
            } catch (Exception e) {
                failures.add(ImportResultResponse.ImportFailureDetail.builder()
                        .rowIndex(rowIndex)
                        .reason(e.getMessage())
                        .build());
                log.warn("导入第{}行数据失败: {}", rowIndex, e.getMessage());
            }
        }

        log.info("数据导入完成: modelId={}, 成功={}, 失败={}", modelId, successCount, failures.size());

        return ImportResultResponse.builder()
                .successCount(successCount)
                .failCount(failures.size())
                .failures(failures)
                .build();
    }

    @Override
    public byte[] getImportTemplate(String modelId) {
        // 验证模型存在
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在: " + modelId);
        }

        List<MdmModelAttribute> attributes = getActiveAttributes(modelId);
        if (attributes.isEmpty()) {
            throw BusinessException.of("该模型没有定义属性，无法生成模板");
        }

        // 构建表头：使用属性名称作为列名
        List<List<String>> headList = attributes.stream()
                .map(attr -> {
                    String header = attr.getIsRequired() != null && attr.getIsRequired() == 1
                            ? attr.getAttributeName() + "(必填)"
                            : attr.getAttributeName();
                    return Collections.singletonList(header);
                })
                .collect(Collectors.toList());

        // 构建示例数据行
        List<List<String>> demoRow = attributes.stream()
                .map(attr -> {
                    String demo = "";
                    if (attr.getDefaultValue() != null && !attr.getDefaultValue().isEmpty()) {
                        demo = attr.getDefaultValue();
                    } else {
                        switch (attr.getDataType()) {
                            case "STRING":
                            case "TEXT":
                                demo = "示例文本";
                                break;
                            case "INTEGER":
                                demo = "0";
                                break;
                            case "DECIMAL":
                                demo = "0.00";
                                break;
                            case "DATE":
                                demo = "2024-01-01";
                                break;
                            case "DATETIME":
                                demo = "2024-01-01 00:00:00";
                                break;
                            case "BOOLEAN":
                                demo = "是/否";
                                break;
                            default:
                                demo = "";
                        }
                    }
                    return Collections.singletonList(demo);
                })
                .collect(Collectors.toList());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream);
            ExcelWriterSheetBuilder sheetBuilder = writerBuilder
                    .head(headList)
                    .sheet(model.getModelName() + "导入模板");
            sheetBuilder.doWrite(demoRow);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw BusinessException.of("生成模板失败: " + e.getMessage());
        }
    }

    private List<MdmModelAttribute> getActiveAttributes(String modelId) {
        LambdaQueryWrapper<MdmModelAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmModelAttribute::getModelId, modelId)
                .eq(MdmModelAttribute::getIsDeleted, 0)
                .orderByAsc(MdmModelAttribute::getSortOrder);
        return mdmModelAttributeMapper.selectList(queryWrapper);
    }
}