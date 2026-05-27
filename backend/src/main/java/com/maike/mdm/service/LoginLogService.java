package com.maike.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.dto.request.LoginLogQueryDTO;
import com.maike.mdm.entity.SysLoginLog;

public interface LoginLogService {

    void logLogin(SysLoginLog log);

    Page<SysLoginLog> queryLogs(LoginLogQueryDTO query);
}
