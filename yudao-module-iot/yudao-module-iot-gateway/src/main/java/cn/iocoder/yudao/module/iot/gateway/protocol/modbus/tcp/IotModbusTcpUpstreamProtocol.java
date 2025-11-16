package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcp;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.router.IotModbusUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * IoT 网关 Modbus TCP 协议：接收设备上行消息
 * <p>
 * Modbus TCP 协议是工业自动化领域广泛使用的通信协议，支持读取和写入设备寄存器、线圈等数据。
 * 本实现作为 Modbus TCP 从站（Slave），接收来自主站（Master）的请求，并转发到 IoT 平台处理。
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpUpstreamProtocol {

    private final IotGatewayProperties.ModbusTcpProperties modbusTcpProperties;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    private NetServer tcpServer;

    public IotModbusTcpUpstreamProtocol(IotGatewayProperties.ModbusTcpProperties modbusTcpProperties,
                                        IotDeviceService deviceService,
                                        IotDeviceMessageService messageService,
                                        Vertx vertx) {
        this.modbusTcpProperties = modbusTcpProperties;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId("MODBUS_TCP", modbusTcpProperties.getPort());
    }

    @PostConstruct
    public void start() {
        // 创建服务器选项
        NetServerOptions options = new NetServerOptions()
                .setPort(modbusTcpProperties.getPort())
                .setTcpKeepAlive(true)
                .setTcpNoDelay(true)
                .setReuseAddress(true);

        // 创建服务器并设置连接处理器
        tcpServer = vertx.createNetServer(options);
        tcpServer.connectHandler(socket -> {
            IotModbusUpstreamHandler handler = new IotModbusUpstreamHandler(
                    this, messageService, deviceService, true); // true = TCP mode
            handler.handle(socket);
        });

        // 启动服务器
        try {
            tcpServer.listen().result();
            log.info("[start][IoT 网关 Modbus TCP 协议启动成功，端口：{}]", modbusTcpProperties.getPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 Modbus TCP 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (tcpServer != null) {
            try {
                tcpServer.close().result();
                log.info("[stop][IoT 网关 Modbus TCP 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 Modbus TCP 协议停止失败]", e);
            }
        }
    }

}
