package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.BaseOrg;
import com.maike.mdm.mapper.BaseOrgMapper;
import com.maike.mdm.service.OrgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final BaseOrgMapper baseOrgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseOrg createOrg(BaseOrg org) {
        LambdaQueryWrapper<BaseOrg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseOrg::getId, org.getId());
        if (baseOrgMapper.exists(queryWrapper)) {
            throw BusinessException.of("单位编码已存在");
        }

        org.setStatus("启用");
        baseOrgMapper.insert(org);
        log.info("创建单位成功: {}", org.getOrgName());
        return org;
    }

    @Override
    public BaseOrg getOrgById(String id) {
        return baseOrgMapper.selectById(id);
    }

    @Override
    public List<BaseOrg> getAllOrgs() {
        return baseOrgMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseOrg updateOrg(String id, BaseOrg org) {
        BaseOrg existingOrg = baseOrgMapper.selectById(id);
        if (existingOrg == null) {
            throw BusinessException.of("单位不存在");
        }

        if (org.getOrgName() != null) {
            existingOrg.setOrgName(org.getOrgName());
        }
        if (org.getParentId() != null) {
            existingOrg.setParentId(org.getParentId());
        }

        baseOrgMapper.updateById(existingOrg);
        log.info("更新单位成功: {}", id);
        return existingOrg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrg(String id) {
        BaseOrg org = baseOrgMapper.selectById(id);
        if (org == null) {
            throw BusinessException.of("单位不存在");
        }

        LambdaQueryWrapper<BaseOrg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseOrg::getParentId, id);
        if (baseOrgMapper.exists(queryWrapper)) {
            throw BusinessException.of("存在下级单位，无法删除");
        }

        baseOrgMapper.deleteById(id);
        log.info("删除单位成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOrgStatus(String id, String status) {
        BaseOrg org = baseOrgMapper.selectById(id);
        if (org == null) {
            throw BusinessException.of("单位不存在");
        }
        org.setStatus(status);
        baseOrgMapper.updateById(org);
        log.info("修改单位状态: {} -> {}", id, status);
    }

    @Override
    public List<BaseOrg> getOrgTree(String parentId) {
        LambdaQueryWrapper<BaseOrg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseOrg::getParentId, parentId);
        return baseOrgMapper.selectList(queryWrapper);
    }
}