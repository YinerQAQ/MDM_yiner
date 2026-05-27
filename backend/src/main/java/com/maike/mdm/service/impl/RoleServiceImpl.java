package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.BaseRole;
import com.maike.mdm.entity.BaseRoleMenu;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.entity.BaseUserRole;
import com.maike.mdm.mapper.BaseRoleMapper;
import com.maike.mdm.mapper.BaseRoleMenuMapper;
import com.maike.mdm.mapper.BaseUserMapper;
import com.maike.mdm.mapper.BaseUserRoleMapper;
import com.maike.mdm.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final BaseRoleMapper baseRoleMapper;
    private final BaseRoleMenuMapper baseRoleMenuMapper;
    private final BaseUserRoleMapper baseUserRoleMapper;
    private final BaseUserMapper baseUserMapper;

    @Override
    public List<BaseRole> listRoles() {
        return baseRoleMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(BaseRole role) {
        LambdaQueryWrapper<BaseRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseRole::getRoleCode, role.getRoleCode());
        if (baseRoleMapper.exists(queryWrapper)) {
            throw BusinessException.of("角色编码已存在");
        }

        role.setId(UUID.randomUUID().toString().replace("-", ""));
        baseRoleMapper.insert(role);
        log.info("创建角色成功: {}", role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(BaseRole role) {
        BaseRole existing = baseRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw BusinessException.of("角色不存在");
        }

        if (role.getRoleCode() != null && !role.getRoleCode().equals(existing.getRoleCode())) {
            LambdaQueryWrapper<BaseRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseRole::getRoleCode, role.getRoleCode())
                    .ne(BaseRole::getId, role.getId());
            if (baseRoleMapper.exists(queryWrapper)) {
                throw BusinessException.of("角色编码已存在");
            }
        }

        baseRoleMapper.updateById(role);
        log.info("更新角色成功: {}", role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        BaseRole role = baseRoleMapper.selectById(id);
        if (role == null) {
            throw BusinessException.of("角色不存在");
        }

        // 删除角色菜单关联
        LambdaQueryWrapper<BaseRoleMenu> roleMenuQuery = new LambdaQueryWrapper<>();
        roleMenuQuery.eq(BaseRoleMenu::getRoleId, id);
        baseRoleMenuMapper.delete(roleMenuQuery);

        // 删除用户角色关联
        LambdaQueryWrapper<BaseUserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(BaseUserRole::getRoleId, id);
        baseUserRoleMapper.delete(userRoleQuery);

        baseRoleMapper.deleteById(id);
        log.info("删除角色成功: {}", id);
    }

    @Override
    public List<String> getRoleMenuIds(String roleId) {
        LambdaQueryWrapper<BaseRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseRoleMenu::getRoleId, roleId);
        List<BaseRoleMenu> roleMenus = baseRoleMenuMapper.selectList(queryWrapper);
        return roleMenus.stream().map(BaseRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(String roleId, List<String> menuIds) {
        BaseRole role = baseRoleMapper.selectById(roleId);
        if (role == null) {
            throw BusinessException.of("角色不存在");
        }

        // 先删除旧关联
        LambdaQueryWrapper<BaseRoleMenu> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(BaseRoleMenu::getRoleId, roleId);
        baseRoleMenuMapper.delete(deleteQuery);

        // 批量插入新关联
        for (String menuId : menuIds) {
            BaseRoleMenu roleMenu = BaseRoleMenu.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .roleId(roleId)
                    .menuId(menuId)
                    .createTime(LocalDateTime.now())
                    .build();
            baseRoleMenuMapper.insert(roleMenu);
        }
        log.info("分配角色菜单权限成功: roleId={}, menuCount={}", roleId, menuIds.size());
    }

    @Override
    public List<BaseUser> getRoleUsers(String roleId) {
        LambdaQueryWrapper<BaseUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseUserRole::getRoleId, roleId);
        List<BaseUserRole> userRoles = baseUserRoleMapper.selectList(queryWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> userIds = userRoles.stream().map(BaseUserRole::getUserId).collect(Collectors.toList());
        LambdaQueryWrapper<BaseUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(BaseUser::getId, userIds);
        return baseUserMapper.selectList(userQuery);
    }
}
