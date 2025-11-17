package cn.iocoder.yudao.module.iot.job.energy;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportGenerateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportTemplateDO;
import cn.iocoder.yudao.module.iot.service.energy.report.IotEnergyReportService;
import cn.iocoder.yudao.module.iot.service.energy.report.IotEnergyReportTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 能源报表定时生成任务
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class EnergyReportGenerateJob {

    @Resource
    private IotEnergyReportTemplateService reportTemplateService;

    @Resource
    private IotEnergyReportService reportService;

    /**
     * 生成日报 - 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @TenantJob
    public void generateDailyReport() {
        log.info("[generateDailyReport][开始生成能源日报]");
        try {
            // 获取启用的日报模板
            List<IotEnergyReportTemplateDO> templates = reportTemplateService.getEnabledReportTemplateList();
            if (CollUtil.isEmpty(templates)) {
                log.info("[generateDailyReport][未找到启用的报表模板]");
                return;
            }

            // 昨天的时间范围
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startTime = LocalDateTime.of(yesterday, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(yesterday, LocalTime.MAX);

            for (IotEnergyReportTemplateDO template : templates) {
                if (!"day".equals(template.getReportType())) {
                    continue;
                }

                try {
                    IotEnergyReportGenerateReqVO reqVO = new IotEnergyReportGenerateReqVO();
                    reqVO.setTemplateId(template.getId());
                    reqVO.setReportName(template.getName() + "_" + yesterday);
                    reqVO.setReportType("day");
                    reqVO.setStartTime(startTime);
                    reqVO.setEndTime(endTime);
                    reqVO.setExportFormat("excel");

                    Long recordId = reportService.generateReport(reqVO);
                    log.info("[generateDailyReport][生成日报成功] templateId={}, recordId={}", template.getId(), recordId);

                } catch (Exception e) {
                    log.error("[generateDailyReport][生成日报失败] templateId={}", template.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[generateDailyReport][生成能源日报失败]", e);
        }
    }

    /**
     * 生成周报 - 每周一凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    @TenantJob
    public void generateWeeklyReport() {
        log.info("[generateWeeklyReport][开始生成能源周报]");
        try {
            // 获取启用的周报模板
            List<IotEnergyReportTemplateDO> templates = reportTemplateService.getEnabledReportTemplateList();
            if (CollUtil.isEmpty(templates)) {
                log.info("[generateWeeklyReport][未找到启用的报表模板]");
                return;
            }

            // 上周的时间范围
            LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate lastSunday = lastMonday.plusDays(6);
            LocalDateTime startTime = LocalDateTime.of(lastMonday, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(lastSunday, LocalTime.MAX);

            for (IotEnergyReportTemplateDO template : templates) {
                if (!"week".equals(template.getReportType())) {
                    continue;
                }

                try {
                    IotEnergyReportGenerateReqVO reqVO = new IotEnergyReportGenerateReqVO();
                    reqVO.setTemplateId(template.getId());
                    reqVO.setReportName(template.getName() + "_" + lastMonday + "_" + lastSunday);
                    reqVO.setReportType("week");
                    reqVO.setStartTime(startTime);
                    reqVO.setEndTime(endTime);
                    reqVO.setExportFormat("excel");

                    Long recordId = reportService.generateReport(reqVO);
                    log.info("[generateWeeklyReport][生成周报成功] templateId={}, recordId={}", template.getId(), recordId);

                } catch (Exception e) {
                    log.error("[generateWeeklyReport][生成周报失败] templateId={}", template.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[generateWeeklyReport][生成能源周报失败]", e);
        }
    }

    /**
     * 生成月报 - 每月1号凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @TenantJob
    public void generateMonthlyReport() {
        log.info("[generateMonthlyReport][开始生成能源月报]");
        try {
            // 获取启用的月报模板
            List<IotEnergyReportTemplateDO> templates = reportTemplateService.getEnabledReportTemplateList();
            if (CollUtil.isEmpty(templates)) {
                log.info("[generateMonthlyReport][未找到启用的报表模板]");
                return;
            }

            // 上月的时间范围
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            LocalDate firstDayOfLastMonth = lastMonth.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfLastMonth = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
            LocalDateTime startTime = LocalDateTime.of(firstDayOfLastMonth, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(lastDayOfLastMonth, LocalTime.MAX);

            for (IotEnergyReportTemplateDO template : templates) {
                if (!"month".equals(template.getReportType())) {
                    continue;
                }

                try {
                    IotEnergyReportGenerateReqVO reqVO = new IotEnergyReportGenerateReqVO();
                    reqVO.setTemplateId(template.getId());
                    reqVO.setReportName(template.getName() + "_" + firstDayOfLastMonth.getYear() + "-" + firstDayOfLastMonth.getMonthValue());
                    reqVO.setReportType("month");
                    reqVO.setStartTime(startTime);
                    reqVO.setEndTime(endTime);
                    reqVO.setExportFormat("excel");

                    Long recordId = reportService.generateReport(reqVO);
                    log.info("[generateMonthlyReport][生成月报成功] templateId={}, recordId={}", template.getId(), recordId);

                } catch (Exception e) {
                    log.error("[generateMonthlyReport][生成月报失败] templateId={}", template.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[generateMonthlyReport][生成能源月报失败]", e);
        }
    }

    /**
     * 生成年报 - 每年1月1号凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 1 1 ?")
    @TenantJob
    public void generateYearlyReport() {
        log.info("[generateYearlyReport][开始生成能源年报]");
        try {
            // 获取启用的年报模板
            List<IotEnergyReportTemplateDO> templates = reportTemplateService.getEnabledReportTemplateList();
            if (CollUtil.isEmpty(templates)) {
                log.info("[generateYearlyReport][未找到启用的报表模板]");
                return;
            }

            // 去年的时间范围
            int lastYear = LocalDate.now().getYear() - 1;
            LocalDateTime startTime = LocalDateTime.of(lastYear, 1, 1, 0, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(lastYear, 12, 31, 23, 59, 59);

            for (IotEnergyReportTemplateDO template : templates) {
                if (!"year".equals(template.getReportType())) {
                    continue;
                }

                try {
                    IotEnergyReportGenerateReqVO reqVO = new IotEnergyReportGenerateReqVO();
                    reqVO.setTemplateId(template.getId());
                    reqVO.setReportName(template.getName() + "_" + lastYear);
                    reqVO.setReportType("year");
                    reqVO.setStartTime(startTime);
                    reqVO.setEndTime(endTime);
                    reqVO.setExportFormat("excel");

                    Long recordId = reportService.generateReport(reqVO);
                    log.info("[generateYearlyReport][生成年报成功] templateId={}, recordId={}", template.getId(), recordId);

                } catch (Exception e) {
                    log.error("[generateYearlyReport][生成年报失败] templateId={}", template.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[generateYearlyReport][生成能源年报失败]", e);
        }
    }

}
