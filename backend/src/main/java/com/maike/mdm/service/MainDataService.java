package com.maike.mdm.service;

import com.maike.mdm.entity.MdmMainData;

import java.util.List;

public interface MainDataService {

    MdmMainData createMainData(MdmMainData mainData);

    MdmMainData getMainDataById(String id);

    List<MdmMainData> getMainDataByModelId(String modelId);

    MdmMainData updateMainData(String id, MdmMainData mainData);

    void deleteMainData(String id);

    void submitForReview(String id);

    void approveMainData(String id);

    void rejectMainData(String id);

    void archiveMainData(String id);

    void createVersion(String id);
}