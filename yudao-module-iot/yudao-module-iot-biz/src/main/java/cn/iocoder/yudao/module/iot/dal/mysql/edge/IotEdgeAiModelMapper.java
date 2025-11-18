package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 边缘 AI 模型 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeAiModelMapper extends BaseMapperX<IotEdgeAiModelDO> {

    default PageResult<IotEdgeAiModelDO> selectPage(IotEdgeAiModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeAiModelDO>()
                .likeIfPresent(IotEdgeAiModelDO::getName, reqVO.getName())
                .eqIfPresent(IotEdgeAiModelDO::getModelType, reqVO.getModelType())
                .eqIfPresent(IotEdgeAiModelDO::getModelFormat, reqVO.getModelFormat())
                .eqIfPresent(IotEdgeAiModelDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEdgeAiModelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotEdgeAiModelDO::getId));
    }

    default IotEdgeAiModelDO selectByNameAndVersion(String name, String version) {
        return selectOne(new LambdaQueryWrapperX<IotEdgeAiModelDO>()
                .eq(IotEdgeAiModelDO::getName, name)
                .eq(IotEdgeAiModelDO::getVersion, version));
    }

    default Long selectCountByModelType(String modelType) {
        return selectCount(IotEdgeAiModelDO::getModelType, modelType);
    }

}
