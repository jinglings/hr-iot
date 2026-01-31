package cn.iocoder.yudao.module.iot.job.backup;

import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotBackupConfigMapper;
import cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum;
import cn.iocoder.yudao.module.iot.service.backup.IotBackupRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TDengine 备份任务调度初始化器
 *
 * 功能：
 * 1. 应用启动时加载所有启用的备份配置
 * 2. 定时检查并执行备份任务
 *
 * 注意：由于使用了 @Scheduled 注解，无需手动操作 Quartz
 *
 * @author claude
 */
@Component
@Slf4j
public class BackupScheduleInitializer implements ApplicationRunner {

    @Resource
    private IotBackupConfigMapper backupConfigMapper;

    @Resource
    private IotBackupRecordService backupRecordService;

    /**
     * 记录每个配置的最后执行时间
     * key: configId, value: 最后执行时间戳
     */
    private final Map<Long, Long> lastExecutionTimes = new ConcurrentHashMap<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[BackupScheduleInitializer][开始初始化备份任务调度器]");

        // 加载所有启用的备份配置
        List<IotBackupConfigDO> configs = backupConfigMapper.selectEnabledList();

        log.info("[BackupScheduleInitializer][初始化完成] enabledConfigs={}", configs.size());
    }

    /**
     * 定时检查并执行备份任务
     * 每分钟执行一次，检查是否有需要执行的备份任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteBackups() {
        try {
            // 查询所有启用的备份配置
            List<IotBackupConfigDO> configs = backupConfigMapper.selectEnabledList();

            for (IotBackupConfigDO config : configs) {
                try {
                    if (shouldExecuteBackup(config)) {
                        executeBackup(config);
                    }
                } catch (Exception e) {
                    log.error("[checkAndExecuteBackups][执行备份失败] configId={}", config.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[checkAndExecuteBackups][检查备份任务失败]", e);
        }
    }

    /**
     * 判断是否应该执行备份
     *
     * 简化版实现：根据 Cron 表达式和最后执行时间判断
     * 实际生产环境建议使用 Quartz 的 CronTrigger
     *
     * @param config 备份配置
     * @return 是否应该执行
     */
    private boolean shouldExecuteBackup(IotBackupConfigDO config) {
        Long lastExecutionTime = lastExecutionTimes.get(config.getId());

        // 第一次执行或距离上次执行超过最小间隔
        long currentTime = System.currentTimeMillis();
        long minInterval = getMinIntervalFromCron(config.getCronExpression());

        if (lastExecutionTime == null || (currentTime - lastExecutionTime) >= minInterval) {
            return isCronMatched(config.getCronExpression());
        }

        return false;
    }

    /**
     * 执行备份
     *
     * @param config 备份配置
     */
    private void executeBackup(IotBackupConfigDO config) {
        log.info("[executeBackup][开始执行定时备份] configId={}, configName={}",
                config.getId(), config.getConfigName());

        try {
            // 构建备份请求
            IotBackupRecordCreateReqVO reqVO = new IotBackupRecordCreateReqVO();
            reqVO.setBackupName(generateBackupName(config));
            reqVO.setBackupType(config.getBackupType());
            reqVO.setBackupScope(config.getBackupScope());
            reqVO.setBackupMode(BackupModeEnum.AUTO.getMode());
            reqVO.setRemark("自动定时备份 - " + config.getConfigName());

            // 执行备份
            Long backupId = backupRecordService.createBackup(reqVO);

            // 更新最后执行时间
            lastExecutionTimes.put(config.getId(), System.currentTimeMillis());

            log.info("[executeBackup][定时备份执行成功] configId={}, backupId={}",
                    config.getId(), backupId);

        } catch (Exception e) {
            log.error("[executeBackup][定时备份执行失败] configId={}", config.getId(), e);
        }
    }

    /**
     * 从 Cron 表达式获取最小执行间隔（毫秒）
     *
     * 简化实现：返回固定间隔
     * 实际生产环境建议解析 Cron 表达式计算精确间隔
     *
     * @param cronExpression Cron 表达式
     * @return 最小间隔（毫秒）
     */
    private long getMinIntervalFromCron(String cronExpression) {
        // 简化实现：默认最小间隔1小时
        // 实际应该解析 Cron 表达式获取精确间隔
        return 60 * 60 * 1000; // 1小时
    }

    /**
     * 判断当前时间是否匹配 Cron 表达式
     *
     * 简化实现：使用基础时间匹配
     * 实际生产环境建议使用 CronSequenceGenerator
     *
     * @param cronExpression Cron 表达式
     * @return 是否匹配
     */
    private boolean isCronMatched(String cronExpression) {
        // 简化实现：解析 Cron 表达式的小时部分
        // 格式：秒 分 时 日 月 星期
        // 例如：0 0 2 * * ? 表示每天凌晨2点

        try {
            LocalDateTime now = LocalDateTime.now();
            String[] parts = cronExpression.split("\\s+");

            if (parts.length >= 3) {
                String minute = parts[1];
                String hour = parts[2];

                // 简单匹配：当前分钟和小时
                if (!"*".equals(minute) && !"?".equals(minute)) {
                    if (now.getMinute() != Integer.parseInt(minute)) {
                        return false;
                    }
                }

                if (!"*".equals(hour) && !"?".equals(hour)) {
                    if (now.getHour() != Integer.parseInt(hour)) {
                        return false;
                    }
                }

                return true;
            }

        } catch (Exception e) {
            log.error("[isCronMatched][Cron表达式匹配失败] cron={}", cronExpression, e);
        }

        return false;
    }

    /**
     * 生成备份名称
     *
     * @param config 配置
     * @return 备份名称
     */
    private String generateBackupName(IotBackupConfigDO config) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return String.format("auto_%s_%s", config.getConfigName(), timestamp);
    }

}
