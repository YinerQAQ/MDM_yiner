package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.SysParam;
import com.maike.mdm.mapper.SysParamMapper;
import com.maike.mdm.service.SysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysParamServiceImpl implements SysParamService {

    private final SysParamMapper sysParamMapper;

    @Override
    public List<SysParam> listParams() {
        return sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParam>().orderByAsc(SysParam::getCreateTime)
        );
    }

    @Override
    public String getParamValue(String key) {
        LambdaQueryWrapper<SysParam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysParam::getParamKey, key);
        SysParam param = sysParamMapper.selectOne(queryWrapper);
        return param != null ? param.getParamValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateParam(SysParam param) {
        SysParam existing = sysParamMapper.selectById(param.getId());
        if (existing == null) {
            throw BusinessException.of("参数不存在");
        }

        param.setUpdateTime(LocalDateTime.now());
        sysParamMapper.updateById(param);
        log.info("更新系统参数成功: {}", param.getId());
    }
}
