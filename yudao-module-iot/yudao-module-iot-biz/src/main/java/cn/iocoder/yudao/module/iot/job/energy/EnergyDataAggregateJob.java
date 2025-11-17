package cn.iocoder.yudao.module.iot.job.energy;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能源数据聚合定时任务
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class EnergyDataAggregateJob {

    @Resource
    private IotEnergyMeterService meterService;

    @Resource
    private IotEnergyStatisticsService statisticsService;

    /**
     * 分钟级数据聚合
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    @TenantJob
    public void aggregateMinuteData() {
        try {
            log.debug("[aggregateMinuteData][开始执行分钟级数据聚合]");

            // 获取所有启用的计量点
            List<IotEnergyMeterDO> meters = meterService.getMeterListByStatus(CommonStatusEnum.ENABLE.getStatus());
            if (CollUtil.isEmpty(meters)) {
                return;
            }

            // 聚合上一分钟的数据
            LocalDateTime statTime = LocalDateTime.now().minusMinutes(1);

            for (IotEnergyMeterDO meter : meters) {
                try {
                    statisticsService.aggregateMinuteData(meter.getId(), statTime);
                } catch (Exception e) {
                    log.error("[aggregateMinuteData][聚合计量点({})分钟数据失败]", meter.getId(), e);
                }
            }

            log.debug("[aggregateMinuteData][分钟级数据聚合完成，处理{}个计量点]", meters.size());
        } catch (Exception e) {
            log.error("[aggregateMinuteData][分钟级数据聚合失败]", e);
        }
    }

    /**
     * 小时级数据聚合
     * 每小时执行一次（整点后1分钟）
     */
    @Scheduled(cron = "0 1 * * * ?")
    @TenantJob
    public void aggregateHourData() {
        try {
            log.info("[aggregateHourData][开始执行小时级数据聚合]");

            List<IotEnergyMeterDO> meters = meterService.getMeterListByStatus(CommonStatusEnum.ENABLE.getStatus());
            if (CollUtil.isEmpty(meters)) {
                return;
            }

            // 聚合上一小时的数据
            LocalDateTime statTime = LocalDateTime.now().minusHours(1);

            for (IotEnergyMeterDO meter : meters) {
                try {
                    statisticsService.aggregateHourData(meter.getId(), statTime);
                } catch (Exception e) {
                    log.error("[aggregateHourData][聚合计量点({})小时数据失败]", meter.getId(), e);
                }
            }

            log.info("[aggregateHourData][小时级数据聚合完成，处理{}个计量点]", meters.size());
        } catch (Exception e) {
            log.error("[aggregateHourData][小时级数据聚合失败]", e);
        }
    }

    /**
     * 日级数据聚合
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @TenantJob
    public void aggregateDayData() {
        try {
            log.info("[aggregateDayData][开始执行日级数据聚合]");

            List<IotEnergyMeterDO> meters = meterService.getMeterListByStatus(CommonStatusEnum.ENABLE.getStatus());
            if (CollUtil.isEmpty(meters)) {
                return;
            }

            // 聚合昨天的数据
            LocalDateTime statTime = LocalDateTime.now().minusDays(1);

            for (IotEnergyMeterDO meter : meters) {
                try {
                    statisticsService.aggregateDayData(meter.getId(), statTime);
                } catch (Exception e) {
                    log.error("[aggregateDayData][聚合计量点({})日数据失败]", meter.getId(), e);
                }
            }

            log.info("[aggregateDayData][日级数据聚合完成，处理{}个计量点]", meters.size());
        } catch (Exception e) {
            log.error("[aggregateDayData][日级数据聚合失败]", e);
        }
    }

    /**
     * 月级数据聚合
     * 每月1号凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @TenantJob
    public void aggregateMonthData() {
        try {
            log.info("[aggregateMonthData][开始执行月级数据聚合]");

            List<IotEnergyMeterDO> meters = meterService.getMeterListByStatus(CommonStatusEnum.ENABLE.getStatus());
            if (CollUtil.isEmpty(meters)) {
                return;
            }

            // 聚合上个月的数据
            LocalDateTime statTime = LocalDateTime.now().minusMonths(1);

            for (IotEnergyMeterDO meter : meters) {
                try {
                    statisticsService.aggregateMonthData(meter.getId(), statTime);
                } catch (Exception e) {
                    log.error("[aggregateMonthData][聚合计量点({})月数据失败]", meter.getId(), e);
                }
            }

            log.info("[aggregateMonthData][月级数据聚合完成，处理{}个计量点]", meters.size());
        } catch (Exception e) {
            log.error("[aggregateMonthData][月级数据聚合失败]", e);
        }
    }

}
