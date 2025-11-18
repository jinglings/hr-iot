package cn.iocoder.yudao.module.iot.service.edge.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log.IotEdgeRuleLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleLogDO;

/**
 * IoT 边缘规则执行日志 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeRuleLogService {

    /**
     * 创建规则执行日志
     *
     * @param ruleId 规则编号
     * @param gatewayId 网关编号
     * @param executeStatus 执行状态
     * @param executeResult 执行结果
     */
    void createRuleLog(Long ruleId, Long gatewayId, Integer executeStatus, String executeResult);

    /**
     * 获得规则执行日志
     *
     * @param id 编号
     * @return 规则执行日志
     */
    IotEdgeRuleLogDO getRuleLog(Long id);

    /**
     * 获得规则执行日志分页
     *
     * @param pageReqVO 分页查询
     * @return 规则执行日志分页
     */
    PageResult<IotEdgeRuleLogDO> getRuleLogPage(IotEdgeRuleLogPageReqVO pageReqVO);

    /**
     * 删除规则执行日志
     *
     * @param id 编号
     */
    void deleteRuleLog(Long id);

    /**
     * 清空规则执行日志
     *
     * @param ruleId 规则编号
     */
    void clearRuleLogs(Long ruleId);

}
