package cn.iocoder.yudao.module.iot.dal.dataobject.scada;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * SCADA 仪表板配置 DO
 *
 * 存储 FUXA 仪表板配置，支持多租户隔离
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@TableName("scada_dashboard")
@KeySequence("scada_dashboard_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IotScadaDashboardDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * FUXA 仪表板 ID (UUID)
     */
    private String dashboardId;

    /**
     * 仪表板名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 仪表板类型
     * <p>
     * 枚举: water(供水系统), hvac(暖通空调), energy(能源管理), custom(自定义)
     */
    private String dashboardType;

    /**
     * FUXA dashboard 配置 (JSON)
     */
    private String configJson;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 是否默认仪表板
     */
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

}
