package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.router;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.modbus.IotModbusRtuDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.modbus.IotModbusTcpDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * Modbus 上行消息处理器
 * <p>
 * 处理来自 Modbus 设备的请求，并转发到 IoT 平台处理
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusUpstreamHandler implements Handler<NetSocket> {

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final String serverId;

    private final boolean isTcpMode;

    public IotModbusUpstreamHandler(Object protocol,
                                    IotDeviceMessageService deviceMessageService,
                                    IotDeviceService deviceService,
                                    boolean isTcpMode) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.isTcpMode = isTcpMode;
        // 获取 serverId
        try {
            this.serverId = (String) protocol.getClass().getMethod("getServerId").invoke(protocol);
        } catch (Exception e) {
            this.serverId = "MODBUS_" + (isTcpMode ? "TCP" : "RTU");
        }
    }

    @Override
    public void handle(NetSocket socket) {
        String clientId = IdUtil.simpleUUID();
        log.debug("[handle][Modbus 设备连接，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());

        // 设置异常和关闭处理器
        socket.exceptionHandler(ex -> {
            log.warn("[handle][Modbus 连接异常，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress(), ex);
            socket.close();
        });
        socket.closeHandler(v -> {
            log.debug("[handle][Modbus 连接关闭，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());
        });

        // 设置消息处理器
        socket.handler(buffer -> {
            try {
                processModbusMessage(clientId, buffer, socket);
            } catch (Exception e) {
                log.error("[handle][Modbus 消息处理失败，客户端 ID: {}，地址: {}，错误: {}]",
                        clientId, socket.remoteAddress(), e.getMessage(), e);
                // Modbus 协议中，如果处理失败，可以选择关闭连接或发送异常响应
                // 这里选择继续保持连接，因为可能只是单个消息有问题
            }
        });
    }

    /**
     * 处理 Modbus 消息
     *
     * @param clientId 客户端 ID
     * @param buffer   消息
     * @param socket   网络连接
     */
    private void processModbusMessage(String clientId, Buffer buffer, NetSocket socket) {
        // 1. 基础检查
        if (buffer == null || buffer.length() == 0) {
            return;
        }

        // 2. 确定编解码器类型
        String codecType = isTcpMode ? IotModbusTcpDeviceMessageCodec.TYPE : IotModbusRtuDeviceMessageCodec.TYPE;

        // 3. 解码消息
        IotDeviceMessage message;
        try {
            message = deviceMessageService.decodeDeviceMessage(buffer.getBytes(), codecType);
            if (message == null) {
                log.warn("[processModbusMessage][解码后消息为空，客户端 ID: {}]", clientId);
                return;
            }
            log.debug("[processModbusMessage][收到 Modbus 消息，客户端 ID: {}，方法: {}]",
                    clientId, message.getMethod());
        } catch (Exception e) {
            log.error("[processModbusMessage][Modbus 消息解码失败，客户端 ID: {}]", clientId, e);
            // 发送 Modbus 异常响应
            sendModbusException(socket, buffer, (byte) 0x01, codecType); // 0x01 = 非法功能
            return;
        }

        // 4. 设置服务器 ID
        message.setServerId(serverId);

        // 5. 处理业务消息
        try {
            // 对于 Modbus，我们直接发布消息到消息总线，让业务层处理
            // 这里可以根据实际需求添加设备认证等逻辑
            deviceMessageService.publishDeviceMessage(message);
            log.debug("[processModbusMessage][Modbus 消息已发布，客户端 ID: {}，消息 ID: {}]",
                    clientId, message.getId());

            // 如果需要响应，业务层会通过下行消息订阅器发送
            // Modbus 协议通常需要立即响应，这里可以选择同步等待或异步处理
            // 简化实现：直接发送成功响应
            if (message.getMethod().equals("modbus.read") || message.getMethod().equals("modbus.write")) {
                // 发送默认成功响应（实际应用中应该等待业务层处理结果）
                sendModbusSuccessResponse(socket, message, codecType);
            }
        } catch (Exception e) {
            log.error("[processModbusMessage][Modbus 消息处理失败，客户端 ID: {}，消息 ID: {}]",
                    clientId, message.getId(), e);
            // 发送 Modbus 异常响应
            sendModbusException(socket, buffer, (byte) 0x04, codecType); // 0x04 = 从站设备故障
        }
    }

    /**
     * 发送 Modbus 异常响应
     *
     * @param socket        网络连接
     * @param requestBuffer 请求数据
     * @param exceptionCode 异常码
     * @param codecType     编解码器类型
     */
    private void sendModbusException(NetSocket socket, Buffer requestBuffer, byte exceptionCode, String codecType) {
        try {
            // 解析请求以获取功能码等信息（简化实现）
            byte[] requestBytes = requestBuffer.getBytes();
            if (requestBytes.length < 2) {
                return;
            }

            // 构建异常响应消息
            IotDeviceMessage exceptionMessage = IotDeviceMessage.of(
                    "error-" + IdUtil.simpleUUID(),
                    "modbus.error",
                    null,
                    null,
                    (int) exceptionCode,
                    "Modbus 异常"
            );

            // 编码并发送
            byte[] responseBytes = deviceMessageService.encodeDeviceMessage(exceptionMessage, codecType);
            socket.write(Buffer.buffer(responseBytes));
            log.debug("[sendModbusException][已发送 Modbus 异常响应，异常码: {}]", exceptionCode);
        } catch (Exception e) {
            log.error("[sendModbusException][发送 Modbus 异常响应失败]", e);
        }
    }

    /**
     * 发送 Modbus 成功响应
     *
     * @param socket    网络连接
     * @param request   请求消息
     * @param codecType 编解码器类型
     */
    private void sendModbusSuccessResponse(NetSocket socket, IotDeviceMessage request, String codecType) {
        try {
            // 构建成功响应消息（简化实现，实际应该根据请求类型构建不同的响应）
            IotDeviceMessage responseMessage = IotDeviceMessage.of(
                    request.getRequestId(),
                    request.getMethod(),
                    request.getParams(),
                    null, // 数据应该由业务层提供
                    0,    // 成功
                    "成功"
            );

            // 编码并发送
            byte[] responseBytes = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.write(Buffer.buffer(responseBytes));
            log.debug("[sendModbusSuccessResponse][已发送 Modbus 成功响应，消息 ID: {}]", request.getId());
        } catch (Exception e) {
            log.error("[sendModbusSuccessResponse][发送 Modbus 成功响应失败]", e);
        }
    }

}
