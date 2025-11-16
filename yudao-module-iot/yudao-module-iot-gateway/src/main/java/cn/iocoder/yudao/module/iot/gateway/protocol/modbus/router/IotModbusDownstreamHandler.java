package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.router;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.manager.IotModbusConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.util.ModbusDataTypeConverter;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import com.ghgande.j2mod.modbus.procimg.Register;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT 网关 Modbus 下行消息处理器
 * 处理发送给 Modbus 设备的命令
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotModbusDownstreamHandler {

    private final IotDeviceService deviceService;
    private final IotModbusConnectionManager connectionManager;
    private final IotGatewayProperties.ModbusProperties modbusProperties;

    /**
     * 处理下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理 Modbus 下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 获取设备信息
            IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(message.getDeviceId());
            if (deviceInfo == null) {
                log.error("[handle][设备不存在，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 2. 查找设备的 Modbus 配置
            IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice = findSlaveDevice(message.getDeviceId());
            if (slaveDevice == null) {
                log.error("[handle][未找到设备的 Modbus 配置，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 3. 根据方法类型处理不同的命令
            String method = message.getMethod();
            if (method == null) {
                log.error("[handle][消息方法为空，设备 ID: {}]", message.getDeviceId());
                return;
            }

            switch (method) {
                case "thing.property.set":
                    handlePropertySet(message, slaveDevice);
                    break;
                case "thing.service.invoke":
                    handleServiceInvoke(message, slaveDevice);
                    break;
                case "modbus.write.register":
                    handleWriteRegister(message, slaveDevice);
                    break;
                case "modbus.write.coil":
                    handleWriteCoil(message, slaveDevice);
                    break;
                default:
                    log.warn("[handle][不支持的方法类型: {}，设备 ID: {}]", method, message.getDeviceId());
            }

        } catch (Exception e) {
            log.error("[handle][处理 Modbus 下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

    /**
     * 处理属性设置
     */
    private void handlePropertySet(IotDeviceMessage message, IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice) {
        try {
            if (!(message.getParams() instanceof Map)) {
                log.error("[handlePropertySet][参数格式错误，设备 ID: {}]", message.getDeviceId());
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();

            String host = slaveDevice.getHost() != null ? slaveDevice.getHost() : modbusProperties.getHost();
            int port = slaveDevice.getPort() != null ? slaveDevice.getPort() : modbusProperties.getPort();
            int slaveId = slaveDevice.getSlaveId();

            // 遍历所有属性
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String identifier = entry.getKey();
                Object value = entry.getValue();

                // 查找对应的轮询配置
                IotGatewayProperties.ModbusProperties.ModbusPollingConfig config = findPollingConfigByIdentifier(slaveDevice, identifier);
                if (config == null) {
                    log.warn("[handlePropertySet][未找到属性的 Modbus 配置，设备 ID: {}，属性: {}]", message.getDeviceId(), identifier);
                    continue;
                }

                // 转换数据类型
                ModbusDataTypeConverter.DataType dataType = ModbusDataTypeConverter.parseDataType(config.getDataType());
                ModbusDataTypeConverter.ByteOrderType byteOrder = ModbusDataTypeConverter.parseByteOrder(config.getByteOrder());
                Register[] registers = ModbusDataTypeConverter.convertToRegisters(value, dataType, byteOrder);

                // 写入寄存器
                if (registers.length == 1) {
                    connectionManager.writeSingleRegister(host, port, slaveId, config.getStartAddress(), registers[0].getValue());
                } else {
                    connectionManager.writeMultipleRegisters(host, port, slaveId, config.getStartAddress(), registers);
                }

                log.info("[handlePropertySet][写入属性成功，设备 ID: {}，属性: {}，值: {}]",
                        message.getDeviceId(), identifier, value);
            }

            // 更新设备在线状态
            connectionManager.updateDeviceStatus(message.getDeviceId(), true);

        } catch (Exception e) {
            log.error("[handlePropertySet][处理属性设置失败，设备 ID: {}]", message.getDeviceId(), e);
            connectionManager.updateDeviceStatus(message.getDeviceId(), false);
        }
    }

    /**
     * 处理服务调用
     */
    private void handleServiceInvoke(IotDeviceMessage message, IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice) {
        try {
            if (!(message.getParams() instanceof Map)) {
                log.error("[handleServiceInvoke][参数格式错误，设备 ID: {}]", message.getDeviceId());
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();

            // 从参数中获取服务相关信息
            String identifier = (String) params.get("identifier");
            Object args = params.get("args");

            log.info("[handleServiceInvoke][调用服务，设备 ID: {}，服务: {}，参数: {}]",
                    message.getDeviceId(), identifier, args);

            // 这里可以根据具体的服务标识符执行不同的 Modbus 操作
            // 示例：如果服务是重启设备，可能需要写入特定的寄存器值

        } catch (Exception e) {
            log.error("[handleServiceInvoke][处理服务调用失败，设备 ID: {}]", message.getDeviceId(), e);
        }
    }

    /**
     * 处理写寄存器命令
     */
    private void handleWriteRegister(IotDeviceMessage message, IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice) {
        try {
            if (!(message.getParams() instanceof Map)) {
                log.error("[handleWriteRegister][参数格式错误，设备 ID: {}]", message.getDeviceId());
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();

            String host = slaveDevice.getHost() != null ? slaveDevice.getHost() : modbusProperties.getHost();
            int port = slaveDevice.getPort() != null ? slaveDevice.getPort() : modbusProperties.getPort();
            int slaveId = slaveDevice.getSlaveId();
            int address = ((Number) params.get("address")).intValue();
            Object value = params.get("value");
            String dataType = (String) params.getOrDefault("dataType", "INT16");
            String byteOrder = (String) params.getOrDefault("byteOrder", "BIG_ENDIAN");

            // 转换数据类型
            ModbusDataTypeConverter.DataType type = ModbusDataTypeConverter.parseDataType(dataType);
            ModbusDataTypeConverter.ByteOrderType order = ModbusDataTypeConverter.parseByteOrder(byteOrder);
            Register[] registers = ModbusDataTypeConverter.convertToRegisters(value, type, order);

            // 写入寄存器
            if (registers.length == 1) {
                connectionManager.writeSingleRegister(host, port, slaveId, address, registers[0].getValue());
            } else {
                connectionManager.writeMultipleRegisters(host, port, slaveId, address, registers);
            }

            log.info("[handleWriteRegister][写入寄存器成功，设备 ID: {}，地址: {}，值: {}]",
                    message.getDeviceId(), address, value);

            connectionManager.updateDeviceStatus(message.getDeviceId(), true);

        } catch (Exception e) {
            log.error("[handleWriteRegister][处理写寄存器失败，设备 ID: {}]", message.getDeviceId(), e);
            connectionManager.updateDeviceStatus(message.getDeviceId(), false);
        }
    }

    /**
     * 处理写线圈命令
     */
    private void handleWriteCoil(IotDeviceMessage message, IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice) {
        try {
            if (!(message.getParams() instanceof Map)) {
                log.error("[handleWriteCoil][参数格式错误，设备 ID: {}]", message.getDeviceId());
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();

            String host = slaveDevice.getHost() != null ? slaveDevice.getHost() : modbusProperties.getHost();
            int port = slaveDevice.getPort() != null ? slaveDevice.getPort() : modbusProperties.getPort();
            int slaveId = slaveDevice.getSlaveId();
            int address = ((Number) params.get("address")).intValue();
            boolean value = (Boolean) params.get("value");

            // 写入线圈
            connectionManager.writeSingleCoil(host, port, slaveId, address, value);

            log.info("[handleWriteCoil][写入线圈成功，设备 ID: {}，地址: {}，值: {}]",
                    message.getDeviceId(), address, value);

            connectionManager.updateDeviceStatus(message.getDeviceId(), true);

        } catch (Exception e) {
            log.error("[handleWriteCoil][处理写线圈失败，设备 ID: {}]", message.getDeviceId(), e);
            connectionManager.updateDeviceStatus(message.getDeviceId(), false);
        }
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

    /**
     * 根据属性标识符查找轮询配置
     */
    private IotGatewayProperties.ModbusProperties.ModbusPollingConfig findPollingConfigByIdentifier(
            IotGatewayProperties.ModbusProperties.ModbusSlaveDevice slaveDevice, String identifier) {
        if (slaveDevice.getPollingConfigs() == null) {
            return null;
        }
        return slaveDevice.getPollingConfigs().stream()
                .filter(config -> identifier.equals(config.getIdentifier()))
                .findFirst()
                .orElse(null);
    }

}
