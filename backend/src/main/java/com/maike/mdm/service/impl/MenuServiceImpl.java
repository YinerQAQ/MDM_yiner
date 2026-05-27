package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.request.MenuSortDTO;
import com.maike.mdm.entity.BaseMenu;
import com.maike.mdm.entity.BaseRoleMenu;
import com.maike.mdm.entity.BaseUserRole;
import com.maike.mdm.mapper.BaseMenuMapper;
import com.maike.mdm.mapper.BaseRoleMenuMapper;
import com.maike.mdm.mapper.BaseUserRoleMapper;
import com.maike.mdm.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final BaseMenuMapper baseMenuMapper;
    private final BaseRoleMenuMapper baseRoleMenuMapper;
    private final BaseUserRoleMapper baseUserRoleMapper;

    @Override
    public List<BaseMenu> getMenuTree() {
        List<BaseMenu> allMenus = baseMenuMapper.selectList(
                new LambdaQueryWrapper<BaseMenu>().orderByAsc(BaseMenu::getSortOrder)
        );
        return buildTree(allMenus);
    }

    @Override
    public List<BaseMenu> getMenuTreeByUserId(String userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<BaseUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(BaseUserRole::getUserId, userId);
        List<BaseUserRole> userRoles = baseUserRoleMapper.selectList(userRoleQuery);
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roleIds = userRoles.stream().map(BaseUserRole::getRoleId).collect(Collectors.toList());

        // 查询角色菜单关联
        LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.in(BaseRoleMenu::getRoleId, roleIds);
        List<BaseRoleMenu> roleMenus = baseRoleMenuMapper.selectList(roleMenuQuery);
        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> menuIds = roleMenus.stream().map(BaseRoleMenu::getMenuId).distinct().collect(Collectors.toList());

        // 查询菜单
        LambdaQueryWrapper<BaseMenu> menuQuery = new LambdaQueryWrapper<>();
        menuQuery.in(BaseMenu::getId, menuIds).orderByAsc(BaseMenu::getSortOrder);
        List<BaseMenu> menus = baseMenuMapper.selectList(menuQuery);

        return buildTree(menus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(BaseMenu menu) {
        LambdaQueryWrapper<BaseMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseMenu::getMenuCode, menu.getMenuCode());
        if (baseMenuMapper.exists(queryWrapper)) {
            throw BusinessException.of("菜单编码已存在");
        }

        menu.setId(UUID.randomUUID().toString().replace("-", ""));
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        baseMenuMapper.insert(menu);
        log.info("创建菜单成功: {}", menu.getMenuCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(BaseMenu menu) {
        BaseMenu existing = baseMenuMapper.selectById(menu.getId());
        if (existing == null) {
            throw BusinessException.of("菜单不存在");
        }

        if (menu.getMenuCode() != null && !menu.getMenuCode().equals(existing.getMenuCode())) {
            LambdaQueryWrapper<BaseMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseMenu::getMenuCode, menu.getMenuCode())
                    .ne(BaseMenu::getId, menu.getId());
            if (baseMenuMapper.exists(queryWrapper)) {
                throw BusinessException.of("菜单编码已存在");
            }
        }

        menu.setUpdateTime(LocalDateTime.now());
        baseMenuMapper.updateById(menu);
        log.info("更新菜单成功: {}", menu.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String id) {
        BaseMenu menu = baseMenuMapper.selectById(id);
        if (menu == null) {
            throw BusinessException.of("菜单不存在");
        }

        // 级联删除子菜单
        deleteMenuCascade(id);

        // 删除角色菜单关联
        LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.eq(BaseRoleMenu::getMenuId, id);
        baseRoleMenuMapper.delete(roleMenuQuery);

        log.info("删除菜单成功: {}", id);
    }

    private void deleteMenuCascade(String parentId) {
        LambdaQueryWrapper<BaseMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseMenu::getParentId, parentId);
        List<BaseMenu> children = baseMenuMapper.selectList(queryWrapper);
        for (BaseMenu child : children) {
            deleteMenuCascade(child.getId());
            // 删除子菜单的角色关联
            LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
            roleMenuQuery.eq(BaseRoleMenu::getMenuId, child.getId());
            baseRoleMenuMapper.delete(roleMenuQuery);
        }
        baseMenuMapper.deleteById(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSort(List<MenuSortDTO> sortList) {
        for (MenuSortDTO sortDTO : sortList) {
            BaseMenu menu = baseMenuMapper.selectById(sortDTO.getId());
            if (menu != null) {
                menu.setSortOrder(sortDTO.getSortOrder());
                menu.setUpdateTime(LocalDateTime.now());
                baseMenuMapper.updateById(menu);
            }
        }
        log.info("更新菜单排序成功");
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
