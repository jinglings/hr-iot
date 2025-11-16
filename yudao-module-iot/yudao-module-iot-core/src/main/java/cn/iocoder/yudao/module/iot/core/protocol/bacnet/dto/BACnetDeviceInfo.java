package cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * BACnet 设备信息 DTO
 *
 * @author yudao
 */
@Data
public class BACnetDeviceInfo implements Serializable {

    /**
     * 设备实例号
     */
    private Integer instanceNumber;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 设备 IP 地址
     */
    private String ipAddress;

    /**
     * 设备端口
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
     * 设备是否在线
     */
    private Boolean online = false;

    /**
     * 最后通信时间
     */
    private Long lastCommunicationTime;

}
