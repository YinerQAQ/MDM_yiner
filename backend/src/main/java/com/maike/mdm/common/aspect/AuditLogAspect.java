package com.maike.mdm.common.aspect;

import com.maike.mdm.common.annotation.AuditLog;
import com.maike.mdm.entity.SysAuditLog;
import com.maike.mdm.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String result = "成功";
        Object returnValue = null;

        try {
            returnValue = joinPoint.proceed();
            return returnValue;
        } catch (Throwable e) {
            result = "失败: " + e.getMessage();
            throw e;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;

                // 获取当前用户
                String username = "unknown";
                String userId = null;
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    username = auth.getName();
                }

                // 获取请求信息
                String ip = null;
                String method = null;
                String params = null;
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = getClientIp(request);
                    method = request.getMethod() + " " + request.getRequestURI();
                }

                // 获取方法签名和参数
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
                if (method == null) {
                    method = methodName;
                }

                // 参数摘要
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    try {
                        String argsStr = java.util.Arrays.toString(args);
                        params = argsStr.length() > 500 ? argsStr.substring(0, 500) + "..." : argsStr;
                    } catch (Exception e) {
                        params = "参数序列化失败";
                    }
                }

                // 构建审计日志
                SysAuditLog auditLogEntity = SysAuditLog.builder()
                        .userId(userId)
                        .username(username)
                        .operation(auditLog.operation().isEmpty() ? signature.getName() : auditLog.operation())
                        .method(method)
                        .params(params)
                        .ip(ip)
                        .result(result)
                        .duration(duration)
                        .createTime(LocalDateTime.now())
                        .build();

                auditLogService.log(auditLogEntity);
            } catch (Exception e) {
                log.error("记录审计日志失败", e);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
