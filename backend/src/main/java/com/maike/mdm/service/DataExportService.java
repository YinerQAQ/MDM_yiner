package com.maike.mdm.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface DataExportService {

    /**
     * 导出数据
     * @param modelId 模型ID
     * @param fieldIds 要导出的属性ID列表（为空则导出全部属性）
     * @param format 格式: EXCEL/CSV
     * @param response HttpServletResponse
     */
    void exportData(String modelId, List<String> fieldIds, String format, HttpServletResponse response);
}