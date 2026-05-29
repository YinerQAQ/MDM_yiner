package com.maike.mdm.common.aspect;

import com.maike.mdm.common.annotation.DataScope;
import com.maike.mdm.common.context.DataScopeContext;
import com.maike.mdm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 数据范围AOP切面
 * 拦截带@DataScope注解的方法，根据当前用户角色的数据范围配置，注入SQL WHERE条件
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final PermissionService permissionService;

    @Before("@annotation(dataScope)")
    public void doDataScope(JoinPoint joinPoint, DataScope dataScope) {
        // 1. 获取当前登录用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            DataScopeContext.setScopeSql("1=0"); // 未登录不可见任何数据
            return;
        }

        String userId = auth.getName();

        // 2. 获取数据范围SQL
        String scopeSql = permissionService.getDataScopeSql(userId, dataScope.alias());

        // 3. 将SQL条件注入到ThreadLocal中
        DataScopeContext.setScopeSql(scopeSql);
        log.debug("数据范围SQL注入: alias={}, sql={}", dataScope.alias(), scopeSql);
    }

    @After("@annotation(dataScope)")
    public void clearDataScope(JoinPoint joinPoint, DataScope dataScope) {
        DataScopeContext.clear();
    }
}
