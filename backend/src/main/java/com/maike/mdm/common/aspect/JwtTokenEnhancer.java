package com.maike.mdm.common.aspect;

import com.maike.mdm.common.util.JwtUtil;
import com.maike.mdm.dto.response.LoginResponse;
import com.maike.mdm.entity.BaseRole;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.entity.BaseUserRole;
import com.maike.mdm.mapper.BaseRoleMapper;
import com.maike.mdm.mapper.BaseUserMapper;
import com.maike.mdm.mapper.BaseUserRoleMapper;
import com.maike.mdm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Token增强切面
 * 在AuthController.login方法返回后，拦截返回值，重新生成携带roles/permissions的增强Token
 * 不修改AuthController.java和AuthService.java
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class JwtTokenEnhancer {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final BaseUserMapper userMapper;
    private final BaseUserRoleMapper userRoleMapper;
    private final BaseRoleMapper roleMapper;

    @AfterReturning(
            pointcut = "execution(* com.maike.mdm.controller.AuthController.login(..))",
            returning = "result"
    )
    public void enhanceToken(JoinPoint joinPoint, Object result) {
        try {
            if (!(result instanceof ResponseEntity)) {
                return;
            }

            ResponseEntity<?> response = (ResponseEntity<?>) result;
            Object body = response.getBody();
            if (!(body instanceof com.maike.mdm.common.response.ApiResponse)) {
                return;
            }

            com.maike.mdm.common.response.ApiResponse<?> apiResponse =
                    (com.maike.mdm.common.response.ApiResponse<?>) body;
            Object data = apiResponse.getData();
            if (!(data instanceof LoginResponse)) {
                return;
            }

            LoginResponse loginResponse = (LoginResponse) data;
            String username = loginResponse.getUsername();

            // 通过用户名查询用户实体，获取用户ID
            BaseUser user = userMapper.selectByUsername(username);
            String userId = (user != null) ? user.getId() : username;

            // 查询用户角色
            List<BaseUserRole> userRoles = userRoleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseUserRole>()
                            .eq(BaseUserRole::getUserId, userId)
            );

            List<String> roleCodes = new ArrayList<>();
            if (!userRoles.isEmpty()) {
                List<String> roleIds = userRoles.stream()
                        .map(BaseUserRole::getRoleId)
                        .collect(Collectors.toList());
                List<BaseRole> roles = roleMapper.selectBatchIds(roleIds);
                roleCodes = roles.stream()
                        .map(BaseRole::getRoleCode)
                        .collect(Collectors.toList());
            }

            // 查询用户权限
            Set<String> permissions = permissionService.getUserPermissions(userId);

            // 生成增强Token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("roles", roleCodes);
            extraClaims.put("permissions", new ArrayList<>(permissions));

            String enhancedToken = jwtUtil.generateToken(username, extraClaims);
            loginResponse.setToken(enhancedToken);

            // 更新返回的roles和permissions
            loginResponse.setRoles(roleCodes);
            loginResponse.setPermissions(new ArrayList<>(permissions));

            log.debug("Token增强完成: username={}, roles={}, permissions={}", username, roleCodes, permissions);
        } catch (Exception e) {
            log.error("Token增强失败", e);
        }
    }
}
