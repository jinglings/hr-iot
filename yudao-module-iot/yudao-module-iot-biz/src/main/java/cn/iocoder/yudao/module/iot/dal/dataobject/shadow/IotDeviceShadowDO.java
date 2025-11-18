package cn.iocoder.yudao.module.iot.dal.dataobject.shadow;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备影子 DO
 *
 * 设备影子是设备在云端的虚拟表示，包含期望状态和实际状态
 *
 * @author AI Assistant
 */
@TableName(value = "iot_device_shadow", autoResultMap = true)
@KeySequence("iot_device_shadow_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceShadowDO extends TenantBaseDO {

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
     * 期望状态（云端希望设备达到的状态）JSON格式
     */
    private String desiredState;

    /**
     * 实际状态（设备实际上报的状态）JSON格式
     */
    private String reportedState;

    /**
     * 元数据（状态更新时间等）JSON格式
     */
    private String metadata;

    /**
     * 版本号（用于并发控制）
     */
    private Integer version;

    /**
     * 期望状态版本号
     */
    private Integer desiredVersion;

    /**
     * 实际状态版本号
     */
    private Integer reportedVersion;

    /**
     * 最后期望状态更新时间
     */
    private LocalDateTime lastDesiredTime;

    /**
     * 最后实际状态上报时间
     */
    private LocalDateTime lastReportedTime;

}
