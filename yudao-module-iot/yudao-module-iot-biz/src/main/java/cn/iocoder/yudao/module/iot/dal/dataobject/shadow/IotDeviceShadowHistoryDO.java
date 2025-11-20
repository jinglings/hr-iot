package cn.iocoder.yudao.module.iot.dal.dataobject.shadow;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备影子变更历史 DO
 *
 * 记录设备影子的所有变更，用于审计和追踪
 *
 * @author AI Assistant
 */
@TableName(value = "iot_device_shadow_history", autoResultMap = true)
@KeySequence("iot_device_shadow_history_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceShadowHistoryDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 影子ID
     */
    private Long shadowId;

    /**
     * 变更类型: DESIRED, REPORTED, DELTA
     */
    private String changeType;

    /**
     * 变更前状态 JSON
     */
    private String beforeState;

    /**
     * 变更后状态 JSON
     */
    private String afterState;

    /**
     * 差量状态 JSON
     */
    private String deltaState;

    /**
     * 版本号
     */
    private Integer version;

}
