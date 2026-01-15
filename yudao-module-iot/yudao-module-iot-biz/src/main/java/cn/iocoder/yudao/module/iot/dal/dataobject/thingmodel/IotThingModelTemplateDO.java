package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 物模型模板 DO
 *
 * @author AI
 */
@TableName(value = "iot_thing_model_template", autoResultMap = true)
@KeySequence("iot_thing_model_template_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotThingModelTemplateDO extends TenantBaseDO {

    /**
     * 模板ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板图标
     */
    private String icon;

    /**
     * 物模型TSL定义(JSON格式)
     */
    private String tsl;

    /**
     * 是否系统模板
     * 0=否, 1=是
     */
    private Boolean isSystem;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

}
