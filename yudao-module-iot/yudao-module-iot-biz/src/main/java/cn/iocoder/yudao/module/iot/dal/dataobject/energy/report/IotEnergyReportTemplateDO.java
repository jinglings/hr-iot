package cn.iocoder.yudao.module.iot.dal.dataobject.energy.report;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 能源报表模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_report_template", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyReportTemplateDO extends TenantBaseDO {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 报表类型：day-日报，week-周报，month-月报，year-年报，custom-自定义
     */
    private String reportType;

    /**
     * 报表内容配置（JSON格式）
     * 包含：统计指标、图表配置、数据源配置等
     */
    private String content;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 定时任务cron表达式
     */
    private String cronExpression;

    /**
     * 接收人用户ID列表（逗号分隔）
     */
    private String receiveUserIds;

    /**
     * 通知方式：1-邮件，2-站内信
     */
    private Integer notifyType;

}
