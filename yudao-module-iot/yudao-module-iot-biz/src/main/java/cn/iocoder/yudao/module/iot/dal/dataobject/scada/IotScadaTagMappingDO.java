package cn.iocoder.yudao.module.iot.dal.dataobject.scada;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * SCADA Tag 映射 DO
 *
 * 映射 SCADA 标签到 IoT 设备属性
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@TableName("scada_tag_mapping")
@KeySequence("scada_tag_mapping_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IotScadaTagMappingDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * SCADA tag 名称 (e.g., Pump1_Status)
     */
    private String tagName;

    /**
     * FUXA 内部 tag ID
     */
    private String tagId;

    /**
     * IoT 平台设备 ID
     */
    private Long deviceId;

    /**
     * 物模型属性标识符
     */
    private String propertyIdentifier;

    /**
     * 数据类型: number, string, boolean, object
     */
    private String dataType;

    /**
     * 单位
     */
    private String unit;

    /**
     * 缩放系数
     */
    private BigDecimal scaleFactor;

    /**
     * 偏移量
     */
    private BigDecimal offset;

    /**
     * 最小值
     */
    private BigDecimal minValue;

    /**
     * 最大值
     */
    private BigDecimal maxValue;

    /**
     * 描述
     */
    private String description;

}
