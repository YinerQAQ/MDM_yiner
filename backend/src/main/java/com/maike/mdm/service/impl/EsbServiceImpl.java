package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maike.mdm.common.exception.BusinessException;
import com.maike.mdm.entity.*;
import com.maike.mdm.mapper.*;
import com.maike.mdm.service.EsbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsbServiceImpl implements EsbService {

    private final MdmEsbModelDistMapper mdmEsbModelDistMapper;
    private final MdmEsbModelDistContentMapper mdmEsbModelDistContentMapper;
    private final MdmEsbModelDistDataMapper mdmEsbModelDistDataMapper;
    private final MdmEsbModelDistRecordMapper mdmEsbModelDistRecordMapper;
    private final MdmEsbInfoSystemMapper mdmEsbInfoSystemMapper;
    private final MdmMainDataMapper mdmMainDataMapper;
    private final MdmDataModelMapper mdmDataModelMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== 分发接口管理 ====================

    @Override
    public List<MdmEsbModelDist> listDistByModelId(String modelId) {
        LambdaQueryWrapper<MdmEsbModelDist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDist::getModelId, modelId)
                .orderByDesc(MdmEsbModelDist::getCreateTime);
        return mdmEsbModelDistMapper.selectList(queryWrapper);
    }

    @Override
    public MdmEsbModelDist getDistById(String id) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(id);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }
        return dist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDist(MdmEsbModelDist dist) {
        dist.setId(UUID.randomUUID().toString().replace("-", ""));
        dist.setStatus("停用");
        dist.setCreateTime(LocalDateTime.now());
        dist.setUpdateTime(LocalDateTime.now());
        mdmEsbModelDistMapper.insert(dist);
        log.info("创建分发接口: {}", dist.getDistName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDist(MdmEsbModelDist dist) {
        MdmEsbModelDist existing = mdmEsbModelDistMapper.selectById(dist.getId());
        if (existing == null) {
            throw BusinessException.of("分发接口不存在");
        }

        if (dist.getDistName() != null) {
            existing.setDistName(dist.getDistName());
        }
        if (dist.getDistCode() != null) {
            existing.setDistCode(dist.getDistCode());
        }
        if (dist.getInterfaceType() != null) {
            existing.setInterfaceType(dist.getInterfaceType());
        }
        if (dist.getSyncType() != null) {
            existing.setSyncType(dist.getSyncType());
        }
        if (dist.getDataFormat() != null) {
            existing.setDataFormat(dist.getDataFormat());
        }
        if (dist.getCronExpr() != null) {
            existing.setCronExpr(dist.getCronExpr());
        }
        if (dist.getBatchSize() != null) {
            existing.setBatchSize(dist.getBatchSize());
        }
        if (dist.getRetryCount() != null) {
            existing.setRetryCount(dist.getRetryCount());
        }
        if (dist.getRetryInterval() != null) {
            existing.setRetryInterval(dist.getRetryInterval());
        }
        if (dist.getTimeout() != null) {
            existing.setTimeout(dist.getTimeout());
        }
        if (dist.getTargetSystemId() != null) {
            existing.setTargetSystemId(dist.getTargetSystemId());
        }
        if (dist.getTargetUrl() != null) {
            existing.setTargetUrl(dist.getTargetUrl());
        }

        existing.setUpdateTime(LocalDateTime.now());
        mdmEsbModelDistMapper.updateById(existing);
        log.info("更新分发接口: {}", dist.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDist(String id) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(id);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }

        mdmEsbModelDistMapper.deleteById(id);

        // 删除关联的分发内容
        LambdaQueryWrapper<MdmEsbModelDistContent> contentDeleteWrapper = new LambdaQueryWrapper<>();
        contentDeleteWrapper.eq(MdmEsbModelDistContent::getDistId, id);
        mdmEsbModelDistContentMapper.delete(contentDeleteWrapper);

        log.info("删除分发接口: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableDist(String id) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(id);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }

        dist.setStatus("启用");
        dist.setUpdateTime(LocalDateTime.now());
        mdmEsbModelDistMapper.updateById(dist);
        log.info("启用分发接口: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableDist(String id) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(id);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }

        dist.setStatus("停用");
        dist.setUpdateTime(LocalDateTime.now());
        mdmEsbModelDistMapper.updateById(dist);
        log.info("停用分发接口: {}", id);
    }

    // ==================== 分发内容配置 ====================

    @Override
    public List<MdmEsbModelDistContent> getDistContent(String distId) {
        LambdaQueryWrapper<MdmEsbModelDistContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDistContent::getDistId, distId)
                .orderByAsc(MdmEsbModelDistContent::getSortOrder);
        return mdmEsbModelDistContentMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDistContent(String distId, List<MdmEsbModelDistContent> contents) {
        // 删除旧内容
        LambdaQueryWrapper<MdmEsbModelDistContent> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(MdmEsbModelDistContent::getDistId, distId);
        mdmEsbModelDistContentMapper.delete(deleteWrapper);

        // 保存新内容
        if (contents != null) {
            for (MdmEsbModelDistContent content : contents) {
                content.setId(UUID.randomUUID().toString().replace("-", ""));
                content.setDistId(distId);
                mdmEsbModelDistContentMapper.insert(content);
            }
        }

        log.info("保存分发内容: distId={}, 内容数={}", distId, contents != null ? contents.size() : 0);
    }

    // ==================== 分发执行 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeDistribute(String distId, List<String> dataIds) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(distId);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }

        if (!"启用".equals(dist.getStatus())) {
            throw BusinessException.of("分发接口未启用");
        }

        // 获取分发内容配置
        List<MdmEsbModelDistContent> contents = getDistContent(distId);
        if (contents.isEmpty()) {
            throw BusinessException.of("未配置分发内容");
        }

        String batchId = UUID.randomUUID().toString().replace("-", "");

        for (String dataId : dataIds) {
            long startTime = System.currentTimeMillis();
            MdmEsbModelDistRecord record = MdmEsbModelDistRecord.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .distId(distId)
                    .dataId(dataId)
                    .batchId(batchId)
                    .createTime(LocalDateTime.now())
                    .build();

            try {
                // 获取主数据
                MdmMainData mainData = mdmMainDataMapper.selectById(dataId);
                if (mainData == null) {
                    throw BusinessException.of("主数据不存在: " + dataId);
                }

                // 根据分发内容配置构建报文
                Map<String, Object> payload = buildPayload(mainData, contents);
                String jsonPayload = toJsonString(payload);
                record.setRequestBody(jsonPayload);

                // 调用目标URL
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

                int timeoutMs = dist.getTimeout() != null ? dist.getTimeout() * 1000 : 30000;
                ResponseEntity<String> response = restTemplate.exchange(
                        dist.getTargetUrl(), HttpMethod.POST, entity, String.class);

                long duration = System.currentTimeMillis() - startTime;
                record.setDuration(duration);
                record.setResponseBody(response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    record.setStatus("成功");
                    // 更新待分发数据状态
                    updateDistDataStatus(distId, dataId, "已同步", null);
                } else {
                    record.setStatus("失败");
                    record.setErrorMsg("HTTP状态码: " + response.getStatusCode());
                    handleDistributeFailure(distId, dataId, dist, "HTTP状态码: " + response.getStatusCode());
                }

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                record.setDuration(duration);
                record.setStatus("失败");
                record.setErrorMsg(e.getMessage());
                handleDistributeFailure(distId, dataId, dist, e.getMessage());
                log.error("分发执行失败: distId={}, dataId={}", distId, dataId, e);
            }

            mdmEsbModelDistRecordMapper.insert(record);
        }

        log.info("分发执行完成: distId={}, batchId={}, 数据数={}", distId, batchId, dataIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToDistQueue(String distId, String dataId) {
        MdmEsbModelDist dist = mdmEsbModelDistMapper.selectById(distId);
        if (dist == null) {
            throw BusinessException.of("分发接口不存在");
        }

        MdmMainData mainData = mdmMainDataMapper.selectById(dataId);
        if (mainData == null) {
            throw BusinessException.of("主数据不存在");
        }

        // 获取模型编码
        String modelCode = null;
        if (mainData.getModelId() != null) {
            MdmDataModel model = mdmDataModelMapper.selectById(mainData.getModelId());
            if (model != null) {
                modelCode = model.getModelCode();
            }
        }

        MdmEsbModelDistData distData = MdmEsbModelDistData.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .distId(distId)
                .dataId(dataId)
                .modelCode(modelCode)
                .dataCode(mainData.getCode())
                .syncStatus("待同步")
                .retryCount(0)
                .createTime(LocalDateTime.now())
                .build();

        mdmEsbModelDistDataMapper.insert(distData);
        log.info("加入待分发队列: distId={}, dataId={}", distId, dataId);
    }

    // ==================== 监控 ====================

    @Override
    public Page<MdmEsbModelDistData> getDistData(String distId, String status, Page<MdmEsbModelDistData> page) {
        LambdaQueryWrapper<MdmEsbModelDistData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDistData::getDistId, distId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(MdmEsbModelDistData::getSyncStatus, status);
        }
        queryWrapper.orderByDesc(MdmEsbModelDistData::getCreateTime);
        return mdmEsbModelDistDataMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<MdmEsbModelDistRecord> getDistRecords(String distId, Page<MdmEsbModelDistRecord> page) {
        LambdaQueryWrapper<MdmEsbModelDistRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDistRecord::getDistId, distId)
                .orderByDesc(MdmEsbModelDistRecord::getCreateTime);
        return mdmEsbModelDistRecordMapper.selectPage(page, queryWrapper);
    }

    // ==================== 信息系统管理 ====================

    @Override
    public List<MdmEsbInfoSystem> listSystems() {
        LambdaQueryWrapper<MdmEsbInfoSystem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(MdmEsbInfoSystem::getCreateTime);
        return mdmEsbInfoSystemMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSystem(MdmEsbInfoSystem system) {
        system.setId(UUID.randomUUID().toString().replace("-", ""));
        system.setStatus("启用");
        system.setCreateTime(LocalDateTime.now());
        system.setUpdateTime(LocalDateTime.now());
        mdmEsbInfoSystemMapper.insert(system);
        log.info("创建信息系统: {}", system.getSystemName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSystem(MdmEsbInfoSystem system) {
        MdmEsbInfoSystem existing = mdmEsbInfoSystemMapper.selectById(system.getId());
        if (existing == null) {
            throw BusinessException.of("信息系统不存在");
        }

        if (system.getSystemCode() != null) {
            existing.setSystemCode(system.getSystemCode());
        }
        if (system.getSystemName() != null) {
            existing.setSystemName(system.getSystemName());
        }
        if (system.getSystemUrl() != null) {
            existing.setSystemUrl(system.getSystemUrl());
        }
        if (system.getAuthType() != null) {
            existing.setAuthType(system.getAuthType());
        }
        if (system.getAuthConfig() != null) {
            existing.setAuthConfig(system.getAuthConfig());
        }
        if (system.getContactPerson() != null) {
            existing.setContactPerson(system.getContactPerson());
        }
        if (system.getContactPhone() != null) {
            existing.setContactPhone(system.getContactPhone());
        }
        if (system.getStatus() != null) {
            existing.setStatus(system.getStatus());
        }
        if (system.getRemark() != null) {
            existing.setRemark(system.getRemark());
        }

        existing.setUpdateTime(LocalDateTime.now());
        mdmEsbInfoSystemMapper.updateById(existing);
        log.info("更新信息系统: {}", system.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSystem(String id) {
        MdmEsbInfoSystem system = mdmEsbInfoSystemMapper.selectById(id);
        if (system == null) {
            throw BusinessException.of("信息系统不存在");
        }

        mdmEsbInfoSystemMapper.deleteById(id);
        log.info("删除信息系统: {}", id);
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> buildPayload(MdmMainData mainData, List<MdmEsbModelDistContent> contents) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dataId", mainData.getId());
        payload.put("code", mainData.getCode());

        // 如果有JSON数据，解析并提取配置的字段
        String jsonData = mainData.getJsonData();
        Map<String, Object> jsonDataMap = new HashMap<>();
        if (jsonData != null && !jsonData.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                jsonDataMap = objectMapper.readValue(jsonData, Map.class);
            } catch (Exception e) {
                log.warn("解析JSON数据失败: {}", e.getMessage());
            }
        }

        for (MdmEsbModelDistContent content : contents) {
            if (!"Y".equals(content.getIncludeFlag())) {
                continue;
            }
            String fieldKey = content.getFieldMapping() != null ? content.getFieldMapping() : content.getAttributeCode();
            Object value = jsonDataMap.get(content.getAttributeCode());
            payload.put(fieldKey, value);
        }

        return payload;
    }

    private String toJsonString(Map<String, Object> map) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }

    private void updateDistDataStatus(String distId, String dataId, String status, String errorMsg) {
        LambdaQueryWrapper<MdmEsbModelDistData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDistData::getDistId, distId)
                .eq(MdmEsbModelDistData::getDataId, dataId);
        MdmEsbModelDistData distData = mdmEsbModelDistDataMapper.selectOne(queryWrapper);
        if (distData != null) {
            distData.setSyncStatus(status);
            distData.setLastSyncTime(LocalDateTime.now());
            if (errorMsg != null) {
                distData.setErrorMsg(errorMsg);
            }
            mdmEsbModelDistDataMapper.updateById(distData);
        }
    }

    private void handleDistributeFailure(String distId, String dataId, MdmEsbModelDist dist, String errorMsg) {
        LambdaQueryWrapper<MdmEsbModelDistData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MdmEsbModelDistData::getDistId, distId)
                .eq(MdmEsbModelDistData::getDataId, dataId);
        MdmEsbModelDistData distData = mdmEsbModelDistDataMapper.selectOne(queryWrapper);
        if (distData != null) {
            int currentRetry = distData.getRetryCount() != null ? distData.getRetryCount() : 0;
            int maxRetry = dist.getRetryCount() != null ? dist.getRetryCount() : 3;

            distData.setRetryCount(currentRetry + 1);
            distData.setErrorMsg(errorMsg);

            if (currentRetry + 1 < maxRetry) {
                // 未超过最大重试次数，保持待同步状态
                distData.setSyncStatus("待同步");
            } else {
                distData.setSyncStatus("失败");
            }

            distData.setLastSyncTime(LocalDateTime.now());
            mdmEsbModelDistDataMapper.updateById(distData);
        }
    }
}
