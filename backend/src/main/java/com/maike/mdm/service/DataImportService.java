package com.maike.mdm.service;

import com.maike.mdm.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DataImportService {

    /**
     * 导入数据
     * @param modelId 模型ID
     * @param file 上传的Excel文件
     * @param importType 导入类型: NORMAL(需审核)/INIT(直接生效)
     * @return 导入结果
     */
    ImportResultResponse importData(String modelId, MultipartFile file, String importType);

    /**
     * 生成导入模板Excel
     * @param modelId 模型ID
     * @return Excel文件字节数组
     */
    byte[] getImportTemplate(String modelId);
}