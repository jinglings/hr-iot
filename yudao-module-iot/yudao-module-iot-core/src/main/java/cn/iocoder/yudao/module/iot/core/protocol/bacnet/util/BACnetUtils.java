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
     * 格式: "objectType:instanceNumber" 例如: "analogInput:0"
     */
    public static ObjectIdentifierPair parseObjectIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("对象标识符不能为空");
        }

        String[] parts = identifier.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("对象标识符格式错误，应为 'objectType:instanceNumber'");
        }

        try {
            ObjectType objectType = ObjectType.valueOf(parts[0]);
            Integer instanceNumber = Integer.parseInt(parts[1]);
            return new ObjectIdentifierPair(objectType, instanceNumber);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析对象标识符: " + identifier, e);
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
