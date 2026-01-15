package cn.iocoder.yudao.module.iot.dal.mysql.thingmodel;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 物模型模板分类 Mapper
 *
 * @author AI
 */
@Mapper
public interface IotThingModelTemplateCategoryMapper extends BaseMapperX<IotThingModelTemplateCategoryDO> {

    /**
     * 查询所有分类（按排序）
     */
    default List<IotThingModelTemplateCategoryDO> selectList() {
        return selectList(new LambdaQueryWrapperX<IotThingModelTemplateCategoryDO>()
                .orderByAsc(IotThingModelTemplateCategoryDO::getSort)
                .orderByAsc(IotThingModelTemplateCategoryDO::getId));
    }

    /**
     * 根据编码查询
     */
    default IotThingModelTemplateCategoryDO selectByCode(String code) {
        return selectOne(IotThingModelTemplateCategoryDO::getCode, code);
    }

}
