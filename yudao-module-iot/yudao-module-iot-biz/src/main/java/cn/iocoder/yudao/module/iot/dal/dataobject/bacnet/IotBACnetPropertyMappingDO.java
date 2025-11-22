package cn.iocoder.yudao.module.iot.dal.dataobject.bacnet;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BACnet 属性映射配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_bacnet_property_mapping")
@KeySequence("iot_bacnet_property_mapping_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotBACnetPropertyMappingDO extends TenantBaseDO {

    /**
     * 映射编号
     */
    @TableId
    private Long id;

    /**
     * BACnet 设备配置编号
     *
     * 关联 {@link IotBACnetDeviceConfigDO#getId()}
     */
    private Long deviceConfigId;

    /**
     * IoT 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * 物模型功能编号
     *
     * 关联 {@link IotThingModelDO#getId()}
     */
    private Long thingModelId;

    /**
     * 物模型属性标识符
     */
    private String identifier;

    // ========== BACnet 对象信息 ==========

    /**
     * BACnet 对象类型
     * 如：ANALOG_INPUT, BINARY_OUTPUT, MULTI_STATE_VALUE
     */
    private String objectType;

    /**
     * BACnet 对象实例号
     */
    private Integer objectInstance;

    /**
     * BACnet 属性标识符
     * 默认 PRESENT_VALUE
     */
    private String propertyIdentifier;

    /**
     * 属性数组索引（可选）
     */
    private Integer propertyArrayIndex;

    // ========== 数据转换配置 ==========

    /**
     * 数据类型
     * int, float, double, bool, text, enum
     */
    private String dataType;

    /**
     * 单位转换公式
     * 如：value * 0.1, value / 10 + 32
     */
    private String unitConversion;

    /**
     * 值映射（JSON 格式）
     * 用于枚举类型，如：{"0":"关闭","1":"开启"}
     */
    private String valueMapping;

    // ========== 访问控制 ==========

    /**
     * 访问模式
     * r=只读, w=只写, rw=读写
     */
    private String accessMode;

    /**
     * 写入优先级（1-16）
     * 用于可写属性
     */
    private Integer priority;

    // ========== 轮询配置 ==========

    /**
     * 是否启用轮询
     */
    private Boolean pollingEnabled;

    /**
     * 是否启用 COV 订阅（Change of Value）
     */
    private Boolean covEnabled;

    /**
     * 映射状态
     * 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

}
