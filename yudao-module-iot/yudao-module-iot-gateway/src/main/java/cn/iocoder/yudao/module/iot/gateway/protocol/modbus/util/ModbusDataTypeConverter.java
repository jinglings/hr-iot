package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.util;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Modbus 数据类型转换工具类
 * 支持各种数据类型：INT16, UINT16, INT32, UINT32, INT64, FLOAT, DOUBLE, BOOL, STRING
 *
 * @author 芋道源码
 */
@Slf4j
public class ModbusDataTypeConverter {

    /**
     * 数据类型枚举
     */
    public enum DataType {
        INT16,      // 有符号 16 位整数（1 个寄存器）
        UINT16,     // 无符号 16 位整数（1 个寄存器）
        INT32,      // 有符号 32 位整数（2 个寄存器）
        UINT32,     // 无符号 32 位整数（2 个寄存器）
        INT64,      // 有符号 64 位整数（4 个寄存器）
        UINT64,     // 无符号 64 位整数（4 个寄存器）
        FLOAT,      // 32 位浮点数（2 个寄存器）
        DOUBLE,     // 64 位浮点数（4 个寄存器）
        BOOL,       // 布尔值（1 个寄存器或线圈）
        STRING      // 字符串（多个寄存器）
    }

    /**
     * 字节序枚举
     */
    public enum ByteOrderType {
        BIG_ENDIAN,              // ABCD (大端序)
        LITTLE_ENDIAN,           // DCBA (小端序)
        BIG_ENDIAN_SWAP,         // BADC (大端字节交换)
        LITTLE_ENDIAN_SWAP       // CDAB (小端字节交换)
    }

    /**
     * 将寄存器数组转换为指定数据类型的值
     *
     * @param registers 寄存器数组
     * @param dataType 数据类型
     * @param byteOrderType 字节序
     * @return 转换后的值
     */
    public static Object convertFromRegisters(InputRegister[] registers, DataType dataType, ByteOrderType byteOrderType) {
        if (registers == null || registers.length == 0) {
            return null;
        }

        try {
            switch (dataType) {
                case INT16:
                    return toInt16(registers[0]);
                case UINT16:
                    return toUInt16(registers[0]);
                case INT32:
                    return toInt32(registers, byteOrderType);
                case UINT32:
                    return toUInt32(registers, byteOrderType);
                case INT64:
                    return toInt64(registers, byteOrderType);
                case UINT64:
                    return toUInt64(registers, byteOrderType);
                case FLOAT:
                    return toFloat(registers, byteOrderType);
                case DOUBLE:
                    return toDouble(registers, byteOrderType);
                case BOOL:
                    return toBool(registers[0]);
                case STRING:
                    return toString(registers);
                default:
                    log.warn("[convertFromRegisters][不支持的数据类型: {}]", dataType);
                    return null;
            }
        } catch (Exception e) {
            log.error("[convertFromRegisters][数据转换失败，数据类型: {}]", dataType, e);
            return null;
        }
    }

    /**
     * 将值转换为寄存器数组
     *
     * @param value 值
     * @param dataType 数据类型
     * @param byteOrderType 字节序
     * @return 寄存器数组
     */
    public static Register[] convertToRegisters(Object value, DataType dataType, ByteOrderType byteOrderType) {
        if (value == null) {
            return new Register[0];
        }

        try {
            switch (dataType) {
                case INT16:
                case UINT16:
                    return fromInt16(((Number) value).intValue());
                case INT32:
                case UINT32:
                    return fromInt32(((Number) value).intValue(), byteOrderType);
                case INT64:
                case UINT64:
                    return fromInt64(((Number) value).longValue(), byteOrderType);
                case FLOAT:
                    return fromFloat(((Number) value).floatValue(), byteOrderType);
                case DOUBLE:
                    return fromDouble(((Number) value).doubleValue(), byteOrderType);
                case BOOL:
                    return fromBool((Boolean) value);
                case STRING:
                    return fromString((String) value);
                default:
                    log.warn("[convertToRegisters][不支持的数据类型: {}]", dataType);
                    return new Register[0];
            }
        } catch (Exception e) {
            log.error("[convertToRegisters][数据转换失败，数据类型: {}，值: {}]", dataType, value, e);
            return new Register[0];
        }
    }

    // ========== INT16 转换 ==========

    private static short toInt16(InputRegister register) {
        return (short) register.getValue();
    }

    private static int toUInt16(InputRegister register) {
        return register.getValue() & 0xFFFF;
    }

