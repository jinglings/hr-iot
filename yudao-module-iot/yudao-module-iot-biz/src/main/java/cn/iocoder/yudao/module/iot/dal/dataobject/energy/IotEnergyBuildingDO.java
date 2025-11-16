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
 * IoT 能源管理 - 建筑 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_building", autoResultMap = true)
@KeySequence("iot_energy_building_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyBuildingDO extends TenantBaseDO {

    /**
     * 建筑ID
     */
    @TableId
    private Long id;

    /**
     * 建筑名称
     */
    private String buildingName;

    /**
     * 建筑编码
     */
    private String buildingCode;

    /**
     * 建筑类型
     *
     * office:办公楼, factory:厂房, dormitory:宿舍, warehouse:仓库, other:其他
     */
    private String buildingType;

    /**
     * 建筑地址
     */
    private String address;

    /**
     * 建筑面积(平方米)
     */
    private BigDecimal area;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 建筑图片URL
     */
    private String imageUrl;

    /**
     * 建筑描述
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
