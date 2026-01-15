package cn.iocoder.yudao.module.iot.dal.dataobject.scada;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SCADA 告警历史记录 DO
 *
 * 记录告警事件的历史
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@TableName("scada_alarm_history")
@KeySequence("scada_alarm_history_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotScadaAlarmHistoryDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 告警配置 ID
     */
    private Long alarmId;

    /**
     * 告警名称
     */
    private String alarmName;

    /**
     * 关联的 tag ID
     */
    private String tagId;

    /**
     * 触发时的 tag 值
     */
    private String tagValue;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 状态: 1-活动, 2-已确认, 3-已恢复, 4-已关闭
     */
    private Integer status;

    /**
     * 触发时间
     */
    private LocalDateTime triggeredAt;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedAt;

    /**
     * 确认人
     */
    private String acknowledgedBy;

    /**
     * 恢复时间
     */
    private LocalDateTime recoveredAt;

    /**
     * 关闭时间
     */
    private LocalDateTime closedAt;

    /**
     * 关闭人
     */
    private String closedBy;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