    private static Register[] fromInt16(int value) {
        return new Register[]{new com.ghgande.j2mod.modbus.procimg.SimpleRegister(value & 0xFFFF)};
    }

    // ========== INT32 转换 ==========

    private static int toInt32(InputRegister[] registers, ByteOrderType byteOrder) {
        if (registers.length < 2) {
            throw new IllegalArgumentException("INT32 需要至少 2 个寄存器");
        }
        byte[] bytes = registersToBytes(registers, 2, byteOrder);
        return ByteBuffer.wrap(bytes).order(getByteOrder(byteOrder)).getInt();
    }

    private static long toUInt32(InputRegister[] registers, ByteOrderType byteOrder) {
        return toInt32(registers, byteOrder) & 0xFFFFFFFFL;
    }

    private static Register[] fromInt32(int value, ByteOrderType byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(getByteOrder(byteOrder));
        buffer.putInt(value);
        return bytesToRegisters(buffer.array(), byteOrder);
    }

    // ========== INT64 转换 ==========

    private static long toInt64(InputRegister[] registers, ByteOrderType byteOrder) {
        if (registers.length < 4) {
            throw new IllegalArgumentException("INT64 需要至少 4 个寄存器");
        }
        byte[] bytes = registersToBytes(registers, 4, byteOrder);
        return ByteBuffer.wrap(bytes).order(getByteOrder(byteOrder)).getLong();
    }

    private static String toUInt64(InputRegister[] registers, ByteOrderType byteOrder) {
        // Java 不支持无符号 long，返回字符串表示
        long value = toInt64(registers, byteOrder);
        if (value < 0) {
            return String.valueOf((value >>> 1) * 2 + (value & 1));
        }
        return String.valueOf(value);
    }

    private static Register[] fromInt64(long value, ByteOrderType byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(getByteOrder(byteOrder));
        buffer.putLong(value);
        return bytesToRegisters(buffer.array(), byteOrder);
    }

    // ========== FLOAT 转换 ==========

    private static float toFloat(InputRegister[] registers, ByteOrderType byteOrder) {
        if (registers.length < 2) {
            throw new IllegalArgumentException("FLOAT 需要至少 2 个寄存器");
        }
        byte[] bytes = registersToBytes(registers, 2, byteOrder);
        return ByteBuffer.wrap(bytes).order(getByteOrder(byteOrder)).getFloat();
    }

    private static Register[] fromFloat(float value, ByteOrderType byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(getByteOrder(byteOrder));
        buffer.putFloat(value);
        return bytesToRegisters(buffer.array(), byteOrder);
    }

    // ========== DOUBLE 转换 ==========

    private static double toDouble(InputRegister[] registers, ByteOrderType byteOrder) {
        if (registers.length < 4) {
            throw new IllegalArgumentException("DOUBLE 需要至少 4 个寄存器");
        }
        byte[] bytes = registersToBytes(registers, 4, byteOrder);
        return ByteBuffer.wrap(bytes).order(getByteOrder(byteOrder)).getDouble();
    }

    private static Register[] fromDouble(double value, ByteOrderType byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(getByteOrder(byteOrder));
        buffer.putDouble(value);
        return bytesToRegisters(buffer.array(), byteOrder);
    }

    // ========== BOOL 转换 ==========

    private static boolean toBool(InputRegister register) {
        return register.getValue() != 0;
    }

    private static Register[] fromBool(boolean value) {
        return new Register[]{new com.ghgande.j2mod.modbus.procimg.SimpleRegister(value ? 1 : 0)};
    }

    // ========== STRING 转换 ==========

    private static String toString(InputRegister[] registers) {
        byte[] bytes = new byte[registers.length * 2];
        for (int i = 0; i < registers.length; i++) {
            int value = registers[i].getValue();
            bytes[i * 2] = (byte) ((value >> 8) & 0xFF);
            bytes[i * 2 + 1] = (byte) (value & 0xFF);
        }
        // 移除尾部的 null 字符
        int length = bytes.length;
        while (length > 0 && bytes[length - 1] == 0) {
            length--;
        }
        return new String(bytes, 0, length);
    }

    private static Register[] fromString(String value) {
        byte[] bytes = value.getBytes();
        int registerCount = (bytes.length + 1) / 2;
        Register[] registers = new Register[registerCount];
        for (int i = 0; i < registerCount; i++) {
            int high = i * 2 < bytes.length ? (bytes[i * 2] & 0xFF) : 0;
            int low = i * 2 + 1 < bytes.length ? (bytes[i * 2 + 1] & 0xFF) : 0;
            registers[i] = new com.ghgande.j2mod.modbus.procimg.SimpleRegister((high << 8) | low);
        }
        return registers;
    }

