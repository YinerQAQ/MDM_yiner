package com.maike.mdm.service;

import com.maike.mdm.entity.BaseDict;
import com.maike.mdm.entity.BaseDictItem;

import java.util.List;

public interface DictService {

    List<BaseDict> listDicts();

    List<BaseDictItem> getItemsByDictCode(String dictCode);

    void createDict(BaseDict dict);

    void updateDict(BaseDict dict);

    void deleteDict(String id);

    void createDictItem(BaseDictItem item);

    void updateDictItem(BaseDictItem item);

    void deleteDictItem(String id);
}
