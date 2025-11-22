package cn.iocoder.yudao.module.iot.dal.dataobject.bacnet;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BACnet 设备配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_bacnet_device_config")
@KeySequence("iot_bacnet_device_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotBACnetDeviceConfigDO extends TenantBaseDO {

    /**
     * 配置编号
     */
    @TableId
    private Long id;

    /**
     * IoT 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * BACnet 设备实例号
     */
    private Integer instanceNumber;

    /**
     * BACnet 设备 IP 地址
     */
    private String ipAddress;

    /**
     * BACnet 端口，默认 47808
     */
    private Integer port;

    /**
     * 网络号（用于路由）
     */
    private Integer networkNumber;

    /**
     * MAC 地址
     */
    private String macAddress;

    /**
     * 最大 APDU 长度
     */
    private Integer maxApduLength;

    /**
     * 分段支持
     * 0=不支持, 1=发送, 2=接收, 3=发送和接收
     */
    private Integer segmentationSupported;

    /**
     * 供应商 ID
     */
    private Integer vendorId;

    /**
     * 是否启用轮询
     */
    private Boolean pollingEnabled;

    /**
     * 轮询间隔（毫秒）
     */
    private Integer pollingInterval;

    /**
     * 读取超时（毫秒）
     */
    private Integer readTimeout;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 配置状态
     * 0=禁用, 1=启用
     */
    private Integer status;

}
