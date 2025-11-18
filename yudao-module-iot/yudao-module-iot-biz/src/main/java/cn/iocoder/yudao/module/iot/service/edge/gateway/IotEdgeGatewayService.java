package cn.iocoder.yudao.module.iot.service.edge.gateway;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;

import javax.validation.Valid;

/**
 * IoT 边缘网关 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeGatewayService {

    /**
     * 创建边缘网关
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGateway(@Valid IotEdgeGatewayCreateReqVO createReqVO);

    /**
     * 更新边缘网关
     *
     * @param updateReqVO 更新信息
     */
    void updateGateway(@Valid IotEdgeGatewayUpdateReqVO updateReqVO);

    /**
     * 删除边缘网关
     *
     * @param id 编号
     */
    void deleteGateway(Long id);

    /**
     * 获得边缘网关
     *
     * @param id 编号
     * @return 边缘网关
     */
    IotEdgeGatewayDO getGateway(Long id);

    /**
     * 获得边缘网关分页
     *
     * @param pageReqVO 分页查询
     * @return 边缘网关分页
     */
    PageResult<IotEdgeGatewayDO> getGatewayPage(IotEdgeGatewayPageReqVO pageReqVO);

    /**
     * 更新网关状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateGatewayStatus(Long id, Integer status);

}
