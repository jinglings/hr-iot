package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT 边缘规则 Convert
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeRuleConvert {

    IotEdgeRuleConvert INSTANCE = Mappers.getMapper(IotEdgeRuleConvert.class);

    IotEdgeRuleDO convert(IotEdgeRuleCreateReqVO bean);

    IotEdgeRuleDO convert(IotEdgeRuleUpdateReqVO bean);

    IotEdgeRuleRespVO convert(IotEdgeRuleDO bean);

    List<IotEdgeRuleRespVO> convertList(List<IotEdgeRuleDO> list);

    PageResult<IotEdgeRuleRespVO> convertPage(PageResult<IotEdgeRuleDO> page);

}