    // ========== 辅助方法 ==========

    /**
     * 将寄存器数组转换为字节数组
     */
    private static byte[] registersToBytes(InputRegister[] registers, int count, ByteOrderType byteOrder) {
        byte[] bytes = new byte[count * 2];
        for (int i = 0; i < count; i++) {
            int value = registers[i].getValue();
            bytes[i * 2] = (byte) ((value >> 8) & 0xFF);
            bytes[i * 2 + 1] = (byte) (value & 0xFF);
        }
        return applyByteOrder(bytes, byteOrder);
    }

    /**
     * 将字节数组转换为寄存器数组
     */
    private static Register[] bytesToRegisters(byte[] bytes, ByteOrderType byteOrder) {
        byte[] orderedBytes = applyByteOrder(bytes, byteOrder);
        int registerCount = orderedBytes.length / 2;
        Register[] registers = new Register[registerCount];
        for (int i = 0; i < registerCount; i++) {
            int high = orderedBytes[i * 2] & 0xFF;
            int low = orderedBytes[i * 2 + 1] & 0xFF;
            registers[i] = new com.ghgande.j2mod.modbus.procimg.SimpleRegister((high << 8) | low);
        }
        return registers;
    }

    /**
     * 应用字节序转换
     */
    private static byte[] applyByteOrder(byte[] bytes, ByteOrderType byteOrder) {
        byte[] result = bytes.clone();
        switch (byteOrder) {
            case BIG_ENDIAN:
                // 不需要转换
                break;
            case LITTLE_ENDIAN:
                // 完全反转
                for (int i = 0; i < result.length / 2; i++) {
                    byte temp = result[i];
                    result[i] = result[result.length - 1 - i];
                    result[result.length - 1 - i] = temp;
                }
                break;
            case BIG_ENDIAN_SWAP:
                // 交换相邻字节对
                for (int i = 0; i < result.length; i += 2) {
                    byte temp = result[i];
                    result[i] = result[i + 1];
                    result[i + 1] = temp;
                }
                break;
            case LITTLE_ENDIAN_SWAP:
                // 先反转，再交换相邻字节对
                for (int i = 0; i < result.length / 2; i++) {
                    byte temp = result[i];
                    result[i] = result[result.length - 1 - i];
                    result[result.length - 1 - i] = temp;
                }
                for (int i = 0; i < result.length; i += 2) {
                    byte temp = result[i];
                    result[i] = result[i + 1];
                    result[i + 1] = temp;
                }
                break;
        }
        return result;
    }

    /**
     * 获取 Java ByteOrder
     */
    private static ByteOrder getByteOrder(ByteOrderType byteOrder) {
        switch (byteOrder) {
            case LITTLE_ENDIAN:
            case LITTLE_ENDIAN_SWAP:
                return ByteOrder.LITTLE_ENDIAN;
            case BIG_ENDIAN:
            case BIG_ENDIAN_SWAP:
            default:
                return ByteOrder.BIG_ENDIAN;
        }
    }

    /**
     * 解析数据类型字符串
     */
    public static DataType parseDataType(String dataType) {
        try {
            return DataType.valueOf(dataType.toUpperCase());
        } catch (Exception e) {
            log.warn("[parseDataType][无效的数据类型: {}，使用默认值 INT16]", dataType);
            return DataType.INT16;
        }
    }

    /**
     * 解析字节序字符串
     */
    public static ByteOrderType parseByteOrder(String byteOrder) {
        try {
            return ByteOrderType.valueOf(byteOrder.toUpperCase());
        } catch (Exception e) {
            log.warn("[parseByteOrder][无效的字节序: {}，使用默认值 BIG_ENDIAN]", byteOrder);
            return ByteOrderType.BIG_ENDIAN;
        }
    }

    /**
     * 计算数据类型需要的寄存器数量
     */
    public static int getRegisterCount(DataType dataType, int stringLength) {
        switch (dataType) {
            case INT16:
            case UINT16:
            case BOOL:
                return 1;
            case INT32:
            case UINT32:
            case FLOAT:
                return 2;
            case INT64:
            case UINT64:
            case DOUBLE:
                return 4;
            case STRING:
                return (stringLength + 1) / 2;
            default:
                return 1;
        }
    }

}
