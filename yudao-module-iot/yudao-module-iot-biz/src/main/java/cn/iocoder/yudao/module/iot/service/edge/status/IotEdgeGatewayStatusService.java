package cn.iocoder.yudao.module.iot.service.edge.status;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status.IotEdgeGatewayStatusPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayStatusDO;

/**
 * IoT 边缘网关状态 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeGatewayStatusService {

    /**
     * 更新网关状态
     *
     * @param gatewayId 网关编号
     * @param status 状态
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @param diskUsage 磁盘使用率
     */
    void updateGatewayStatus(Long gatewayId, Integer status, Integer cpuUsage,
                            Integer memoryUsage, Integer diskUsage);

    /**
     * 获得网关状态
     *
     * @param id 编号
     * @return 网关状态
     */
    IotEdgeGatewayStatusDO getGatewayStatus(Long id);

    /**
     * 根据网关ID获得状态
     *
     * @param gatewayId 网关编号
     * @return 网关状态
     */
    IotEdgeGatewayStatusDO getGatewayStatusByGatewayId(Long gatewayId);

    /**
     * 获得网关状态分页
     *
     * @param pageReqVO 分页查询
     * @return 网关状态分页
     */
    PageResult<IotEdgeGatewayStatusDO> getGatewayStatusPage(IotEdgeGatewayStatusPageReqVO pageReqVO);

}
