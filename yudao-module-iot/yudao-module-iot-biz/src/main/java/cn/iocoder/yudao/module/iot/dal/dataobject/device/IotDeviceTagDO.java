package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 设备标签 DO
 *
 * @author AI
 */
@TableName(value = "iot_device_tag", autoResultMap = true)
@KeySequence("iot_device_tag_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceTagDO extends TenantBaseDO {

    /**
     * 标签ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 标签键
     */
    private String tagKey;

    /**
     * 标签值
     */
    private String tagValue;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签显示颜色（如 #409EFF）
     */
    private String color;

    /**
     * 是否预置标签
     * 0=否, 1=是
     */
    private Boolean isPreset;

    /**
     * 使用次数
     */
    private Integer usageCount;

}
