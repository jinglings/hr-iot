package cn.iocoder.yudao.module.iot.job.backup;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotBackupConfigMapper;
import cn.iocoder.yudao.module.iot.service.backup.IotBackupRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * TDengine 备份清理定时任务
 *
 * 根据备份配置，自动清理过期的备份文件
 *
 * @author claude
 */
@Component
@Slf4j
public class BackupCleanupJob {

    @Resource
    private IotBackupRecordService backupRecordService;

    @Resource
    private IotBackupConfigMapper backupConfigMapper;

    /**
     * 清理过期备份
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @TenantJob
    public void cleanupExpiredBackups() {
        try {
            log.info("[cleanupExpiredBackups][开始清理过期备份]");

            // 查询所有启用的备份配置
            List<IotBackupConfigDO> configs = backupConfigMapper.selectEnabledList();

            int totalCleaned = 0;
            for (IotBackupConfigDO config : configs) {
                try {
                    // 按配置清理过期备份
                    backupRecordService.cleanExpiredBackups(
                            config.getRetentionDays(),
                            config.getMaxBackupCount()
                    );
                    totalCleaned++;
                } catch (Exception e) {
                    log.error("[cleanupExpiredBackups][清理失败] configId={}, configName={}",
                            config.getId(), config.getConfigName(), e);
                }
            }

            log.info("[cleanupExpiredBackups][过期备份清理完成] totalConfigs={}, cleaned={}",
                    configs.size(), totalCleaned);

        } catch (Exception e) {
            log.error("[cleanupExpiredBackups][清理过期备份失败]", e);
        }
    }

}
