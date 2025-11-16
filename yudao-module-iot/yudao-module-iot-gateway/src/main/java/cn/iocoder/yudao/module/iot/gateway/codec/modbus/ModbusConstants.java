package cn.iocoder.yudao.module.iot.gateway.codec.modbus;

/**
 * Modbus 协议常量定义
 *
 * @author 芋道源码
 */
public class ModbusConstants {

    /**
     * Modbus 方法名前缀
     */
    public static final String METHOD_PREFIX = "modbus.";

    /**
     * Modbus 读取方法
     */
    public static final String METHOD_READ = "modbus.read";

    /**
     * Modbus 写入方法
     */
    public static final String METHOD_WRITE = "modbus.write";

    /**
     * Modbus TCP 协议标识符
     */
    public static final int MODBUS_TCP_PROTOCOL_ID = 0x0000;

    /**
     * Modbus 功能码 - 读取线圈
     */
    public static final byte FUNCTION_CODE_READ_COILS = 0x01;

    /**
     * Modbus 功能码 - 读取离散输入
     */
    public static final byte FUNCTION_CODE_READ_DISCRETE_INPUTS = 0x02;

    /**
     * Modbus 功能码 - 读取保持寄存器
     */
    public static final byte FUNCTION_CODE_READ_HOLDING_REGISTERS = 0x03;

    /**
     * Modbus 功能码 - 读取输入寄存器
     */
    public static final byte FUNCTION_CODE_READ_INPUT_REGISTERS = 0x04;

    /**
     * Modbus 功能码 - 写单个线圈
     */
    public static final byte FUNCTION_CODE_WRITE_SINGLE_COIL = 0x05;

    /**
     * Modbus 功能码 - 写单个寄存器
     */
    public static final byte FUNCTION_CODE_WRITE_SINGLE_REGISTER = 0x06;

    /**
     * Modbus 功能码 - 写多个线圈
     */
    public static final byte FUNCTION_CODE_WRITE_MULTIPLE_COILS = 0x0F;

    /**
     * Modbus 功能码 - 写多个寄存器
     */
    public static final byte FUNCTION_CODE_WRITE_MULTIPLE_REGISTERS = 0x10;

    /**
     * Modbus TCP MBAP 头长度
     */
    public static final int MBAP_HEADER_LENGTH = 7;

    /**
     * Modbus RTU CRC 长度
     */
    public static final int RTU_CRC_LENGTH = 2;

    /**
     * Modbus RTU 地址长度
     */
    public static final int RTU_ADDRESS_LENGTH = 1;

    /**
     * 异常功能码标志（设置功能码的最高位）
     */
    public static final byte EXCEPTION_FLAG = (byte) 0x80;

    /**
     * 参数键 - 单元标识符（从站地址）
     */
    public static final String PARAM_UNIT_ID = "unitId";

    /**
     * 参数键 - 功能码
     */
    public static final String PARAM_FUNCTION_CODE = "functionCode";

    /**
     * 参数键 - 起始地址
     */
    public static final String PARAM_START_ADDRESS = "startAddress";

    /**
     * 参数键 - 数量（线圈/寄存器数量）
     */
    public static final String PARAM_QUANTITY = "quantity";

    /**
     * 参数键 - 值（写入的值）
     */
    public static final String PARAM_VALUE = "value";

    /**
     * 参数键 - 值数组（写入多个值）
     */
    public static final String PARAM_VALUES = "values";

    /**
     * 数据键 - 寄存器数据
     */
    public static final String DATA_REGISTERS = "registers";

    /**
     * 数据键 - 线圈状态
     */
    public static final String DATA_COILS = "coils";

    /**
     * 数据键 - 异常码
     */
    public static final String DATA_EXCEPTION_CODE = "exceptionCode";

    /**
     * 私有构造函数，防止实例化
     */
    private ModbusConstants() {
    }

}
