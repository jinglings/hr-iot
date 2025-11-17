package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT 边缘网关 Convert
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeGatewayConvert {

    IotEdgeGatewayConvert INSTANCE = Mappers.getMapper(IotEdgeGatewayConvert.class);

    IotEdgeGatewayDO convert(IotEdgeGatewayCreateReqVO bean);

    IotEdgeGatewayDO convert(IotEdgeGatewayUpdateReqVO bean);

    IotEdgeGatewayRespVO convert(IotEdgeGatewayDO bean);

    List<IotEdgeGatewayRespVO> convertList(List<IotEdgeGatewayDO> list);

    PageResult<IotEdgeGatewayRespVO> convertPage(PageResult<IotEdgeGatewayDO> page);

}
