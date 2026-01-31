package cn.iocoder.yudao.module.iot.dal.dataobject.backup;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT TDengine 备份配置 DO
 *
 * @author claude
 */
@TableName(value = "iot_backup_config")
@KeySequence("iot_backup_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IotBackupConfigDO extends TenantBaseDO {

    /**
     * 配置ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备份类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.backup.BackupTypeEnum}
     *
     * 1 - 全量备份
     * 2 - 数据库备份
     * 3 - 表备份
     */
    private Integer backupType;

    /**
     * 备份范围（JSON格式）
     *
     * 数据库列表或表列表
     */
    private String backupScope;

    /**
     * Cron表达式
     *
     * 例如：0 0 2 * * ? 表示每天凌晨2点执行
     */
    private String cronExpression;

    /**
     * 保留天数
     *
     * 超过该天数的备份将自动删除
     */
    private Integer retentionDays;

    /**
     * 最大备份数量
     *
     * 超过该数量的备份将删除最旧的
     */
    private Integer maxBackupCount;

    /**
     * 是否启用压缩
     */
    private Boolean compressionEnabled;

    /**
     * 成功时是否通知
     */
    private Boolean notifyOnSuccess;

    /**
     * 失败时是否通知
     */
    private Boolean notifyOnFailure;

    /**
     * 通知邮箱列表（逗号分隔）
     */
    private String notifyEmails;

}
