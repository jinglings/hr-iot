package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 设备调试日志 DO
 *
 * @author AI
 */
@TableName(value = "iot_device_debug_log", autoResultMap = true)
@KeySequence("iot_device_debug_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDebugLogDO {

    /**
     * 日志ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备标识
     */
    private String deviceKey;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 消息方向: 1=上行, 2=下行
     */
    private Integer direction;

    /**
     * 消息类型: property_report, property_set, service_invoke, event_report
     */
    private String type;

    /**
     * 属性/事件/服务标识符
     */
    private String identifier;

    /**
     * 消息内容(JSON)
     */
    private String payload;

    /**
     * 调用结果(JSON)
     */
    private String result;

    /**
     * 状态: 0=失败, 1=成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 响应延迟(毫秒)
     */
    private Integer latency;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

    // ========== 常量 ==========

    /**
     * 方向: 上行
     */
    public static final int DIRECTION_UP = 1;

    /**
     * 方向: 下行
     */
    public static final int DIRECTION_DOWN = 2;

    /**
     * 类型: 属性上报
     */
    public static final String TYPE_PROPERTY_REPORT = "property_report";

    /**
     * 类型: 属性设置
     */
    public static final String TYPE_PROPERTY_SET = "property_set";

    /**
     * 类型: 服务调用
     */
    public static final String TYPE_SERVICE_INVOKE = "service_invoke";

    /**
     * 类型: 事件上报
     */
    public static final String TYPE_EVENT_REPORT = "event_report";

}
