package com.maike.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.AuditLogQueryDTO;
import com.maike.mdm.dto.request.LoginLogQueryDTO;
import com.maike.mdm.entity.SysAuditLog;
import com.maike.mdm.entity.SysLoginLog;
import com.maike.mdm.service.AuditLogService;
import com.maike.mdm.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final LoginLogService loginLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SysAuditLog>>> queryAuditLogs(AuditLogQueryDTO query) {
        Page<SysAuditLog> page = auditLogService.queryLogs(query);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/login-logs")
    public ResponseEntity<ApiResponse<Page<SysLoginLog>>> queryLoginLogs(LoginLogQueryDTO query) {
        Page<SysLoginLog> page = loginLogService.queryLogs(query);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
