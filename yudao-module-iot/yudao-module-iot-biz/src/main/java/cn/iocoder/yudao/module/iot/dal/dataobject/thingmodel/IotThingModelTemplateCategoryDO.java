package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 物模型模板分类 DO
 *
 * @author AI
 */
@TableName(value = "iot_thing_model_template_category", autoResultMap = true)
@KeySequence("iot_thing_model_template_category_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotThingModelTemplateCategoryDO extends TenantBaseDO {

    /**
     * 分类ID，主键，自增
     */
    @TableId
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否系统分类
     * 0=否, 1=是
     */
    private Boolean isSystem;

}
