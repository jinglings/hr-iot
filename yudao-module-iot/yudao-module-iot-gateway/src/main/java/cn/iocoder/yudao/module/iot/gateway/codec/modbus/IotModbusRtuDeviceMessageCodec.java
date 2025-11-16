package cn.iocoder.yudao.module.iot.gateway.codec.modbus;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.iot.gateway.codec.modbus.ModbusConstants.*;

/**
 * Modbus RTU 格式 {@link IotDeviceMessage} 编解码器
 * <p>
 * Modbus RTU 协议格式：
 * <pre>
 * +----------------+----------------------------------+----------------+
 * |    Address     |           PDU (变长)              |   CRC-16       |
 * |   (1 byte)     | Function Code (1 byte) + Data    |   (2 bytes)    |
 * +----------------+----------------------------------+----------------+
 * </pre>
 * <p>
 * RTU 模式使用 CRC-16 校验，所有数据以二进制传输
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotModbusRtuDeviceMessageCodec implements IotDeviceMessageCodec {

    public static final String TYPE = "MODBUS_RTU";

    /**
     * Modbus RTU 最小包长度（地址 + 功能码 + CRC）
     */
    private static final int MIN_PACKET_LENGTH = 4;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        Assert.notNull(message, "消息不能为空");
        Assert.notBlank(message.getMethod(), "消息方法不能为空");
        try {
            Buffer buffer = Buffer.buffer();

            // 获取从站地址
            int unitId = getUnitId(message);
            buffer.appendUnsignedByte((short) unitId);

            // 写入 PDU
            if (message.getCode() != null) {
                // 响应消息
                encodeModbusResponse(buffer, message);
            } else {
                // 请求消息
                encodeModbusRequest(buffer, message);
            }

            // 计算并添加 CRC
            byte[] data = buffer.getBytes();
            int crc = calculateCrc(data);
            buffer.appendUnsignedByte((short) (crc & 0xFF));          // CRC Low
            buffer.appendUnsignedByte((short) ((crc >> 8) & 0xFF));   // CRC High

            return buffer.getBytes();
        } catch (Exception e) {
            log.error("[encode][Modbus RTU 消息编码失败，消息: {}]", message, e);
            throw new RuntimeException("Modbus RTU 消息编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        Assert.notNull(bytes, "待解码数据不能为空");
        Assert.isTrue(bytes.length >= MIN_PACKET_LENGTH, "Modbus RTU 数据包长度不足");
        try {
            Buffer buffer = Buffer.buffer(bytes);

            // 1. 验证 CRC
            byte[] dataWithoutCrc = new byte[bytes.length - 2];
            System.arraycopy(bytes, 0, dataWithoutCrc, 0, dataWithoutCrc.length);
            int calculatedCrc = calculateCrc(dataWithoutCrc);
            int receivedCrc = (bytes[bytes.length - 2] & 0xFF) | ((bytes[bytes.length - 1] & 0xFF) << 8);
            Assert.isTrue(calculatedCrc == receivedCrc,
                    String.format("CRC 校验失败，期望: 0x%04X, 实际: 0x%04X", calculatedCrc, receivedCrc));

            // 2. 解析地址和功能码
            int index = 0;
            int unitId = buffer.getUnsignedByte(index);
            index += 1;

            byte functionCode = buffer.getByte(index);
            index += 1;

            // 判断是否为异常响应
            boolean isException = (functionCode & EXCEPTION_FLAG) != 0;

            // 3. 生成消息 ID
            String messageId = String.format("modbus-rtu-%02X-%d", unitId, System.currentTimeMillis() % 10000);

            if (isException) {
                // 异常响应
                return decodeExceptionResponse(buffer, index, messageId, functionCode, unitId);
            } else if (isReadFunction(functionCode)) {
                // 读取响应或请求
                return decodeReadMessage(buffer, index, messageId, functionCode, unitId, bytes.length - 2);
            } else if (isWriteFunction(functionCode)) {
                // 写入响应或请求
                return decodeWriteMessage(buffer, index, messageId, functionCode, unitId, bytes.length - 2);
            } else {
                log.warn("[decode][不支持的 Modbus 功能码: {}]", functionCode);
                return createErrorMessage(messageId, functionCode, unitId, "不支持的功能码");
            }
        } catch (Exception e) {
            log.error("[decode][Modbus RTU 消息解码失败，数据长度: {}]", bytes.length, e);
            throw new RuntimeException("Modbus RTU 消息解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 编码 Modbus 请求
     */
    private void encodeModbusRequest(Buffer buffer, IotDeviceMessage message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) message.getParams();
        Assert.notNull(params, "请求参数不能为空");

        int functionCode = getIntParam(params, PARAM_FUNCTION_CODE);
        buffer.appendUnsignedByte((short) functionCode);

        if (isReadFunction((byte) functionCode)) {
            // 读取请求：起始地址 + 数量
            int startAddress = getIntParam(params, PARAM_START_ADDRESS);
            int quantity = getIntParam(params, PARAM_QUANTITY);
            buffer.appendUnsignedShort(startAddress);
            buffer.appendUnsignedShort(quantity);
        } else if (functionCode == FUNCTION_CODE_WRITE_SINGLE_COIL ||
                   functionCode == FUNCTION_CODE_WRITE_SINGLE_REGISTER) {
            // 写单个：地址 + 值
            int address = getIntParam(params, PARAM_START_ADDRESS);
            int value = getIntParam(params, PARAM_VALUE);
            buffer.appendUnsignedShort(address);
            buffer.appendUnsignedShort(value);
        } else if (functionCode == FUNCTION_CODE_WRITE_MULTIPLE_COILS ||
                   functionCode == FUNCTION_CODE_WRITE_MULTIPLE_REGISTERS) {
            // 写多个：地址 + 数量 + 字节数 + 值
            int startAddress = getIntParam(params, PARAM_START_ADDRESS);
            @SuppressWarnings("unchecked")
            List<Integer> values = (List<Integer>) params.get(PARAM_VALUES);
            Assert.notNull(values, "写入值不能为空");

            buffer.appendUnsignedShort(startAddress);
            buffer.appendUnsignedShort(values.size());

            if (functionCode == FUNCTION_CODE_WRITE_MULTIPLE_REGISTERS) {
                buffer.appendUnsignedByte((short) (values.size() * 2)); // 字节数
                for (Integer value : values) {
                    buffer.appendUnsignedShort(value);
                }
            } else {
                // 线圈数据需要打包成字节
                int byteCount = (values.size() + 7) / 8;
                buffer.appendUnsignedByte((short) byteCount);
                byte[] coilBytes = packCoils(values);
                buffer.appendBytes(coilBytes);
            }
        }
    }

    /**
     * 编码 Modbus 响应
     */
    private void encodeModbusResponse(Buffer buffer, IotDeviceMessage message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) message.getParams();
        int functionCode = params != null ? getIntParam(params, PARAM_FUNCTION_CODE) : 0x03;

        if (message.getCode() != 0) {
            // 异常响应
            buffer.appendUnsignedByte((short) (functionCode | EXCEPTION_FLAG));
            buffer.appendUnsignedByte((short) message.getCode().intValue());
        } else {
            // 正常响应
            buffer.appendUnsignedByte((short) functionCode);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            if (data != null) {
                if (data.containsKey(DATA_REGISTERS)) {
                    // 寄存器数据
                    @SuppressWarnings("unchecked")
                    List<Integer> registers = (List<Integer>) data.get(DATA_REGISTERS);
                    buffer.appendUnsignedByte((short) (registers.size() * 2)); // 字节数
                    for (Integer register : registers) {
                        buffer.appendUnsignedShort(register);
                    }
                } else if (data.containsKey(DATA_COILS)) {
                    // 线圈数据
                    @SuppressWarnings("unchecked")
                    List<Integer> coils = (List<Integer>) data.get(DATA_COILS);
                    byte[] coilBytes = packCoils(coils);
                    buffer.appendUnsignedByte((short) coilBytes.length);
                    buffer.appendBytes(coilBytes);
                }
            }
        }
    }

    /**
     * 解码读取消息
     */
    private IotDeviceMessage decodeReadMessage(Buffer buffer, int index, String messageId,
                                               byte functionCode, int unitId, int dataLength) {
        int remainingLength = dataLength - index;

        if (remainingLength >= 4) {
            // 可能是请求（起始地址 + 数量）
            int startAddress = buffer.getUnsignedShort(index);
            int quantity = buffer.getUnsignedShort(index + 2);

            Map<String, Object> params = new HashMap<>();
            params.put(PARAM_UNIT_ID, unitId);
            params.put(PARAM_FUNCTION_CODE, (int) functionCode);
            params.put(PARAM_START_ADDRESS, startAddress);
            params.put(PARAM_QUANTITY, quantity);

            return IotDeviceMessage.of(messageId, METHOD_READ, params, null, null, null);
        } else if (remainingLength >= 1) {
            // 可能是响应（字节数 + 数据）
            int byteCount = buffer.getUnsignedByte(index);
            index += 1;

            Map<String, Object> data = new HashMap<>();
            if (functionCode == FUNCTION_CODE_READ_COILS ||
                functionCode == FUNCTION_CODE_READ_DISCRETE_INPUTS) {
                // 线圈/离散输入数据
                List<Integer> coils = new ArrayList<>();
                for (int i = 0; i < byteCount; i++) {
                    byte coilByte = buffer.getByte(index + i);
                    for (int bit = 0; bit < 8; bit++) {
                        coils.add((coilByte >> bit) & 0x01);
                    }
                }
                data.put(DATA_COILS, coils);
            } else {
                // 寄存器数据
                List<Integer> registers = new ArrayList<>();
                for (int i = 0; i < byteCount; i += 2) {
                    registers.add(buffer.getUnsignedShort(index + i));
                }
                data.put(DATA_REGISTERS, registers);
            }

            Map<String, Object> params = new HashMap<>();
            params.put(PARAM_UNIT_ID, unitId);
            params.put(PARAM_FUNCTION_CODE, (int) functionCode);

            return IotDeviceMessage.of(messageId, METHOD_READ, params, data, 0, "成功");
        }

        return createErrorMessage(messageId, functionCode, unitId, "无效的读取消息格式");
    }

    /**
     * 解码写入消息
     */
    private IotDeviceMessage decodeWriteMessage(Buffer buffer, int index, String messageId,
                                                byte functionCode, int unitId, int dataLength) {
        int remainingLength = dataLength - index;

        if (functionCode == FUNCTION_CODE_WRITE_SINGLE_COIL ||
            functionCode == FUNCTION_CODE_WRITE_SINGLE_REGISTER) {
            // 写单个
            if (remainingLength >= 4) {
                int address = buffer.getUnsignedShort(index);
                int value = buffer.getUnsignedShort(index + 2);

                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_UNIT_ID, unitId);
                params.put(PARAM_FUNCTION_CODE, (int) functionCode);
                params.put(PARAM_START_ADDRESS, address);
                params.put(PARAM_VALUE, value);

                return IotDeviceMessage.of(messageId, METHOD_WRITE, params, null, 0, "成功");
            }
        } else if (functionCode == FUNCTION_CODE_WRITE_MULTIPLE_COILS ||
                   functionCode == FUNCTION_CODE_WRITE_MULTIPLE_REGISTERS) {
            // 写多个
            if (remainingLength >= 4) {
                int startAddress = buffer.getUnsignedShort(index);
                int quantity = buffer.getUnsignedShort(index + 2);

                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_UNIT_ID, unitId);
                params.put(PARAM_FUNCTION_CODE, (int) functionCode);
                params.put(PARAM_START_ADDRESS, startAddress);
                params.put(PARAM_QUANTITY, quantity);

                return IotDeviceMessage.of(messageId, METHOD_WRITE, params, null, 0, "成功");
            }
        }

        return createErrorMessage(messageId, functionCode, unitId, "无效的写入消息格式");
    }

    /**
     * 解码异常响应
     */
    private IotDeviceMessage decodeExceptionResponse(Buffer buffer, int index, String messageId,
                                                     byte functionCode, int unitId) {
        int exceptionCode = buffer.getUnsignedByte(index);

        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_UNIT_ID, unitId);
        params.put(PARAM_FUNCTION_CODE, (int) (functionCode & ~EXCEPTION_FLAG));

        Map<String, Object> data = new HashMap<>();
        data.put(DATA_EXCEPTION_CODE, exceptionCode);

        String method = isReadFunction((byte) (functionCode & ~EXCEPTION_FLAG)) ? METHOD_READ : METHOD_WRITE;
        return IotDeviceMessage.of(messageId, method, params, data, exceptionCode,
                "Modbus 异常: " + getExceptionDescription(exceptionCode));
    }

    /**
     * 创建错误消息
     */
    private IotDeviceMessage createErrorMessage(String messageId, byte functionCode, int unitId, String errorMsg) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_UNIT_ID, unitId);
        params.put(PARAM_FUNCTION_CODE, (int) functionCode);

        return IotDeviceMessage.of(messageId, METHOD_READ, params, null, -1, errorMsg);
    }

    /**
     * 判断是否为读取功能
     */
    private boolean isReadFunction(byte functionCode) {
        return functionCode == FUNCTION_CODE_READ_COILS ||
               functionCode == FUNCTION_CODE_READ_DISCRETE_INPUTS ||
               functionCode == FUNCTION_CODE_READ_HOLDING_REGISTERS ||
               functionCode == FUNCTION_CODE_READ_INPUT_REGISTERS;
    }

    /**
     * 判断是否为写入功能
     */
    private boolean isWriteFunction(byte functionCode) {
        return functionCode == FUNCTION_CODE_WRITE_SINGLE_COIL ||
               functionCode == FUNCTION_CODE_WRITE_SINGLE_REGISTER ||
               functionCode == FUNCTION_CODE_WRITE_MULTIPLE_COILS ||
               functionCode == FUNCTION_CODE_WRITE_MULTIPLE_REGISTERS;
    }

    /**
     * 获取 Unit ID
     */
    private int getUnitId(IotDeviceMessage message) {
        if (message.getParams() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            return getIntParam(params, PARAM_UNIT_ID, 1);
        }
        return 1;
    }

    /**
     * 从参数 Map 中获取整数值
     */
    private int getIntParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        Assert.notNull(value, "参数 " + key + " 不能为空");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 从参数 Map 中获取整数值（带默认值）
     */
    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 打包线圈数据
     */
    private byte[] packCoils(List<Integer> coils) {
        int byteCount = (coils.size() + 7) / 8;
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < coils.size(); i++) {
            if (coils.get(i) != 0) {
                bytes[i / 8] |= (byte) (1 << (i % 8));
            }
        }
        return bytes;
    }

    /**
     * 计算 CRC-16 (Modbus)
     * <p>
     * Modbus RTU 使用 CRC-16-ANSI 算法，多项式为 0xA001（反向）
     */
    private int calculateCrc(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc;
    }

    /**
     * 获取异常描述
     */
    private String getExceptionDescription(int exceptionCode) {
        switch (exceptionCode) {
            case 0x01: return "非法功能";
            case 0x02: return "非法数据地址";
            case 0x03: return "非法数据值";
            case 0x04: return "从站设备故障";
            case 0x05: return "确认";
            case 0x06: return "从站设备忙";
            case 0x08: return "存储奇偶性差错";
            case 0x0A: return "不可用网关路径";
            case 0x0B: return "网关目标设备响应失败";
            default: return "未知异常";
        }
    }

}
