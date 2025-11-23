package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T19:37:26+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
public class IotEdgeRuleConvertImpl implements IotEdgeRuleConvert {

    @Override
    public IotEdgeRuleDO convert(IotEdgeRuleCreateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeRuleDO.IotEdgeRuleDOBuilder iotEdgeRuleDO = IotEdgeRuleDO.builder();

        iotEdgeRuleDO.name( bean.getName() );
        iotEdgeRuleDO.gatewayId( bean.getGatewayId() );
        iotEdgeRuleDO.ruleType( bean.getRuleType() );
        iotEdgeRuleDO.triggerConfig( bean.getTriggerConfig() );
        iotEdgeRuleDO.conditionConfig( bean.getConditionConfig() );
        iotEdgeRuleDO.actionConfig( bean.getActionConfig() );
        iotEdgeRuleDO.executeLocal( bean.getExecuteLocal() );
        iotEdgeRuleDO.priority( bean.getPriority() );
        iotEdgeRuleDO.description( bean.getDescription() );

        return iotEdgeRuleDO.build();
    }

    @Override
    public IotEdgeRuleDO convert(IotEdgeRuleUpdateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeRuleDO.IotEdgeRuleDOBuilder iotEdgeRuleDO = IotEdgeRuleDO.builder();

        iotEdgeRuleDO.id( bean.getId() );
        iotEdgeRuleDO.name( bean.getName() );
        iotEdgeRuleDO.triggerConfig( bean.getTriggerConfig() );
        iotEdgeRuleDO.conditionConfig( bean.getConditionConfig() );
        iotEdgeRuleDO.actionConfig( bean.getActionConfig() );
        iotEdgeRuleDO.priority( bean.getPriority() );
        iotEdgeRuleDO.description( bean.getDescription() );

        return iotEdgeRuleDO.build();
    }

    @Override
    public IotEdgeRuleRespVO convert(IotEdgeRuleDO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeRuleRespVO iotEdgeRuleRespVO = new IotEdgeRuleRespVO();

        iotEdgeRuleRespVO.setId( bean.getId() );
        iotEdgeRuleRespVO.setName( bean.getName() );
        iotEdgeRuleRespVO.setGatewayId( bean.getGatewayId() );
        iotEdgeRuleRespVO.setRuleType( bean.getRuleType() );
        iotEdgeRuleRespVO.setTriggerConfig( bean.getTriggerConfig() );
        iotEdgeRuleRespVO.setConditionConfig( bean.getConditionConfig() );
        iotEdgeRuleRespVO.setActionConfig( bean.getActionConfig() );
        iotEdgeRuleRespVO.setExecuteLocal( bean.getExecuteLocal() );
        iotEdgeRuleRespVO.setPriority( bean.getPriority() );
        iotEdgeRuleRespVO.setStatus( bean.getStatus() );
        iotEdgeRuleRespVO.setDeployStatus( bean.getDeployStatus() );
        iotEdgeRuleRespVO.setDeployTime( bean.getDeployTime() );
        iotEdgeRuleRespVO.setDeployError( bean.getDeployError() );
        iotEdgeRuleRespVO.setExecuteCount( bean.getExecuteCount() );
        iotEdgeRuleRespVO.setLastExecuteTime( bean.getLastExecuteTime() );
        iotEdgeRuleRespVO.setDescription( bean.getDescription() );
        iotEdgeRuleRespVO.setCreateTime( bean.getCreateTime() );

        return iotEdgeRuleRespVO;
    }

    @Override
    public List<IotEdgeRuleRespVO> convertList(List<IotEdgeRuleDO> list) {
        if ( list == null ) {
            return null;
        }

        List<IotEdgeRuleRespVO> list1 = new ArrayList<IotEdgeRuleRespVO>( list.size() );
        for ( IotEdgeRuleDO iotEdgeRuleDO : list ) {
            list1.add( convert( iotEdgeRuleDO ) );
        }

        return list1;
    }

    @Override
    public PageResult<IotEdgeRuleRespVO> convertPage(PageResult<IotEdgeRuleDO> page) {
        if ( page == null ) {
            return null;
        }

        PageResult<IotEdgeRuleRespVO> pageResult = new PageResult<IotEdgeRuleRespVO>();

        pageResult.setTotal( page.getTotal() );
        pageResult.setList( convertList( page.getList() ) );

        return pageResult;
    }
}
