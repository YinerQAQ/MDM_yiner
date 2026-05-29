package com.maike.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maike.mdm.entity.MdmEsbModelDist;
import com.maike.mdm.mapper.MdmEsbModelDistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ESB定时分发调度服务
 * 对应需求: FR-ESB-002
 * 每分钟检查配置了cron表达式且到达执行时间的分发任务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EsbScheduleService {

    private final EsbServiceImpl esbService;
    private final MdmEsbModelDistMapper distMapper;

    /**
     * 每分钟检查是否有定时分发任务到达执行时间
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledDists() {
        try {
            // 查询所有配置了cron且为启用状态的分发任务
            List<MdmEsbModelDist> scheduledDists = distMapper.selectList(
                new QueryWrapper<MdmEsbModelDist>()
                    .isNotNull("CRON_EXPR")
                    .ne("CRON_EXPR", "")
                    .eq("STATUS", "启用")
            );

            LocalDateTime now = LocalDateTime.now();
            for (MdmEsbModelDist dist : scheduledDists) {
                try {
                    if (shouldExecuteNow(dist.getCronExpr(), now)) {
                        log.info("定时分发任务触发: distId={}, distName={}, cron={}",
                            dist.getId(), dist.getDistName(), dist.getCronExpr());
                        esbService.executeDistWithRetry(dist.getId());
                    }
                } catch (Exception e) {
                    log.error("定时分发任务执行异常: distId={}", dist.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("定时分发调度异常", e);
        }
    }

    /**
     * 判断cron表达式是否应在当前时间执行
     * 使用Spring的CronExpression解析，判断当前分钟是否匹配cron
     */
    private boolean shouldExecuteNow(String cronExpr, LocalDateTime now) {
        try {
            org.springframework.scheduling.support.CronExpression cron =
                org.springframework.scheduling.support.CronExpression.parse(cronExpr);
            if (cron == null) return false;

            // 获取当前时间之后下一次执行时间
            LocalDateTime nextExec = cron.next(now);
            if (nextExec == null) return false;

            // 如果下一次执行时间在当前分钟内(距现在不超过60秒)，则应执行
            long secondsUntilNext = java.time.Duration.between(now, nextExec).getSeconds();
            return secondsUntilNext >= 0 && secondsUntilNext < 60;
        } catch (Exception e) {
            log.warn("Cron表达式解析失败: {}", cronExpr, e);
            return false;
        }
    }
}
