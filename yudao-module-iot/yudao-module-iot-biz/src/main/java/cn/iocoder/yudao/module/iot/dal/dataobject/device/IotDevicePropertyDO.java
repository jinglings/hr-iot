package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备属性项 Redis DO
 *
 * @see cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants#DEVICE_PROPERTY
 * @see DevicePropertyRedisDAO
 *
 * @author haohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDevicePropertyDO {

    /**
     * 属性值（最新）
     */
    private Object value;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 值最后一次真正变化的时间（冻结起点）
     *
     * 值与上一次不同则刷新为本次上报时间；值未变化则一直保持不动。
     * 用于判断设备"通信正常但数据冻结"，即从该时间起就没有获取到新的值。
     */
    private LocalDateTime changeTime;

    /**
     * 数据是否疑似冻结：1=冻结（持续未变化超过阈值），0=正常
     *
     * 注意：兼容旧缓存数据，该字段可能为 {@code null}，前端按"正常"处理。
     */
    private Integer stale;

}