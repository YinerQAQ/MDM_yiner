package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.BaseDict;
import com.maike.mdm.entity.BaseDictItem;
import com.maike.mdm.mapper.BaseDictItemMapper;
import com.maike.mdm.mapper.BaseDictMapper;
import com.maike.mdm.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final BaseDictMapper baseDictMapper;
    private final BaseDictItemMapper baseDictItemMapper;

    @Override
    public List<BaseDict> listDicts() {
        return baseDictMapper.selectList(
                new LambdaQueryWrapper<BaseDict>().orderByDesc(BaseDict::getCreateTime)
        );
    }

    @Override
    public List<BaseDictItem> getItemsByDictCode(String dictCode) {
        LambdaQueryWrapper<BaseDict> dictQuery = new LambdaQueryWrapper<>();
        dictQuery.eq(BaseDict::getDictCode, dictCode);
        BaseDict dict = baseDictMapper.selectOne(dictQuery);
        if (dict == null) {
            throw BusinessException.of("字典不存在: " + dictCode);
        }

        LambdaQueryWrapper<BaseDictItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(BaseDictItem::getDictId, dict.getId())
                .orderByAsc(BaseDictItem::getSortOrder);
        return baseDictItemMapper.selectList(itemQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDict(BaseDict dict) {
        LambdaQueryWrapper<BaseDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseDict::getDictCode, dict.getDictCode());
        if (baseDictMapper.exists(queryWrapper)) {
            throw BusinessException.of("字典编码已存在");
        }

        dict.setId(UUID.randomUUID().toString().replace("-", ""));
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        baseDictMapper.insert(dict);
        log.info("创建字典成功: {}", dict.getDictCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDict(BaseDict dict) {
        BaseDict existing = baseDictMapper.selectById(dict.getId());
        if (existing == null) {
            throw BusinessException.of("字典不存在");
        }

        if (dict.getDictCode() != null && !dict.getDictCode().equals(existing.getDictCode())) {
            LambdaQueryWrapper<BaseDict> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BaseDict::getDictCode, dict.getDictCode())
                    .ne(BaseDict::getId, dict.getId());
            if (baseDictMapper.exists(queryWrapper)) {
                throw BusinessException.of("字典编码已存在");
            }
        }

        dict.setUpdateTime(LocalDateTime.now());
        baseDictMapper.updateById(dict);
        log.info("更新字典成功: {}", dict.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(String id) {
        BaseDict dict = baseDictMapper.selectById(id);
        if (dict == null) {
            throw BusinessException.of("字典不存在");
        }

        // 删除字典项
        LambdaQueryWrapper<BaseDictItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(BaseDictItem::getDictId, id);
        baseDictItemMapper.delete(itemQuery);

        baseDictMapper.deleteById(id);
        log.info("删除字典成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictItem(BaseDictItem item) {
        item.setId(UUID.randomUUID().toString().replace("-", ""));
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        // 自动填充dictCode
        if (item.getDictCode() == null && item.getDictId() != null) {
            BaseDict dict = baseDictMapper.selectById(item.getDictId());
            if (dict != null) {
                item.setDictCode(dict.getDictCode());
            }
        }

        baseDictItemMapper.insert(item);
        log.info("创建字典项成功: {}", item.getItemValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(BaseDictItem item) {
        BaseDictItem existing = baseDictItemMapper.selectById(item.getId());
        if (existing == null) {
            throw BusinessException.of("字典项不存在");
        }

        item.setUpdateTime(LocalDateTime.now());
        baseDictItemMapper.updateById(item);
        log.info("更新字典项成功: {}", item.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictItem(String id) {
        BaseDictItem item = baseDictItemMapper.selectById(id);
        if (item == null) {
            throw BusinessException.of("字典项不存在");
        }

        baseDictItemMapper.deleteById(id);
        log.info("删除字典项成功: {}", id);
    }
}
