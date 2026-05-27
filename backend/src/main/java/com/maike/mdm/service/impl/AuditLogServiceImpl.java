package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.AuditLogQueryDTO;
import com.maike.mdm.entity.SysAuditLog;
import com.maike.mdm.mapper.SysAuditLogMapper;
import com.maike.mdm.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Override
    public void log(SysAuditLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        sysAuditLogMapper.insert(log);
    }

    @Override
    public Page<SysAuditLog> queryLogs(AuditLogQueryDTO query) {
        Page<SysAuditLog> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysAuditLog> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null && !query.getUserId().isEmpty()) {
            queryWrapper.eq(SysAuditLog::getUserId, query.getUserId());
        }
        if (query.getOperation() != null && !query.getOperation().isEmpty()) {
            queryWrapper.like(SysAuditLog::getOperation, query.getOperation());
        }
        if (query.getStartTime() != null) {
            queryWrapper.ge(SysAuditLog::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            queryWrapper.le(SysAuditLog::getCreateTime, query.getEndTime());
        }

        queryWrapper.orderByDesc(SysAuditLog::getCreateTime);
        return sysAuditLogMapper.selectPage(page, queryWrapper);
    }
}
