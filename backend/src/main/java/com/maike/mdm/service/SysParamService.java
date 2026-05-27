package com.maike.mdm.service;

import com.maike.mdm.entity.SysParam;

import java.util.List;

public interface SysParamService {

    List<SysParam> listParams();

    String getParamValue(String key);

    void updateParam(SysParam param);
}
