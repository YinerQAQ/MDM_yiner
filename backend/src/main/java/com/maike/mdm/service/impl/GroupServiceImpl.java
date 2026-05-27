package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.dto.request.OrgPermDTO;
import com.maike.mdm.entity.BaseGroup;
import com.maike.mdm.entity.BaseGroupOrg;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.entity.BaseUserGroup;
import com.maike.mdm.mapper.BaseGroupMapper;
import com.maike.mdm.mapper.BaseGroupOrgMapper;
import com.maike.mdm.mapper.BaseUserGroupMapper;
import com.maike.mdm.mapper.BaseUserMapper;
import com.maike.mdm.service.GroupService;
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
public class GroupServiceImpl implements GroupService {

    private final BaseGroupMapper baseGroupMapper;
    private final BaseGroupOrgMapper baseGroupOrgMapper;
    private final BaseUserGroupMapper baseUserGroupMapper;
    private final BaseUserMapper baseUserMapper;

    @Override
    public List<BaseGroup> listGroups() {
        return baseGroupMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGroup(BaseGroup group) {
        LambdaQueryWrapper<BaseGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseGroup::getGroupCode, group.getGroupCode());
        if (baseGroupMapper.exists(queryWrapper)) {
            throw BusinessException.of("用户组编码已存在");
        }

        group.setId(UUID.randomUUID().toString().replace("-", ""));
        baseGroupMapper.insert(group);
        log.info("创建用户组成功: {}", group.getGroupCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(BaseGroup group) {
        BaseGroup existing = baseGroupMapper.selectById(group.getId());
        if (existing == null) {
            throw BusinessException.of("用户组不存在");
        }

        if (group.getGroupCode() != null && !group.getGroupCode().equals(existing.getGroupCode())) {
            LambdaQueryWrapper<BaseGroup> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseGroup::getGroupCode, group.getGroupCode())
                    .ne(BaseGroup::getId, group.getId());
            if (baseGroupMapper.exists(queryWrapper)) {
                throw BusinessException.of("用户组编码已存在");
            }
        }

        baseGroupMapper.updateById(group);
        log.info("更新用户组成功: {}", group.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String id) {
        BaseGroup group = baseGroupMapper.selectById(id);
        if (group == null) {
            throw BusinessException.of("用户组不存在");
        }

        // 删除组单位关联
        LambdaQueryWrapper<BaseGroupOrg> groupOrgQuery = new LambdaQueryWrapper<>();
        groupOrgQuery.eq(BaseGroupOrg::getGroupId, id);
        baseGroupOrgMapper.delete(groupOrgQuery);

        // 删除用户组关联
        LambdaQueryWrapper<BaseUserGroup> userGroupQuery = new LambdaQueryWrapper<>();
        userGroupQuery.eq(BaseUserGroup::getGroupId, id);
        baseUserGroupMapper.delete(userGroupQuery);

        baseGroupMapper.deleteById(id);
        log.info("删除用户组成功: {}", id);
    }

    @Override
    public List<String> getGroupOrgIds(String groupId) {
        LambdaQueryWrapper<BaseGroupOrg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseGroupOrg::getGroupId, groupId);
        List<BaseGroupOrg> groupOrgs = baseGroupOrgMapper.selectList(queryWrapper);
        return groupOrgs.stream().map(BaseGroupOrg::getOrgId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignOrgs(String groupId, List<OrgPermDTO> orgPerms) {
        BaseGroup group = baseGroupMapper.selectById(groupId);
        if (group == null) {
            throw BusinessException.of("用户组不存在");
        }

        // 先删除旧关联
        LambdaQueryWrapper<BaseGroupOrg> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(BaseGroupOrg::getGroupId, groupId);
        baseGroupOrgMapper.delete(deleteQuery);

        // 批量插入新关联
        for (OrgPermDTO orgPerm : orgPerms) {
            BaseGroupOrg groupOrg = BaseGroupOrg.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .groupId(groupId)
                    .orgId(orgPerm.getOrgId())
                    .cascadeFlag(orgPerm.getCascadeFlag())
                    .createTime(LocalDateTime.now())
                    .build();
            baseGroupOrgMapper.insert(groupOrg);
        }
        log.info("分配用户组单位权限成功: groupId={}, orgCount={}", groupId, orgPerms.size());
    }

    @Override
    public List<BaseUser> getGroupUsers(String groupId) {
        LambdaQueryWrapper<BaseUserGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseUserGroup::getGroupId, groupId);
        List<BaseUserGroup> userGroups = baseUserGroupMapper.selectList(queryWrapper);

        if (userGroups.isEmpty()) {
            return List.of();
        }

        List<String> userIds = userGroups.stream().map(BaseUserGroup::getUserId).collect(Collectors.toList());
        LambdaQueryWrapper<BaseUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(BaseUser::getId, userIds);
        return baseUserMapper.selectList(userQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUsersToGroup(String groupId, List<String> userIds) {
        BaseGroup group = baseGroupMapper.selectById(groupId);
        if (group == null) {
            throw BusinessException.of("用户组不存在");
        }

        for (String userId : userIds) {
            // 检查是否已存在关联
            LambdaQueryWrapper<BaseUserGroup> existsQuery = new LambdaQueryWrapper<>();
            existsQuery.eq(BaseUserGroup::getGroupId, groupId)
                    .eq(BaseUserGroup::getUserId, userId);
            if (baseUserGroupMapper.exists(existsQuery)) {
                continue;
            }

            BaseUserGroup userGroup = BaseUserGroup.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .userId(userId)
                    .groupId(groupId)
                    .createTime(LocalDateTime.now())
                    .build();
            baseUserGroupMapper.insert(userGroup);
        }
        log.info("添加用户到用户组成功: groupId={}, userCount={}", groupId, userIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserFromGroup(String groupId, String userId) {
        LambdaQueryWrapper<BaseUserGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseUserGroup::getGroupId, groupId)
                .eq(BaseUserGroup::getUserId, userId);
        baseUserGroupMapper.delete(queryWrapper);
        log.info("从用户组移除用户成功: groupId={}, userId={}", groupId, userId);
    }
}
