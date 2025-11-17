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
 * IoT 能源管理 - 区域 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_area", autoResultMap = true)
@KeySequence("iot_energy_area_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyAreaDO extends TenantBaseDO {

    /**
     * 区域ID
     */
    @TableId
    private Long id;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 所属建筑ID
     */
    private Long buildingId;

    /**
     * 区域类型
     *
     * production:生产区, office:办公区, public:公共区, storage:仓储区, other:其他
     */
    private String areaType;

    /**
     * 区域面积(平方米)
     */
    private BigDecimal area;

    /**
     * 区域描述
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
