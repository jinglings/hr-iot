package cn.iocoder.yudao.module.report.dal.dataobject.visualization;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 可视化大屏表
 *
 * 每个大屏对应一个项目
 *
 * @author 芋道源码
 */
@TableName(value = "report_visualization_dashboard", autoResultMap = true)
@KeySequence("report_visualization_dashboard_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationDashboardDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId
    private Long id;

    /**
     * 大屏名称
     */
    private String name;

    /**
     * 大屏描述
     */
    private String description;

    /**
     * 预览图片 URL
     */
    private String picUrl;

    /**
     * 缩略图 URL
     */
    private String thumbnail;

    /**
     * 大屏配置
     *
     * JSON 配置，包含画布配置和所有组件
     */
    private String config;

    /**
     * 画布宽度
     */
    private Integer width;

    /**
     * 画布高度
     */
    private Integer height;

    /**
     * 背景颜色
     */
    private String backgroundColor;

    /**
     * 背景图片 URL
     */
    private String backgroundImage;

    /**
     * 屏幕适配模式
     *
     * scale - 等比缩放
     * width - 宽度适配
     * height - 高度适配
     * stretch - 拉伸铺满
     */
    private String scaleMode;

    /**
     * 发布状态
     *
     * 0 - 未发布
     * 1 - 已发布
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    /**
     * 访问密码
     */
    private String password;

    /**
     * 是否公开
     *
     * true - 公开
     * false - 私有
     */
    private Boolean isPublic;

    /**
     * 分类
     *
     * kpi - KPI仪表板
     * data_analysis - 数据分析
     * iot_monitor - 物联网监控
     * business - 业务监控
     */
    private String category;

    /**
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * 备注
     */
    private String remark;
}
