package cn.iocoder.yudao.module.iot.service.edge.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log.IotEdgeRuleLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeRuleLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * IoT 边缘规则执行日志 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
public class IotEdgeRuleLogServiceImpl implements IotEdgeRuleLogService {

    @Resource
    private IotEdgeRuleLogMapper ruleLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRuleLog(Long ruleId, Long gatewayId, Integer executeStatus, String executeResult) {
        IotEdgeRuleLogDO log = IotEdgeRuleLogDO.builder()
                .ruleId(ruleId)
                .gatewayId(gatewayId)
                .executeTime(LocalDateTime.now())
                .executeStatus(executeStatus)
                .executeResult(Integer.valueOf(executeResult))
                .build();
        ruleLogMapper.insert(log);
    }

    @Override
    public IotEdgeRuleLogDO getRuleLog(Long id) {
        return ruleLogMapper.selectById(id);
    }

    @Override
    public PageResult<IotEdgeRuleLogDO> getRuleLogPage(IotEdgeRuleLogPageReqVO pageReqVO) {
        return ruleLogMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRuleLog(Long id) {
        ruleLogMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRuleLogs(Long ruleId) {
        ruleLogMapper.deleteByRuleId(ruleId);
    }

}
