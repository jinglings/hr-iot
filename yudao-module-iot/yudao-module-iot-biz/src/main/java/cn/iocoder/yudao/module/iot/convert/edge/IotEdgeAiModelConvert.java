package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT 边缘AI模型 Convert
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeAiModelConvert {

    IotEdgeAiModelConvert INSTANCE = Mappers.getMapper(IotEdgeAiModelConvert.class);

    IotEdgeAiModelDO convert(IotEdgeAiModelCreateReqVO bean);

    IotEdgeAiModelDO convert(IotEdgeAiModelUpdateReqVO bean);

    IotEdgeAiModelRespVO convert(IotEdgeAiModelDO bean);

    List<IotEdgeAiModelRespVO> convertList(List<IotEdgeAiModelDO> list);

    PageResult<IotEdgeAiModelRespVO> convertPage(PageResult<IotEdgeAiModelDO> page);

}
