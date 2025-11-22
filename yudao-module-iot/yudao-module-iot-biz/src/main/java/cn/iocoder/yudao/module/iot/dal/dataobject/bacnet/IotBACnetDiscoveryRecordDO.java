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

import java.time.LocalDateTime;

/**
 * BACnet 设备发现记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_bacnet_discovery_record")
@KeySequence("iot_bacnet_discovery_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotBACnetDiscoveryRecordDO extends TenantBaseDO {

    /**
     * 记录编号
     */
    @TableId
    private Long id;

    /**
     * BACnet 设备实例号
     */
    private Integer instanceNumber;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 供应商 ID
     */
    private Integer vendorId;

    /**
     * 供应商名称
     */
    private String vendorName;

    /**
     * 设备型号
     */
    private String modelName;

    /**
     * 固件版本
     */
    private String firmwareRevision;

    /**
     * 应用软件版本
     */
    private String applicationSoftwareVersion;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 对象列表（JSON 格式）
     */
    private String objectList;

    /**
     * 是否已绑定到 IoT 设备
     */
    private Boolean isBound;

    /**
     * 绑定的 IoT 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long boundDeviceId;

    /**
     * 发现时间
     */
    private LocalDateTime discoveryTime;

    /**
     * 最后发现时间
     */
    private LocalDateTime lastSeenTime;

    /**
     * 状态
     * 0=离线, 1=在线
     */
    private Integer status;

}
