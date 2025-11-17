package cn.iocoder.yudao.module.iot.dal.dataobject.energy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * IoT 能源管理 - 能源类型 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_type", autoResultMap = true)
@KeySequence("iot_energy_type_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyTypeDO extends TenantBaseDO {

    /**
     * 能源类型ID
     */
    @TableId
    private Long id;

    /**
     * 能源名称
     */
    private String energyName;

    /**
     * 能源编码
     */
    private String energyCode;

    /**
     * 父级能源ID（0表示顶级）
     */
    private Long parentId;

    /**
     * 计量单位（kWh, m³, GJ, kg等）
     */
    private String unit;

    /**
     * 折标煤系数（kgce）
     */
    private BigDecimal coalConversionFactor;

    /**
     * 碳排放系数（kgCO₂）
     */
    private BigDecimal carbonEmissionFactor;

    /**
     * 能源图标
     */
    private String icon;

    /**
     * 图表颜色（十六进制）
     */
    private String color;

    /**
     * 能源描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     *
     * 0:停用 1:启用
     */
    private Integer status;

}
