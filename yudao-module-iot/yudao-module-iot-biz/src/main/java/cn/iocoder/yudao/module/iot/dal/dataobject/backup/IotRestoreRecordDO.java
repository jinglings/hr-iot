package cn.iocoder.yudao.module.iot.dal.dataobject.backup;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT TDengine 恢复记录 DO
 *
 * @author claude
 */
@TableName(value = "iot_restore_record")
@KeySequence("iot_restore_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IotRestoreRecordDO extends TenantBaseDO {

    /**
     * 恢复ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 备份记录ID
     *
     * 关联 {@link IotBackupRecordDO#getId()}
     */
    private Long backupId;

    /**
     * 恢复类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.backup.RestoreTypeEnum}
     *
     * 1 - 完整恢复
     * 2 - 选择性恢复
     */
    private Integer restoreType;

    /**
     * 恢复目标（JSON格式）
     *
     * 选择性恢复时指定要恢复的表或范围
     */
    private String restoreTarget;

    /**
     * 恢复状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.backup.RestoreStatusEnum}
     *
     * 0 - 恢复中
     * 1 - 成功
     * 2 - 失败
     */
    private Integer restoreStatus;

    /**
     * 恢复开始时间
     */
    private LocalDateTime startTime;

    /**
     * 恢复结束时间
     */
    private LocalDateTime endTime;

    /**
     * 恢复耗时（秒）
     */
    private Integer duration;

    /**
     * 恢复数据条数
     */
    private Long recordCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 恢复备注
     */
    private String remark;

}
