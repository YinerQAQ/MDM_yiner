package com.maike.mdm.service;

import com.maike.mdm.entity.BaseMenu;
import com.maike.mdm.entity.BaseDataScope;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    /**
     * 获取用户所有权限标识集合
     */
    Set<String> getUserPermissions(String userId);

    /**
     * 获取用户可见菜单树
     */
    List<BaseMenu> getUserMenuTree(String userId);

    /**
     * 判断用户是否拥有指定权限
     */
    boolean hasPermission(String userId, String permission);

    /**
     * 获取用户数据范围SQL条件片段
     */
    String getDataScopeSql(String userId, String tableAlias);

    /**
     * 获取用户数据范围配置
     */
    List<BaseDataScope> getDataScope(String userId);
}
