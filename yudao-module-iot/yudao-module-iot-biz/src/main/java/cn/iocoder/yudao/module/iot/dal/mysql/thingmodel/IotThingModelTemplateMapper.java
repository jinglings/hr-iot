package cn.iocoder.yudao.module.iot.dal.mysql.thingmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template.IotThingModelTemplateListReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 物模型模板 Mapper
 *
 * @author AI
 */
@Mapper
public interface IotThingModelTemplateMapper extends BaseMapperX<IotThingModelTemplateDO> {

    /**
     * 查询模板列表
     */
    default List<IotThingModelTemplateDO> selectList(IotThingModelTemplateListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<IotThingModelTemplateDO>()
                .eqIfPresent(IotThingModelTemplateDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(IotThingModelTemplateDO::getName, reqVO.getName())
                .eqIfPresent(IotThingModelTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotThingModelTemplateDO::getIsSystem, reqVO.getIsSystem())
                .orderByAsc(IotThingModelTemplateDO::getCategoryId)
                .orderByAsc(IotThingModelTemplateDO::getSort)
                .orderByDesc(IotThingModelTemplateDO::getUsageCount));
    }

    /**
     * 根据编码查询
     */
    default IotThingModelTemplateDO selectByCode(String code) {
        return selectOne(IotThingModelTemplateDO::getCode, code);
    }

    /**
     * 查询指定分类下的模板
     */
    default List<IotThingModelTemplateDO> selectListByCategoryId(Long categoryId) {
        return selectList(new LambdaQueryWrapperX<IotThingModelTemplateDO>()
                .eq(IotThingModelTemplateDO::getCategoryId, categoryId)
                .eq(IotThingModelTemplateDO::getStatus, 1)
                .orderByAsc(IotThingModelTemplateDO::getSort));
    }

    /**
     * 增加使用次数
     */
    default void incrementUsageCount(Long id) {
        IotThingModelTemplateDO template = selectById(id);
        if (template != null) {
            template.setUsageCount(template.getUsageCount() + 1);
            updateById(template);
        }
    }

}
