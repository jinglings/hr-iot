package cn.iocoder.yudao.module.iot.service.edge.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;

import javax.validation.Valid;

/**
 * IoT 边缘规则 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeRuleService {

    /**
     * 创建边缘规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRule(@Valid IotEdgeRuleCreateReqVO createReqVO);

    /**
     * 更新边缘规则
     *
     * @param updateReqVO 更新信息
     */
    void updateRule(@Valid IotEdgeRuleUpdateReqVO updateReqVO);

    /**
     * 删除边缘规则
     *
     * @param id 编号
     */
    void deleteRule(Long id);

    /**
     * 获得边缘规则
     *
     * @param id 编号
     * @return 边缘规则
     */
    IotEdgeRuleDO getRule(Long id);

    /**
     * 获得边缘规则分页
     *
     * @param pageReqVO 分页查询
     * @return 边缘规则分页
     */
    PageResult<IotEdgeRuleDO> getRulePage(IotEdgeRulePageReqVO pageReqVO);

    /**
     * 部署边缘规则
     *
     * @param id 编号
     */
    void deployRule(Long id);

    /**
     * 取消部署边缘规则
     *
     * @param id 编号
     */
    void undeployRule(Long id);

    /**
     * 更新规则状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateRuleStatus(Long id, Integer status);

    /**
     * 取消部署边缘规则
     *
     * @param id 编号
     */
    void undeployRule(Long id);

}
