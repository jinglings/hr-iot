package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 设备标签关联 DO
 *
 * @author AI
 */
@TableName(value = "iot_device_tag_relation", autoResultMap = true)
@KeySequence("iot_device_tag_relation_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceTagRelationDO {

    /**
     * 关联ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 标签ID
     */
    private Long tagId;

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

}
