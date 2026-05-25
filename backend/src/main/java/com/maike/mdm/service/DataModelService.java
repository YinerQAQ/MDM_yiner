package com.maike.mdm.service;

import com.maike.mdm.entity.MdmDataModel;

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
}