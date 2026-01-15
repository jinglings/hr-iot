package cn.iocoder.yudao.module.iot.dal.dataobject.scada;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * SCADA 告警配置 DO
 *
 * 配置 SCADA 告警规则
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@TableName("scada_alarm")
@KeySequence("scada_alarm_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IotScadaAlarmDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 告警名称
     */
    private String alarmName;

    /**
     * 关联的 tag ID
     */
    private String tagId;

    /**
     * 告警类型: HIGH, HIGHHIGH, LOW, LOWLOW, BOOL, etc.
     */
    private String alarmType;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 触发值
     */
    private BigDecimal triggerValue;

    /**
     * 滞后值
     */
    private BigDecimal hysteresis;

    /**
     * 触发延迟(秒)
     */
    private Integer delayOn;

    /**
     * 恢复延迟(秒)
     */
    private Integer delayOff;

    /**
     * 优先级: 1-低, 2-中, 3-高, 4-紧急
     */
    private Integer priority;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 是否需要确认
     */
    private Boolean ackRequired;

    /**
     * 扩展配置 (JSON)
     */
    private String configJson;

}
