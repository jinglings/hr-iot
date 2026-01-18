package cn.iocoder.yudao.module.iot.core.protocol.bacnet.util;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

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
     * 支持多种格式：camelCase, kebab-case, SCREAMING_SNAKE_CASE, 缩写(AI/AO/AV等)
     */
    public static ObjectType getObjectTypeByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("对象类型名称不能为空");
        }
        // 常用的 BACnet 对象类型映射
        switch (name.toLowerCase().replace("_", "").replace("-", "")) {
            // Analog Input
            case "analoginput":
            case "ai":
                return ObjectType.analogInput;
            // Analog Output
            case "analogoutput":
            case "ao":
                return ObjectType.analogOutput;
            // Analog Value
            case "analogvalue":
            case "av":
                return ObjectType.analogValue;
            // Binary Input
            case "binaryinput":
            case "bi":
                return ObjectType.binaryInput;
            // Binary Output
            case "binaryoutput":
            case "bo":
                return ObjectType.binaryOutput;
            // Binary Value
            case "binaryvalue":
            case "bv":
                return ObjectType.binaryValue;
            // Device
            case "device":
            case "dev":
                return ObjectType.device;
            // Multi-State Input
            case "multistateinput":
            case "msi":
                return ObjectType.multiStateInput;
            // Multi-State Output
            case "multistateoutput":
            case "mso":
                return ObjectType.multiStateOutput;
            // Multi-State Value
            case "multistatevalue":
            case "msv":
                return ObjectType.multiStateValue;
            // Schedule
            case "schedule":
            case "sch":
                return ObjectType.schedule;
            // Calendar
            case "calendar":
            case "cal":
                return ObjectType.calendar;
            // Notification Class
            case "notificationclass":
            case "nc":
                return ObjectType.notificationClass;
            // Trend Log
            case "trendlog":
            case "tl":
                return ObjectType.trendLog;
            // Loop
            case "loop":
                return ObjectType.loop;
            // Program
            case "program":
            case "prg":
                return ObjectType.program;
            // File
            case "file":
                return ObjectType.file;
            default:
                throw new IllegalArgumentException("未知的对象类型: " + name);
        }
    }

    /**
     * 根据名称获取 PropertyIdentifier
     * 支持多种格式：camelCase, kebab-case, SCREAMING_SNAKE_CASE, 缩写(PV/SV/SF等)
     */
    public static PropertyIdentifier getPropertyIdentifierByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("属性标识符名称不能为空");
        }
        // 常用的 BACnet 属性标识符映射
        switch (name.toLowerCase().replace("_", "").replace("-", "")) {
            // Present Value
            case "presentvalue":
            case "pv":
                return PropertyIdentifier.presentValue;
            // Status Flags
            case "statusflags":
            case "sf":
                return PropertyIdentifier.statusFlags;
            // Object Name
            case "objectname":
            case "name":
                return PropertyIdentifier.objectName;
            // Object Type
            case "objecttype":
            case "type":
                return PropertyIdentifier.objectType;
            // Object Identifier
            case "objectidentifier":
            case "oid":
                return PropertyIdentifier.objectIdentifier;
            // Description
            case "description":
            case "desc":
                return PropertyIdentifier.description;
            // Device Type
            case "devicetype":
                return PropertyIdentifier.deviceType;
            // Units
            case "units":
                return PropertyIdentifier.units;
            // Out of Service
            case "outofservice":
            case "oos":
                return PropertyIdentifier.outOfService;
            // Event State
            case "eventstate":
                return PropertyIdentifier.eventState;
            // Reliability
            case "reliability":
                return PropertyIdentifier.reliability;
            // Priority Array
            case "priorityarray":
                return PropertyIdentifier.priorityArray;
            // Relinquish Default
            case "relinquishdefault":
                return PropertyIdentifier.relinquishDefault;
            // COV Increment
            case "covincrement":
                return PropertyIdentifier.covIncrement;
            // High Limit
            case "highlimit":
                return PropertyIdentifier.highLimit;
            // Low Limit
            case "lowlimit":
                return PropertyIdentifier.lowLimit;
            // Deadband
            case "deadband":
                return PropertyIdentifier.deadband;
            // Resolution
            case "resolution":
                return PropertyIdentifier.resolution;
            // Min Pres Value
            case "minpresvalue":
                return PropertyIdentifier.minPresValue;
            // Max Pres Value
            case "maxpresvalue":
                return PropertyIdentifier.maxPresValue;
            // Update Interval
            case "updateinterval":
                return PropertyIdentifier.updateInterval;
            // Polarity
            case "polarity":
                return PropertyIdentifier.polarity;
            // Inactive Text
            case "inactivetext":
                return PropertyIdentifier.inactiveText;
            // Active Text
            case "activetext":
                return PropertyIdentifier.activeText;
            // Number of States
            case "numberofstates":
                return PropertyIdentifier.numberOfStates;
            // State Text
            case "statetext":
                return PropertyIdentifier.stateText;
            // Setpoint Reference
            case "setpointreference":
                return PropertyIdentifier.setpointReference;
            // Controlled Variable Value
            case "controlledvariablevalue":
                return PropertyIdentifier.controlledVariableValue;
            // Manipulated Variable Reference
            case "manipulatedvariablereference":
                return PropertyIdentifier.manipulatedVariableReference;
            // Object List
            case "objectlist":
                return PropertyIdentifier.objectList;
            // Vendor Name
            case "vendorname":
                return PropertyIdentifier.vendorName;
            // Vendor Identifier
            case "vendoridentifier":
                return PropertyIdentifier.vendorIdentifier;
            // Model Name
            case "modelname":
                return PropertyIdentifier.modelName;
            // Firmware Revision
            case "firmwarerevision":
                return PropertyIdentifier.firmwareRevision;
            // Application Software Version
            case "applicationsoftwareversion":
                return PropertyIdentifier.applicationSoftwareVersion;
            // Protocol Version
            case "protocolversion":
                return PropertyIdentifier.protocolVersion;
            // Protocol Revision
            case "protocolrevision":
                return PropertyIdentifier.protocolRevision;
            // System Status
            case "systemstatus":
                return PropertyIdentifier.systemStatus;
            default:
                throw new IllegalArgumentException("未知的属性标识符: " + name);
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
