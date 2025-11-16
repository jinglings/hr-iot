package cn.iocoder.yudao.module.iot.gateway.codec.modbus;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
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
 * Modbus TCP 格式 {@link IotDeviceMessage} 编解码器
 * <p>
 * Modbus TCP 协议格式：
 * <pre>
 * +----------------+----------------+----------------+----------------+
 * | Transaction ID |  Protocol ID   |     Length     |    Unit ID     |
 * |   (2 bytes)    |   (2 bytes)    |   (2 bytes)    |   (1 byte)     |
 * +----------------+----------------+----------------+----------------+
 * |                           PDU (变长)                             |
 * |  Function Code (1 byte) + Data (变长)                            |
 * +------------------------------------------------------------------+
 * </pre>
 * <p>
 * 映射关系：
 * - 请求消息：method="modbus.read/write"，params 包含 unitId、functionCode、startAddress 等
 * - 响应消息：code=0 表示成功，data 包含寄存器或线圈数据
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotModbusTcpDeviceMessageCodec implements IotDeviceMessageCodec {

    public static final String TYPE = "MODBUS_TCP";

    /**
     * Modbus TCP 最小包长度（MBAP Header）
     */
    private static final int MIN_PACKET_LENGTH = MBAP_HEADER_LENGTH + 1; // MBAP + 功能码

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

            // 生成事务标识符（从请求 ID 派生，或使用随机值）
            int transactionId = generateTransactionId(message.getRequestId());

            // 写入 MBAP Header
            buffer.appendUnsignedShort(transactionId);          // Transaction ID
            buffer.appendUnsignedShort(MODBUS_TCP_PROTOCOL_ID); // Protocol ID (0x0000)

            // 预留长度位置
            int lengthPosition = buffer.length();
            buffer.appendUnsignedShort(0); // Length (稍后更新)

            // Unit ID (从 params 中获取，默认为 1)
            int unitId = getUnitId(message);
            buffer.appendUnsignedByte((short) unitId);

            // 写入 PDU
            int pduStartPosition = buffer.length();
            if (message.getCode() != null) {
                // 响应消息
                encodeMbdusResponse(buffer, message);
            } else {
                // 请求消息
                encodeModbusRequest(buffer, message);
            }

            // 更新长度字段（Unit ID + PDU 的长度）
            int pduLength = buffer.length() - pduStartPosition + 1; // +1 for Unit ID
            buffer.setUnsignedShort(lengthPosition, pduLength);

            return buffer.getBytes();
        } catch (Exception e) {
            log.error("[encode][Modbus TCP 消息编码失败，消息: {}]", message, e);
            throw new RuntimeException("Modbus TCP 消息编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        Assert.notNull(bytes, "待解码数据不能为空");
        Assert.isTrue(bytes.length >= MIN_PACKET_LENGTH, "Modbus TCP 数据包长度不足");
        try {
            Buffer buffer = Buffer.buffer(bytes);
            int index = 0;

            // 1. 解析 MBAP Header
            int transactionId = buffer.getUnsignedShort(index);
            index += 2;

            int protocolId = buffer.getUnsignedShort(index);
            index += 2;
            Assert.isTrue(protocolId == MODBUS_TCP_PROTOCOL_ID,
                    "无效的 Modbus TCP 协议标识符: " + protocolId);

            int length = buffer.getUnsignedShort(index);
            index += 2;
            Assert.isTrue(buffer.length() >= MBAP_HEADER_LENGTH + length - 1,
                    "数据包长度不匹配");

            int unitId = buffer.getUnsignedByte(index);
            index += 1;

            // 2. 解析 PDU
            byte functionCode = buffer.getByte(index);
            index += 1;

            // 判断是否为异常响应
            boolean isException = (functionCode & EXCEPTION_FLAG) != 0;

            // 3. 生成消息 ID
            String messageId = String.format("modbus-%04X", transactionId);

            if (isException) {
                // 异常响应
                return decodeExceptionResponse(buffer, index, messageId, functionCode, unitId);
            } else if (isReadFunction(functionCode)) {
                // 读取响应或请求
                return decodeReadMessage(buffer, index, messageId, functionCode, unitId, bytes);
            } else if (isWriteFunction(functionCode)) {
                // 写入响应或请求
                return decodeWriteMessage(buffer, index, messageId, functionCode, unitId, bytes);
            } else {
                log.warn("[decode][不支持的 Modbus 功能码: {}]", functionCode);
                return createErrorMessage(messageId, functionCode, unitId, "不支持的功能码");
            }
        } catch (Exception e) {
            log.error("[decode][Modbus TCP 消息解码失败，数据长度: {}]", bytes.length, e);
            throw new RuntimeException("Modbus TCP 消息解码失败: " + e.getMessage(), e);
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
    private void encodeMbdusResponse(Buffer buffer, IotDeviceMessage message) {
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
                                               byte functionCode, int unitId, byte[] originalBytes) {
        int remainingLength = buffer.length() - index;

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
                                                byte functionCode, int unitId, byte[] originalBytes) {
        int remainingLength = buffer.length() - index;

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

                // 写入响应格式与请求相同，这里假定为响应
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

                // 这是响应（只有地址和数量）
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
     * 生成事务标识符
     */
    private int generateTransactionId(String requestId) {
        if (requestId != null && requestId.startsWith("modbus-")) {
            try {
                return Integer.parseInt(requestId.substring(7), 16);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        return (int) (System.currentTimeMillis() & 0xFFFF);
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
