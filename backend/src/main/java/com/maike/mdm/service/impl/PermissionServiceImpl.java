package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.entity.*;
import com.maike.mdm.mapper.*;
import com.maike.mdm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final BaseUserRoleMapper userRoleMapper;
    private final BaseRoleMenuMapper roleMenuMapper;
    private final BaseMenuMapper menuMapper;
    private final BaseUserGroupMapper userGroupMapper;
    private final BaseDataScopeMapper dataScopeMapper;
    private final BaseOrgMapper orgMapper;
    private final BaseUserMapper userMapper;

    @Override
    public Set<String> getUserPermissions(String userId) {
        // 1. 查询用户所有角色ID
        LambdaQueryWrapper<BaseUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(BaseUserRole::getUserId, userId);
        List<BaseUserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> roleIds = userRoles.stream()
                .map(BaseUserRole::getRoleId)
                .collect(Collectors.toList());

        // 2. 查询角色关联的菜单ID
        LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.in(BaseRoleMenu::getRoleId, roleIds);
        List<BaseRoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuQuery);
        if (roleMenus.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> menuIds = roleMenus.stream()
                .map(BaseRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询菜单的PERMS字段
        List<BaseMenu> menus = menuMapper.selectBatchIds(menuIds);
        return menus.stream()
                .map(BaseMenu::getPerms)
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public List<BaseMenu> getUserMenuTree(String userId) {
        // 1. 查询用户所有角色ID
        LambdaQueryWrapper<BaseUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(BaseUserRole::getUserId, userId);
        List<BaseUserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> roleIds = userRoles.stream()
                .map(BaseUserRole::getRoleId)
                .collect(Collectors.toList());

        // 2. 查询角色关联的菜单ID
        LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.in(BaseRoleMenu::getRoleId, roleIds);
        List<BaseRoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuQuery);
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> menuIds = roleMenus.stream()
                .map(BaseRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询菜单（排除按钮类型，只查目录和菜单）
        LambdaQueryWrapper<BaseMenu> menuQuery = new LambdaQueryWrapper<>();
        menuQuery.in(BaseMenu::getId, menuIds)
                .ne(BaseMenu::getMenuType, "按钮")
                .orderByAsc(BaseMenu::getSortOrder);
        List<BaseMenu> menus = menuMapper.selectList(menuQuery);

        return buildTree(menus);
    }

    @Override
    public boolean hasPermission(String userId, String permission) {
        return getUserPermissions(userId).contains(permission);
    }

    @Override
    public String getDataScopeSql(String userId, String tableAlias) {
        // 1. 获取用户所属用户组列表
        LambdaQueryWrapper<BaseUserGroup> userGroupQuery = new LambdaQueryWrapper<>();
        userGroupQuery.eq(BaseUserGroup::getUserId, userId);
        List<BaseUserGroup> userGroups = userGroupMapper.selectList(userGroupQuery);

        if (userGroups.isEmpty()) {
            // 无用户组，默认仅本人
            return tableAlias + ".CREATED_BY_ID = '" + userId + "'";
        }

        List<String> groupIds = userGroups.stream()
                .map(BaseUserGroup::getGroupId)
                .collect(Collectors.toList());

        // 2. 获取每个用户组的数据范围配置
        LambdaQueryWrapper<BaseDataScope> scopeQuery = new LambdaQueryWrapper<>();
        scopeQuery.in(BaseDataScope::getGroupId, groupIds);
        List<BaseDataScope> dataScopes = dataScopeMapper.selectList(scopeQuery);

        if (dataScopes.isEmpty()) {
            // 无数据范围配置，默认仅本人
            return tableAlias + ".CREATED_BY_ID = '" + userId + "'";
        }

        // 3. 获取当前用户信息
        BaseUser currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            return tableAlias + ".CREATED_BY_ID = '" + userId + "'";
        }

        // 4. 对每个配置生成SQL条件片段，多用户组取OR并集
        List<String> conditions = new ArrayList<>();
        for (BaseDataScope scope : dataScopes) {
            String condition = buildScopeCondition(scope, currentUser, tableAlias);
            if (condition != null) {
                conditions.add(condition);
            }
        }

        if (conditions.isEmpty()) {
            return tableAlias + ".CREATED_BY_ID = '" + userId + "'";
        }

        // ALL类型存在时不加过滤
        if (conditions.stream().anyMatch(c -> "1=1".equals(c))) {
            return "1=1";
        }

        return "(" + String.join(" OR ", conditions) + ")";
    }

    @Override
    public List<BaseDataScope> getDataScope(String userId) {
        LambdaQueryWrapper<BaseUserGroup> userGroupQuery = new LambdaQueryWrapper<>();
        userGroupQuery.eq(BaseUserGroup::getUserId, userId);
        List<BaseUserGroup> userGroups = userGroupMapper.selectList(userGroupQuery);

        if (userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> groupIds = userGroups.stream()
                .map(BaseUserGroup::getGroupId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<BaseDataScope> scopeQuery = new LambdaQueryWrapper<>();
        scopeQuery.in(BaseDataScope::getGroupId, groupIds);
        return dataScopeMapper.selectList(scopeQuery);
    }

    private String buildScopeCondition(BaseDataScope scope, BaseUser currentUser, String tableAlias) {
        switch (scope.getScopeType()) {
            case "ALL":
                return "1=1";
            case "DEPT":
                return tableAlias + ".CREATED_BY_ORGID = '" + currentUser.getOrgId() + "'";
            case "DEPT_AND_SUB":
                // 查询本部门及下级部门ID
                List<String> orgIds = getAllSubOrgIds(currentUser.getOrgId());
                orgIds.add(currentUser.getOrgId());
                return tableAlias + ".CREATED_BY_ORGID IN ('" + String.join("','", orgIds) + "')";
            case "SELF":
                return tableAlias + ".CREATED_BY_ID = '" + currentUser.getId() + "'";
            default:
                return tableAlias + ".CREATED_BY_ID = '" + currentUser.getId() + "'";
        }
    }

    private List<String> getAllSubOrgIds(String parentOrgId) {
        List<String> result = new ArrayList<>();
        LambdaQueryWrapper<BaseOrg> query = new LambdaQueryWrapper<>();
        query.eq(BaseOrg::getParentId, parentOrgId);
        List<BaseOrg> children = orgMapper.selectList(query);
        for (BaseOrg child : children) {
            result.add(child.getId());
            result.addAll(getAllSubOrgIds(child.getId()));
        }
        return result;
    }

    private List<BaseMenu> buildTree(List<BaseMenu> allMenus) {
        List<BaseMenu> roots = new ArrayList<>();
        for (BaseMenu menu : allMenus) {
            if (menu.getParentId() == null || menu.getParentId().isEmpty()) {
                roots.add(menu);
            }
        }
        for (BaseMenu root : roots) {
            root.setChildren(findChildren(root.getId(), allMenus));
        }
        return roots;
    }

    private List<BaseMenu> findChildren(String parentId, List<BaseMenu> allMenus) {
        List<BaseMenu> children = new ArrayList<>();
        for (BaseMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(findChildren(menu.getId(), allMenus));
                children.add(menu);
            }
        }
        return children;
    }
}
