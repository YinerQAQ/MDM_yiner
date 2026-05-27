package com.maike.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.AuditLogQueryDTO;
import com.maike.mdm.entity.SysAuditLog;

public interface AuditLogService {

    void log(SysAuditLog log);

    Page<SysAuditLog> queryLogs(AuditLogQueryDTO query);
}
