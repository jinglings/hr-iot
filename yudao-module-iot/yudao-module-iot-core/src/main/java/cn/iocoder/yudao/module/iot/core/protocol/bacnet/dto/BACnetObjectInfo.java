package cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * BACnet 对象信息 DTO
 *
 * @author yudao
 */
@Data
public class BACnetObjectInfo implements Serializable {

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 对象实例号
     */
    private Integer instanceNumber;

    /**
     * 对象名称
     */
    private String objectName;

    /**
     * 对象描述
     */
    private String description;

    /**
     * 当前值
     */
    private Object presentValue;

    /**
     * 单位
     */
    private String units;

    /**
     * 状态标志
     */
    private String statusFlags;

    /**
     * 是否可写
     */
    private Boolean writable = false;

}
