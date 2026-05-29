package com.maike.mdm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.entity.MdmMainData;
import com.maike.mdm.entity.MdmModelAttribute;
import com.maike.mdm.mapper.MdmDataModelMapper;
import com.maike.mdm.mapper.MdmMainDataMapper;
import com.maike.mdm.mapper.MdmModelAttributeMapper;
import com.maike.mdm.service.DataExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportServiceImpl implements DataExportService {

    private final MdmDataModelMapper mdmDataModelMapper;
    private final MdmModelAttributeMapper mdmModelAttributeMapper;
    private final MdmMainDataMapper mdmMainDataMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void exportData(String modelId, List<String> fieldIds, String format, HttpServletResponse response) {
        // 验证模型
        MdmDataModel model = mdmDataModelMapper.selectById(modelId);
        if (model == null) {
            throw BusinessException.of("模型不存在: " + modelId);
        }

        // 获取模型属性
        List<MdmModelAttribute> allAttributes = getActiveAttributes(modelId);
        List<MdmModelAttribute> exportAttributes;

        if (fieldIds != null && !fieldIds.isEmpty()) {
            // 只导出指定字段
            Set<String> fieldIdSet = new HashSet<>(fieldIds);
            exportAttributes = allAttributes.stream()
                    .filter(attr -> fieldIdSet.contains(attr.getId()))
                    .collect(Collectors.toList());
        } else {
            exportAttributes = allAttributes;
        }

        if (exportAttributes.isEmpty()) {
            throw BusinessException.of("没有可导出的属性");
        }

        // 查询主数据
        LambdaQueryWrapper<MdmMainData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmMainData::getModelId, modelId)
                .eq(MdmMainData::getIsDeleted, 0)
                .orderByDesc(MdmMainData::getCreateTime);
        List<MdmMainData> dataList = mdmMainDataMapper.selectList(queryWrapper);

        // 构建导出数据
        List<String> attrCodes = exportAttributes.stream()
                .map(MdmModelAttribute::getAttributeCode)
                .collect(Collectors.toList());
        List<String> attrNames = exportAttributes.stream()
                .map(MdmModelAttribute::getAttributeName)
                .collect(Collectors.toList());

        // 解析每条主数据的jsonData
        List<Map<String, Object>> parsedRows = new ArrayList<>();
        for (MdmMainData data : dataList) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("数据编码", data.getCode());
            row.put("数据状态", data.getDataStatus());
            row.put("版本", data.getVersion() != null ? data.getVersion().toString() : "");

            if (data.getJsonData() != null && !data.getJsonData().trim().isEmpty()) {
                try {
                    Map<String, Object> jsonMap = objectMapper.readValue(data.getJsonData(),
                            new TypeReference<Map<String, Object>>() {});
                    for (String code : attrCodes) {
                        Object value = jsonMap.get(code);
                        row.put(code, value != null ? value.toString() : "");
                    }
                } catch (Exception e) {
                    log.warn("解析jsonData失败: dataId={}, error={}", data.getId(), e.getMessage());
                    for (String code : attrCodes) {
                        row.put(code, "");
                    }
                }
            } else {
                for (String code : attrCodes) {
                    row.put(code, "");
                }
            }
            parsedRows.add(row);
        }

        // 根据格式导出
        if ("CSV".equalsIgnoreCase(format)) {
            exportCsv(model, attrCodes, attrNames, parsedRows, response);
        } else {
            exportExcel(model, attrCodes, attrNames, parsedRows, response);
        }
    }

    private void exportExcel(MdmDataModel model, List<String> attrCodes, List<String> attrNames,
                             List<Map<String, Object>> rows, HttpServletResponse response) {
        try {
            String fileName = URLEncoder.encode(model.getModelName() + "_导出数据.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 构建表头
            List<List<String>> headList = new ArrayList<>();
            headList.add(Collections.singletonList("数据编码"));
            headList.add(Collections.singletonList("数据状态"));
            headList.add(Collections.singletonList("版本"));
            for (String name : attrNames) {
                headList.add(Collections.singletonList(name));
            }

            // 构建数据行
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                List<Object> rowData = new ArrayList<>();
                rowData.add(row.get("数据编码"));
                rowData.add(row.get("数据状态"));
                rowData.add(row.get("版本"));
                for (String code : attrCodes) {
                    rowData.add(row.getOrDefault(code, ""));
                }
                dataList.add(rowData);
            }

            EasyExcel.write(response.getOutputStream())
                    .head(headList)
                    .sheet(model.getModelName())
                    .doWrite(dataList);

        } catch (IOException e) {
            throw BusinessException.of("导出Excel失败: " + e.getMessage());
        }
    }

    private void exportCsv(MdmDataModel model, List<String> attrCodes, List<String> attrNames,
                           List<Map<String, Object>> rows, HttpServletResponse response) {
        try {
            String fileName = URLEncoder.encode(model.getModelName() + "_导出数据.csv", StandardCharsets.UTF_8);
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            PrintWriter writer = response.getWriter();

            // 写BOM以便Excel正确识别UTF-8
            writer.write('\uFEFF');

            // 表头
            List<String> headers = new ArrayList<>();
            headers.add("数据编码");
            headers.add("数据状态");
            headers.add("版本");
            headers.addAll(attrNames);
            writer.println(String.join(",", headers));

            // 数据行
            for (Map<String, Object> row : rows) {
                List<String> values = new ArrayList<>();
                values.add(csvEscape(row.get("数据编码")));
                values.add(csvEscape(row.get("数据状态")));
                values.add(csvEscape(row.get("版本")));
                for (String code : attrCodes) {
                    values.add(csvEscape(row.getOrDefault(code, "")));
                }
                writer.println(String.join(",", values));
            }

            writer.flush();
        } catch (IOException e) {
            throw BusinessException.of("导出CSV失败: " + e.getMessage());
        }
    }

    private String csvEscape(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private List<MdmModelAttribute> getActiveAttributes(String modelId) {
        LambdaQueryWrapper<MdmModelAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmModelAttribute::getModelId, modelId)
                .eq(MdmModelAttribute::getIsDeleted, 0)
                .orderByAsc(MdmModelAttribute::getSortOrder);
        return mdmModelAttributeMapper.selectList(queryWrapper);
    }
}