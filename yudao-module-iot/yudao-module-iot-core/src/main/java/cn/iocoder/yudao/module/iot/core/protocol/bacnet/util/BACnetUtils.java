package cn.iocoder.yudao.module.iot.core.protocol.bacnet.util;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;

/**
 * BACnet 工具类
 *
 * @author yudao
 */
public class BACnetUtils {

    /**
     * 根据对象类型代码获取 ObjectType
     */
    public static ObjectType getObjectType(int typeCode) {
        return ObjectType.forId(typeCode);
    }

    /**
     * 获取对象类型名称
     */
    public static String getObjectTypeName(ObjectType objectType) {
        if (objectType == null) {
            return "Unknown";
        }
        return objectType.toString();
    }

    /**
     * 解析 BACnet 对象标识符字符串
     * 格式: "objectType:instanceNumber" 例如: "analogInput:0" 或 "0:0" (使用类型ID)
     */
    public static ObjectIdentifierPair parseObjectIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("对象标识符不能为空");
        }

        String[] parts = identifier.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("对象标识符格式错误，应为 'objectType:instanceNumber' 或 'typeId:instanceNumber'");
        }

        try {
            ObjectType objectType;
            // 尝试将第一部分解析为数字ID
            try {
                int typeId = Integer.parseInt(parts[0]);
                objectType = ObjectType.forId(typeId);
            } catch (NumberFormatException e) {
                // 如果不是数字，则尝试通过名称查找
                // bacnet4j 6.x 使用 forName 方法
                objectType = getObjectTypeByName(parts[0]);
            }

            Integer instanceNumber = Integer.parseInt(parts[1]);
            return new ObjectIdentifierPair(objectType, instanceNumber);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析对象标识符: " + identifier, e);
        }
    }

    /**
     * 根据名称获取 ObjectType
     * 在 bacnet4j 6.x 中需要手动查找匹配的类型
     */
    private static ObjectType getObjectTypeByName(String name) {
        // 常用的 BACnet 对象类型映射
        switch (name.toLowerCase()) {
            case "analoginput":
            case "analog-input":
                return ObjectType.analogInput;
            case "analogoutput":
            case "analog-output":
                return ObjectType.analogOutput;
            case "analogvalue":
            case "analog-value":
                return ObjectType.analogValue;
            case "binaryinput":
            case "binary-input":
                return ObjectType.binaryInput;
            case "binaryoutput":
            case "binary-output":
                return ObjectType.binaryOutput;
            case "binaryvalue":
            case "binary-value":
                return ObjectType.binaryValue;
            case "device":
                return ObjectType.device;
            case "multistateinput":
            case "multi-state-input":
                return ObjectType.multiStateInput;
            case "multistateoutput":
            case "multi-state-output":
                return ObjectType.multiStateOutput;
            case "multistatevalue":
            case "multi-state-value":
                return ObjectType.multiStateValue;
            default:
                throw new IllegalArgumentException("未知的对象类型: " + name);
        }
    }

    /**
     * 对象标识符对
     */
    public static class ObjectIdentifierPair {
        private final ObjectType objectType;
        private final Integer instanceNumber;

        public ObjectIdentifierPair(ObjectType objectType, Integer instanceNumber) {
            this.objectType = objectType;
            this.instanceNumber = instanceNumber;
        }

        public ObjectType getObjectType() {
            return objectType;
        }

        public Integer getInstanceNumber() {
            return instanceNumber;
        }
    }

}
