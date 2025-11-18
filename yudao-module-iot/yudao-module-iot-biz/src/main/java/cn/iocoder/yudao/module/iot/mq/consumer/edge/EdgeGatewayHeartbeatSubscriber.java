package cn.iocoder.yudao.module.iot.mq.consumer.edge;

import cn.iocoder.yudao.module.iot.core.edge.constant.EdgeMessageTopicConstants;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeGatewayHeartbeatDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayMapper;
import cn.iocoder.yudao.module.iot.service.edge.status.IotEdgeGatewayStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 边缘网关心跳消息订阅者
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class EdgeGatewayHeartbeatSubscriber implements IotMessageSubscriber<EdgeGatewayHeartbeatDTO> {

    @Resource
    private IotEdgeGatewayMapper edgeGatewayMapper;
    @Resource
    private IotEdgeGatewayStatusService gatewayStatusService;

    @Override
    public String getTopic() {
        return EdgeMessageTopicConstants.EDGE_GATEWAY_HEARTBEAT;
    }

    @Override
    public String getGroup() {
        return "edge-gateway-heartbeat-group";
    }

    @Override
    public void onMessage(EdgeGatewayHeartbeatDTO message) {
        log.info("[onMessage][收到网关心跳消息: gatewayKey={}, status={}]",
                message.getGatewayKey(), message.getStatus());

        try {
            // 1. 查找网关
            IotEdgeGatewayDO gateway = edgeGatewayMapper.selectByGatewayKey(message.getGatewayKey());
            if (gateway == null) {
                log.warn("[onMessage][网关不存在: gatewayKey={}]", message.getGatewayKey());
                return;
            }

            // 2. 更新网关状态
            gatewayStatusService.updateGatewayStatus(
                    gateway.getId(),
                    message.getStatus(),
                    message.getCpuUsage(),
                    message.getMemoryUsage(),
                    message.getDiskUsage()
            );

            // 3. 更新网关在线状态
            if (!gateway.getStatus().equals(message.getStatus())) {
                IotEdgeGatewayDO updateObj = IotEdgeGatewayDO.builder()
                        .id(gateway.getId())
                        .status(message.getStatus())
                        .build();
                edgeGatewayMapper.updateById(updateObj);
            }

            log.info("[onMessage][网关心跳处理成功: gatewayKey={}, gatewayId={}]",
                    message.getGatewayKey(), gateway.getId());

        } catch (Exception e) {
            log.error("[onMessage][网关心跳处理失败: gatewayKey={}]", message.getGatewayKey(), e);
        }
    }

}
