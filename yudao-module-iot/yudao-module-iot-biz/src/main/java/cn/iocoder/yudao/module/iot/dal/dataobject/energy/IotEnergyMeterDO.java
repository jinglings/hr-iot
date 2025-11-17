package cn.iocoder.yudao.module.iot.dal.dataobject.energy;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * IoT 能源管理 - 计量点 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_meter", autoResultMap = true)
@KeySequence("iot_energy_meter_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyMeterDO extends TenantBaseDO {

    /**
     * 计量点ID
     */
    @TableId
    private Long id;

    /**
     * 计量点名称
     */
    private String meterName;

    /**
     * 计量点编码
     */
    private String meterCode;

    /**
     * 能源类型ID
     */
    private Long energyTypeId;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 设备属性标识符(物模型identifier)
     */
    private String deviceProperty;

    /**
     * 所属建筑ID
     */
    private Long buildingId;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 所属楼层ID
     */
    private Long floorId;

    /**
     * 所属房间ID
     */
    private Long roomId;

    /**
     * 计量点级别(1:一级表 2:二级表 3:三级表)
     */
    private Integer meterLevel;

    /**
     * 父级计量点ID(0表示顶级)
     */
    private Long parentId;

    /**
     * 计量倍率(CT/PT比)
     */
    private BigDecimal ratio;

    /**
     * 是否虚拟表(通过其他表计算)
     */
    private Boolean isVirtual;

    /**
     * 计算公式(虚拟表使用,如:M001+M002-M003)
     */
    private String calcFormula;

    /**
     * 计量点描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0:停用 1:启用)
     */
    private Integer status;

}
