package cn.iocoder.yudao.module.iot.gateway.protocol.modbus;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.manager.IotModbusConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.util.ModbusDataTypeConverter;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 Modbus 主站协议：主动读取 Modbus 从站设备数据
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusMasterProtocol {

    private final IotGatewayProperties.ModbusProperties modbusProperties;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotModbusConnectionManager connectionManager;

    @Getter
    private final String serverId;

    /**
     * 轮询调度器
     */
    private ScheduledExecutorService pollingScheduler;

    public IotModbusMasterProtocol(IotGatewayProperties.ModbusProperties modbusProperties,
                                   IotDeviceService deviceService,
                                   IotDeviceMessageService messageService,
                                   IotModbusConnectionManager connectionManager) {
        this.modbusProperties = modbusProperties;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.connectionManager = connectionManager;
        // 生成服务器 ID（使用 modbus 协议标识）
        this.serverId = IotDeviceMessageUtils.generateServerId(modbusProperties.getPort());
    }

    @PostConstruct
    public void start() {
        if (!Boolean.TRUE.equals(modbusProperties.getPollingEnabled())) {
            log.info("[start][Modbus 主站轮询未开启]");
            return;
        }

        if (modbusProperties.getSlaves() == null || modbusProperties.getSlaves().isEmpty()) {
            log.warn("[start][未配置 Modbus 从站设备]");
            return;
        }

        // 创建轮询调度器
        int slaveCount = modbusProperties.getSlaves().size();
        pollingScheduler = Executors.newScheduledThreadPool(
                Math.min(slaveCount, Runtime.getRuntime().availableProcessors()),
                r -> new Thread(r, "modbus-polling-" + Thread.currentThread().getId())
        );

        // 为每个从站设备启动轮询任务
        for (IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slave : modbusProperties.getSlaves()) {
            startPollingForSlave(slave);
        }

        log.info("[start][Modbus 主站协议启动成功，从站数量: {}，轮询间隔: {} ms]",
                slaveCount, modbusProperties.getPollingIntervalMs());
    }

    @PreDestroy
    public void stop() {
        if (pollingScheduler != null) {
            try {
                pollingScheduler.shutdown();
                if (!pollingScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    pollingScheduler.shutdownNow();
                }
                log.info("[stop][Modbus 主站协议已停止]");
            } catch (InterruptedException e) {
                pollingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("[stop][Modbus 主站协议停止失败]", e);
            }
        }

        // 关闭所有连接
        connectionManager.closeAll();
    }

    /**
     * 为从站设备启动轮询任务
     */
    private void startPollingForSlave(IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slave) {
        if (slave.getPollingConfigs() == null || slave.getPollingConfigs().isEmpty()) {
            log.warn("[startPollingForSlave][从站设备未配置轮询项，设备 ID: {}]", slave.getDeviceId());
            return;
        }

        // 定时轮询任务
        pollingScheduler.scheduleAtFixedRate(
                () -> pollSlaveDevice(slave),
                0,
                modbusProperties.getPollingIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        log.info("[startPollingForSlave][启动从站设备轮询，设备 ID: {}, 从站地址: {}, 轮询项数量: {}]",
                slave.getDeviceId(), slave.getSlaveId(), slave.getPollingConfigs().size());
    }

    /**
     * 轮询从站设备
     */
    private void pollSlaveDevice(IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slave) {
        try {
            // 获取连接参数
            String host = slave.getHost() != null ? slave.getHost() : modbusProperties.getHost();
            int port = slave.getPort() != null ? slave.getPort() : modbusProperties.getPort();
            int slaveId = slave.getSlaveId();

            // 存储所有读取的属性值
            Map<String, Object> properties = new HashMap<>();

            // 遍历所有轮询配置
            for (IotGatewayProperties.ModbusProperties.ModbusPollingConfig config : slave.getPollingConfigs()) {
                try {
                    Object value = readModbusData(host, port, slaveId, config);
                    if (value != null) {
                        properties.put(config.getIdentifier(), value);
                    }
                } catch (Exception e) {
                    log.error("[pollSlaveDevice][读取 Modbus 数据失败，设备 ID: {}, 标识符: {}, 功能码: {}, 地址: {}]",
                            slave.getDeviceId(), config.getIdentifier(), config.getFunctionCode(),
                            config.getStartAddress(), e);
                }
            }

            // 如果成功读取到数据，发布到消息总线
            if (!properties.isEmpty()) {
                publishDeviceMessage(slave, properties);
                connectionManager.updateDeviceStatus(slave.getDeviceId(), true);
            } else {
                connectionManager.updateDeviceStatus(slave.getDeviceId(), false);
            }

        } catch (Exception e) {
            log.error("[pollSlaveDevice][轮询从站设备失败，设备 ID: {}]", slave.getDeviceId(), e);
            connectionManager.updateDeviceStatus(slave.getDeviceId(), false);
        }
    }

    /**
     * 读取 Modbus 数据
     */
    private Object readModbusData(String host, int port, int slaveId,
                                  IotGatewayProperties.ModbusProperties.ModbusPollingConfig config) throws ModbusException {
        String functionCode = config.getFunctionCode();
        int startAddress = config.getStartAddress();
        int quantity = config.getQuantity();
        ModbusDataTypeConverter.DataType dataType = ModbusDataTypeConverter.parseDataType(config.getDataType());
        ModbusDataTypeConverter.ByteOrderType byteOrder = ModbusDataTypeConverter.parseByteOrder(config.getByteOrder());

        switch (functionCode) {
            case "READ_COILS":
                BitVector coils = connectionManager.readCoils(host, port, slaveId, startAddress, quantity);
                return coils.getBit(0); // 返回第一个线圈的值

            case "READ_DISCRETE_INPUTS":
                BitVector discreteInputs = connectionManager.readDiscreteInputs(host, port, slaveId, startAddress, quantity);
                return discreteInputs.getBit(0); // 返回第一个离散输入的值

            case "READ_HOLDING_REGISTERS":
                InputRegister[] holdingRegisters = connectionManager.readHoldingRegisters(host, port, slaveId, startAddress, quantity);
                return ModbusDataTypeConverter.convertFromRegisters(holdingRegisters, dataType, byteOrder);

            case "READ_INPUT_REGISTERS":
                InputRegister[] inputRegisters = connectionManager.readInputRegisters(host, port, slaveId, startAddress, quantity);
                return ModbusDataTypeConverter.convertFromRegisters(inputRegisters, dataType, byteOrder);

            default:
                log.warn("[readModbusData][不支持的功能码: {}]", functionCode);
                return null;
        }
    }

    /**
     * 发布设备消息到消息总线
     */
    private void publishDeviceMessage(IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slave,
                                      Map<String, Object> properties) {
        try {
            // 获取设备信息
            IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(slave.getDeviceId());
            if (deviceInfo == null) {
                log.error("[publishDeviceMessage][设备不存在，设备 ID: {}]", slave.getDeviceId());
                return;
            }

            // 构建设备消息
            IotDeviceMessage message = new IotDeviceMessage();
            message.setId(IotDeviceMessageUtils.generateMessageId());
            message.setReportTime(LocalDateTime.now());
            message.setDeviceId(slave.getDeviceId());
            message.setTenantId(deviceInfo.getTenantId());
            message.setServerId(serverId);
            message.setMethod("thing.property.report");
            message.setParams(properties);

            // 发布消息
            messageService.publishDeviceMessage(message, deviceInfo.getProductKey(), deviceInfo.getDeviceName());

            log.debug("[publishDeviceMessage][发布 Modbus 设备消息，设备 ID: {}，属性数量: {}]",
                    slave.getDeviceId(), properties.size());

        } catch (Exception e) {
            log.error("[publishDeviceMessage][发布设备消息失败，设备 ID: {}]", slave.getDeviceId(), e);
        }
    }

    /**
     * 手动触发读取设备数据（供外部调用）
     */
    public Map<String, Object> readDeviceData(Long deviceId) {
        IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slave = findSlaveDevice(deviceId);
        if (slave == null) {
            log.error("[readDeviceData][未找到设备的 Modbus 配置，设备 ID: {}]", deviceId);
            return null;
        }

        String host = slave.getHost() != null ? slave.getHost() : modbusProperties.getHost();
        int port = slave.getPort() != null ? slave.getPort() : modbusProperties.getPort();
        int slaveId = slave.getSlaveId();

        Map<String, Object> properties = new HashMap<>();
        for (IotGatewayProperties.ModbusProperties.ModbusPollingConfig config : slave.getPollingConfigs()) {
            try {
                Object value = readModbusData(host, port, slaveId, config);
                if (value != null) {
                    properties.put(config.getIdentifier(), value);
                }
            } catch (Exception e) {
                log.error("[readDeviceData][读取数据失败，设备 ID: {}, 标识符: {}]",
                        deviceId, config.getIdentifier(), e);
            }
        }

        return properties;
    }

    /**
     * 查找从站设备配置
     */
    private IotGatewayProperties.ModbusProperties.ModbusSlaveDevice findSlaveDevice(Long deviceId) {
        if (modbusProperties.getSlaves() == null) {
            return null;
        }
        return modbusProperties.getSlaves().stream()
                .filter(slave -> slave.getDeviceId().equals(deviceId))
                .findFirst()
                .orElse(null);
    }

}
