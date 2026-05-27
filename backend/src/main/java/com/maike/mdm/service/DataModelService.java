package com.maike.mdm.service;

import com.maike.mdm.entity.MdmDataModel;
import com.maike.mdm.entity.MdmModelAttribute;

import java.util.List;

public interface DataModelService {

    MdmDataModel createModel(MdmDataModel model);

    MdmDataModel getModelById(String id);

    List<MdmDataModel> getAllModels();

    MdmDataModel updateModel(String id, MdmDataModel model);

    void deleteModel(String id);

    void submitForReview(String id);

    void approveModel(String id);

    void rejectModel(String id);

    // 模型属性管理
    MdmModelAttribute addAttribute(String modelId, MdmModelAttribute attribute);

    MdmModelAttribute updateAttribute(String modelId, String attrId, MdmModelAttribute attribute);

    void deleteAttribute(String modelId, String attrId);

    List<MdmModelAttribute> getAttributesByModelId(String modelId);

    // 模型发布与变更
    void publishModel(String id);

    MdmDataModel changeModel(String id);
}
