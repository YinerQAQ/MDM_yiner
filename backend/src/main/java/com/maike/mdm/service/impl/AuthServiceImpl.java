package com.maike.mdm.service.impl;

import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.common.util.JwtUtil;
import com.maike.mdm.common.util.PasswordUtil;
import com.maike.mdm.dto.request.LoginRequest;
import com.maike.mdm.dto.response.LoginResponse;
import com.maike.mdm.entity.BaseOrg;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.mapper.BaseOrgMapper;
import com.maike.mdm.service.AuthService;
import com.maike.mdm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final BaseOrgMapper baseOrgMapper;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("登录请求: {}", request.getUsername());

        BaseUser user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            throw BusinessException.of("401", "用户名或密码错误");
        }

        // 支持明文密码（初始数据）和BCrypt加密密码
        boolean passwordMatch = false;
        if (passwordUtil.matches(request.getPassword(), user.getPassword())) {
            passwordMatch = true;
        } else if (request.getPassword().equals(user.getPassword())) {
            // 明文密码匹配（用于初始化数据兼容）
            passwordMatch = true;
            // 自动将明文密码升级为BCrypt加密
            user.setPassword(passwordUtil.encode(request.getPassword()));
            userService.updateUserPassword(user.getId(), user.getPassword());
        }

        if (!passwordMatch) {
            throw BusinessException.of("401", "用户名或密码错误");
        }

        if (!"启用".equals(user.getStatus())) {
            throw BusinessException.of("401", "用户已被停用，无法登录");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        // 获取用户单位名称
        String orgName = user.getOrgName();
        if (orgName == null && user.getOrgId() != null) {
            BaseOrg org = baseOrgMapper.selectById(user.getOrgId());
            if (org != null) {
                orgName = org.getOrgName();
            }
        }

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .orgId(user.getOrgId())
                .orgName(orgName != null ? orgName : "")
                .roles(List.of("admin"))
                .permissions(List.of("all"))
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("用户登出");
    }
}