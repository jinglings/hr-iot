package cn.iocoder.yudao.module.iot.dal.dataobject.backup;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT TDengine 备份记录 DO
 *
 * @author claude
 */
@TableName(value = "iot_backup_record")
@KeySequence("iot_backup_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IotBackupRecordDO extends TenantBaseDO {

    /**
     * 备份ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 备份名称
     */
    private String backupName;

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
     * 数据库列表或表列表，例如：
     * - 全量备份：null 或 []
     * - 数据库备份：["ruoyi_vue_pro"]
     * - 表备份：["device_message", "device_property"]
     */
    private String backupScope;

    /**
     * 备份模式
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum}
     *
     * 1 - 手动
     * 2 - 自动定时
     */
    private Integer backupMode;

    /**
     * 备份状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.backup.BackupStatusEnum}
     *
     * 0 - 备份中
     * 1 - 成功
     * 2 - 失败
     */
    private Integer backupStatus;

    /**
     * 备份文件路径
     */
    private String filePath;

    /**
     * 备份文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件格式
     *
     * 例如：zip, tar.gz
     */
    private String fileFormat;

    /**
     * 备份开始时间
     */
    private LocalDateTime startTime;

    /**
     * 备份结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备份耗时（秒）
     */
    private Integer duration;

    /**
     * 备份数据条数
     */
    private Long recordCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 备份备注
     */
    private String remark;

}
