package cn.iocoder.yudao.module.iot.dal.dataobject.energy.report;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 能源报表记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_report_record", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyReportRecordDO extends TenantBaseDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报表模板ID
     */
    private Long templateId;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 报表类型：day-日报，week-周报，month-月报，year-年报，custom-自定义
     */
    private String reportType;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报表数据（JSON格式）
     */
    private String reportData;

    /**
     * 文件路径（Excel/PDF）
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 生成状态：0-生成中，1-生成成功，2-生成失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 生成方式：1-手动生成，2-定时任务生成
     */
    private Integer generateType;

}
