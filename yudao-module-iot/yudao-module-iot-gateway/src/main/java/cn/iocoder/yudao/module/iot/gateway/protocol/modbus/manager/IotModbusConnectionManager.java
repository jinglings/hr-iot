package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.manager;

import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modbus 连接管理器
 * 管理 Modbus TCP/RTU 主站连接
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusConnectionManager {

    private final IotGatewayProperties.ModbusProperties modbusProperties;

    /**
     * TCP 连接池: key = host:port, value = ModbusTCPMaster
     */
    private final Map<String, ModbusTCPMaster> tcpMasterMap = new ConcurrentHashMap<>();

    /**
     * RTU 连接池: key = serialPort, value = ModbusSerialMaster
     */
    private final Map<String, ModbusSerialMaster> rtuMasterMap = new ConcurrentHashMap<>();

    /**
     * 设备连接状态: key = deviceId, value = isOnline
     */
    private final Map<Long, Boolean> deviceStatusMap = new ConcurrentHashMap<>();

    public IotModbusConnectionManager(IotGatewayProperties.ModbusProperties modbusProperties) {
        this.modbusProperties = modbusProperties;
    }

    /**
     * 获取或创建 TCP Master
     */
    public ModbusTCPMaster getTcpMaster(String host, int port) {
        String key = host + ":" + port;
        return tcpMasterMap.computeIfAbsent(key, k -> {
            try {
                ModbusTCPMaster master = new ModbusTCPMaster(host, port);
                master.setTimeout(modbusProperties.getReadTimeoutMs());
                master.setReconnecting(true);
                master.connect();
                log.info("[getTcpMaster][创建 Modbus TCP 连接成功，地址: {}:{}]", host, port);
                return master;
            } catch (Exception e) {
                log.error("[getTcpMaster][创建 Modbus TCP 连接失败，地址: {}:{}]", host, port, e);
                return null;
            }
        });
    }

    /**
     * 获取或创建 RTU Master
     */
    public ModbusSerialMaster getRtuMaster(String serialPort) {
        return rtuMasterMap.computeIfAbsent(serialPort, k -> {
            try {
                SerialParameters params = new SerialParameters();
                params.setPortName(serialPort);
                params.setBaudRate(modbusProperties.getBaudRate());
                params.setDatabits(modbusProperties.getDataBits());
                params.setStopbits(modbusProperties.getStopBits());
                params.setParity(modbusProperties.getParity());
                params.setEncoding("rtu");
                params.setEcho(false);

                ModbusSerialMaster master = new ModbusSerialMaster(params);
                master.setTimeout(modbusProperties.getReadTimeoutMs());
                master.connect();
                log.info("[getRtuMaster][创建 Modbus RTU 连接成功，串口: {}]", serialPort);
                return master;
            } catch (Exception e) {
                log.error("[getRtuMaster][创建 Modbus RTU 连接失败，串口: {}]", serialPort, e);
                return null;
            }
        });
    }

    /**
     * 读取线圈（功能码 01）
     */
    public BitVector readCoils(String host, int port, int slaveId, int startAddress, int quantity) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            return master.readCoils(slaveId, startAddress, quantity);
        } catch (ModbusException e) {
            log.error("[readCoils][读取线圈失败，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, quantity, e);
            throw e;
        }
    }

    /**
     * 读取离散输入（功能码 02）
     */
    public BitVector readDiscreteInputs(String host, int port, int slaveId, int startAddress, int quantity) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            return master.readInputDiscretes(slaveId, startAddress, quantity);
        } catch (ModbusException e) {
            log.error("[readDiscreteInputs][读取离散输入失败，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, quantity, e);
            throw e;
        }
    }

    /**
     * 读取保持寄存器（功能码 03）
     */
    public InputRegister[] readHoldingRegisters(String host, int port, int slaveId, int startAddress, int quantity) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            return master.readMultipleRegisters(slaveId, startAddress, quantity);
        } catch (ModbusException e) {
            log.error("[readHoldingRegisters][读取保持寄存器失败，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, quantity, e);
            throw e;
        }
    }

    /**
     * 读取输入寄存器（功能码 04）
     */
    public InputRegister[] readInputRegisters(String host, int port, int slaveId, int startAddress, int quantity) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            return master.readInputRegisters(slaveId, startAddress, quantity);
        } catch (ModbusException e) {
            log.error("[readInputRegisters][读取输入寄存器失败，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, quantity, e);
            throw e;
        }
    }

    /**
     * 写单个线圈（功能码 05）
     */
    public void writeSingleCoil(String host, int port, int slaveId, int address, boolean value) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            master.writeCoil(slaveId, address, value);
            log.debug("[writeSingleCoil][写单个线圈成功，地址: {}:{}, 从站: {}, 地址: {}, 值: {}]",
                    host, port, slaveId, address, value);
        } catch (ModbusException e) {
            log.error("[writeSingleCoil][写单个线圈失败，地址: {}:{}, 从站: {}, 地址: {}, 值: {}]",
                    host, port, slaveId, address, value, e);
            throw e;
        }
    }



    /**
     * 写单个寄存器（功能码 06）
     */
    public void writeSingleRegister(String host, int port, int slaveId, int address, int value) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            // 需要将 int 值包装成 Register 类型
            master.writeSingleRegister(slaveId, address, new SimpleRegister(value));
            log.debug("[writeSingleRegister][写单个寄存器成功，地址: {}:{}, 从站: {}, 地址: {}, 值: {}]",
                    host, port, slaveId, address, value);
        } catch (ModbusException e) {
            log.error("[writeSingleRegister][写单个寄存器失败，地址: {}:{}, 从站: {}, 地址: {}, 值: {}]",
                    host, port, slaveId, address, value, e);
            throw e;
        }
    }

    /**
     * 写多个线圈（功能码 15）
     */
    public void writeMultipleCoils(String host, int port, int slaveId, int startAddress, BitVector coils) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            master.writeMultipleCoils(slaveId, startAddress, coils);
            log.debug("[writeMultipleCoils][写多个线圈成功，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, coils.size());
        } catch (ModbusException e) {
            log.error("[writeMultipleCoils][写多个线圈失败，地址: {}:{}, 从站: {}, 起始: {}]",
                    host, port, slaveId, startAddress, e);
            throw e;
        }
    }

    /**
     * 写多个寄存器（功能码 16）
     */
    public void writeMultipleRegisters(String host, int port, int slaveId, int startAddress, Register[] registers) throws ModbusException {
        ModbusTCPMaster master = getTcpMaster(host, port);
        if (master == null) {
            throw new ModbusException("Modbus TCP 连接失败");
        }
        try {
            master.writeMultipleRegisters(slaveId, startAddress, registers);
            log.debug("[writeMultipleRegisters][写多个寄存器成功，地址: {}:{}, 从站: {}, 起始: {}, 数量: {}]",
                    host, port, slaveId, startAddress, registers.length);
        } catch (ModbusException e) {
            log.error("[writeMultipleRegisters][写多个寄存器失败，地址: {}:{}, 从站: {}, 起始: {}]",
                    host, port, slaveId, startAddress, e);
            throw e;
        }
    }

    /**
     * 检查设备是否离线
     */
    public boolean isDeviceOffline(Long deviceId) {
        return !deviceStatusMap.getOrDefault(deviceId, false);
    }

    /**
     * 更新设备在线状态
     */
    public void updateDeviceStatus(Long deviceId, boolean isOnline) {
        deviceStatusMap.put(deviceId, isOnline);
        log.debug("[updateDeviceStatus][设备状态更新，设备 ID: {}, 在线: {}]", deviceId, isOnline);
    }

    /**
     * 获取设备在线状态
     */
    public boolean getDeviceStatus(Long deviceId) {
        return deviceStatusMap.getOrDefault(deviceId, false);
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        // 关闭所有 TCP 连接
        tcpMasterMap.forEach((key, master) -> {
            try {
                if (master != null) {
                    master.disconnect();
                    log.info("[closeAll][关闭 Modbus TCP 连接: {}]", key);
                }
            } catch (Exception e) {
                log.error("[closeAll][关闭 Modbus TCP 连接失败: {}]", key, e);
            }
        });
        tcpMasterMap.clear();

        // 关闭所有 RTU 连接
        rtuMasterMap.forEach((key, master) -> {
            try {
                if (master != null) {
                    master.disconnect();
                    log.info("[closeAll][关闭 Modbus RTU 连接: {}]", key);
                }
            } catch (Exception e) {
                log.error("[closeAll][关闭 Modbus RTU 连接失败: {}]", key, e);
            }
        });
        rtuMasterMap.clear();

        deviceStatusMap.clear();
    }

    /**
     * 重连指定的 TCP 连接
     */
    public void reconnectTcp(String host, int port) {
        String key = host + ":" + port;
        ModbusTCPMaster master = tcpMasterMap.remove(key);
        if (master != null) {
            try {
                master.disconnect();
            } catch (Exception e) {
                log.warn("[reconnectTcp][断开旧连接失败: {}]", key, e);
            }
        }
        // 重新获取连接将自动创建新连接
        getTcpMaster(host, port);
    }

}
