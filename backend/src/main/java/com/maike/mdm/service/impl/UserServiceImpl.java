package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.common.util.PasswordUtil;
import com.maike.mdm.dto.request.UserCreateRequest;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.mapper.BaseUserMapper;
import com.maike.mdm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final BaseUserMapper baseUserMapper;
    private final PasswordUtil passwordUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseUser createUser(UserCreateRequest request) {
        LambdaQueryWrapper<BaseUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseUser::getId, request.getId());
        if (baseUserMapper.exists(queryWrapper)) {
            throw BusinessException.of("用户编码已存在");
        }

        queryWrapper.clear();
        queryWrapper.eq(BaseUser::getUsername, request.getUsername());
        if (baseUserMapper.exists(queryWrapper)) {
            throw BusinessException.of("用户名已存在");
        }

        BaseUser user = BaseUser.builder()
                .id(request.getId())
                .username(request.getUsername())
                .password(passwordUtil.encode(request.getPassword() != null ? request.getPassword() : "123456"))
                .nickname(request.getNickname())
                .sex(request.getSex())
                .orgId(request.getOrgId())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("启用")
                .securityLevel(request.getSecurityLevel())
                .build();

        baseUserMapper.insert(user);
        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }

    @Override
    public BaseUser getUserById(String id) {
        return baseUserMapper.selectById(id);
    }

    @Override
    public BaseUser getUserByUsername(String username) {
        LambdaQueryWrapper<BaseUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseUser::getUsername, username);
        return baseUserMapper.selectOne(queryWrapper);
    }

    @Override
    public List<BaseUser> getAllUsers() {
        return baseUserMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseUser updateUser(String id, UserCreateRequest request) {
        BaseUser user = baseUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }

        if (request.getUsername() != null) {
            LambdaQueryWrapper<BaseUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseUser::getUsername, request.getUsername())
                    .ne(BaseUser::getId, id);
            if (baseUserMapper.exists(queryWrapper)) {
                throw BusinessException.of("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getOrgId() != null) {
            user.setOrgId(request.getOrgId());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getSecurityLevel() != null) {
            user.setSecurityLevel(request.getSecurityLevel());
        }

        baseUserMapper.updateById(user);
        log.info("更新用户成功: {}", user.getId());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        BaseUser user = baseUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        baseUserMapper.deleteById(id);
        log.info("删除用户成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserStatus(String id, String status) {
        BaseUser user = baseUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        user.setStatus(status);
        baseUserMapper.updateById(user);
        log.info("修改用户状态: {} -> {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String id, String newPassword) {
        BaseUser user = baseUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        user.setPassword(passwordUtil.encode(newPassword));
        baseUserMapper.updateById(user);
        log.info("重置用户密码: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(String id, String encodedPassword) {
        BaseUser user = baseUserMapper.selectById(id);
        if (user != null) {
            user.setPassword(encodedPassword);
            baseUserMapper.updateById(user);
            log.info("更新用户密码为加密格式: {}", id);
        }
    }
}