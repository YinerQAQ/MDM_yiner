package com.maike.mdm.service;

import com.maike.mdm.dto.request.MenuSortDTO;
import com.maike.mdm.entity.BaseMenu;

import java.util.List;

public interface MenuService {

    List<BaseMenu> getMenuTree();

    List<BaseMenu> getMenuTreeByUserId(String userId);

    void createMenu(BaseMenu menu);

    void updateMenu(BaseMenu menu);

    void deleteMenu(String id);

    void updateSort(List<MenuSortDTO> sortList);
}
