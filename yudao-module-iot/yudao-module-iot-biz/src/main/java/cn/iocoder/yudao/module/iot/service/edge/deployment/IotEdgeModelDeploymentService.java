package cn.iocoder.yudao.module.iot.service.edge.deployment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment.IotEdgeModelDeploymentPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeModelDeploymentDO;

/**
 * IoT 边缘模型部署 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeModelDeploymentService {

    /**
     * 部署AI模型到网关
     *
     * @param modelId 模型编号
     * @param gatewayId 网关编号
     * @return 部署记录编号
     */
    Long deployModel(Long modelId, Long gatewayId);

    /**
     * 取消部署AI模型
     *
     * @param id 部署记录编号
     */
    void undeployModel(Long id);

    /**
     * 更新部署状态
     *
     * @param id 部署记录编号
     * @param status 状态: 0=停止, 1=运行
     */
    void updateDeploymentStatus(Long id, Integer status);

    /**
     * 获得模型部署记录
     *
     * @param id 编号
     * @return 模型部署记录
     */
    IotEdgeModelDeploymentDO getDeployment(Long id);

    /**
     * 获得模型部署记录分页
     *
     * @param pageReqVO 分页查询
     * @return 模型部署记录分页
     */
    PageResult<IotEdgeModelDeploymentDO> getDeploymentPage(IotEdgeModelDeploymentPageReqVO pageReqVO);

}
