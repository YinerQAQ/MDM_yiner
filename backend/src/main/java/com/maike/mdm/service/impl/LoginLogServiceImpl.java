package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.LoginLogQueryDTO;
import com.maike.mdm.entity.SysLoginLog;
import com.maike.mdm.mapper.SysLoginLogMapper;
import com.maike.mdm.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final SysLoginLogMapper sysLoginLogMapper;

    @Override
    public void logLogin(SysLoginLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        sysLoginLogMapper.insert(log);
    }

    @Override
    public Page<SysLoginLog> queryLogs(LoginLogQueryDTO query) {
        Page<SysLoginLog> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysLoginLog> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null && !query.getUserId().isEmpty()) {
            queryWrapper.eq(SysLoginLog::getUserId, query.getUserId());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            queryWrapper.eq(SysLoginLog::getStatus, query.getStatus());
        }
        if (query.getStartTime() != null) {
            queryWrapper.ge(SysLoginLog::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            queryWrapper.le(SysLoginLog::getCreateTime, query.getEndTime());
        }

        queryWrapper.orderByDesc(SysLoginLog::getCreateTime);
        return sysLoginLogMapper.selectPage(page, queryWrapper);
    }
}
