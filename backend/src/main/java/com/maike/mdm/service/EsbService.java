package com.maike.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.entity.MdmEsbInfoSystem;
import com.maike.mdm.entity.MdmEsbModelDist;
import com.maike.mdm.entity.MdmEsbModelDistContent;
import com.maike.mdm.entity.MdmEsbModelDistData;
import com.maike.mdm.entity.MdmEsbModelDistRecord;

import java.util.List;

public interface EsbService {

    // 分发接口管理
    List<MdmEsbModelDist> listDistByModelId(String modelId);

    MdmEsbModelDist getDistById(String id);

    void createDist(MdmEsbModelDist dist);

    void updateDist(MdmEsbModelDist dist);

    void deleteDist(String id);

    void enableDist(String id);

    void disableDist(String id);

    // 分发内容配置
    List<MdmEsbModelDistContent> getDistContent(String distId);

    void saveDistContent(String distId, List<MdmEsbModelDistContent> contents);

    // 分发执行
    void executeDistribute(String distId, List<String> dataIds);

    void addToDistQueue(String distId, String dataId);

    // 监控
    Page<MdmEsbModelDistData> getDistData(String distId, String status, Page<MdmEsbModelDistData> page);

    Page<MdmEsbModelDistRecord> getDistRecords(String distId, Page<MdmEsbModelDistRecord> page);

    // 信息系统管理
    List<MdmEsbInfoSystem> listSystems();

    void createSystem(MdmEsbInfoSystem system);

    void updateSystem(MdmEsbInfoSystem system);

    void deleteSystem(String id);
}
