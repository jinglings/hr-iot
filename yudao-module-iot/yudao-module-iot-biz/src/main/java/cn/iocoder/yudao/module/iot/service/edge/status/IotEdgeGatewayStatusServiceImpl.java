package cn.iocoder.yudao.module.iot.service.edge.status;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status.IotEdgeGatewayStatusPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayStatusDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayStatusMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * IoT 边缘网关状态 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
public class IotEdgeGatewayStatusServiceImpl implements IotEdgeGatewayStatusService {

    @Resource
    private IotEdgeGatewayStatusMapper gatewayStatusMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGatewayStatus(Long gatewayId, Integer status, Integer cpuUsage,
                                   Integer memoryUsage, Integer diskUsage) {
        IotEdgeGatewayStatusDO existStatus = gatewayStatusMapper.selectByGatewayId(gatewayId);

        if (existStatus == null) {
            // 首次上报，创建记录
            IotEdgeGatewayStatusDO statusDO = IotEdgeGatewayStatusDO.builder()
                    .gatewayId(gatewayId)
                    .status(status)
                    .cpuUsage(cpuUsage)
                    .memoryUsage(memoryUsage)
                    .diskUsage(diskUsage)
                    .lastHeartbeatTime(LocalDateTime.now())
                    .build();
            gatewayStatusMapper.insert(statusDO);
        } else {
            // 更新状态
            IotEdgeGatewayStatusDO updateObj = IotEdgeGatewayStatusDO.builder()
                    .id(existStatus.getId())
                    .status(status)
                    .cpuUsage(cpuUsage)
                    .memoryUsage(memoryUsage)
                    .diskUsage(diskUsage)
                    .lastHeartbeatTime(LocalDateTime.now())
                    .build();
            gatewayStatusMapper.updateById(updateObj);
        }
    }

    @Override
    public IotEdgeGatewayStatusDO getGatewayStatus(Long id) {
        return gatewayStatusMapper.selectById(id);
    }

    @Override
    public IotEdgeGatewayStatusDO getGatewayStatusByGatewayId(Long gatewayId) {
        return gatewayStatusMapper.selectByGatewayId(gatewayId);
    }

    @Override
    public PageResult<IotEdgeGatewayStatusDO> getGatewayStatusPage(IotEdgeGatewayStatusPageReqVO pageReqVO) {
        return gatewayStatusMapper.selectPage(pageReqVO);
    }

}
