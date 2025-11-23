package cn.iocoder.yudao.module.iot.core.protocol.bacnet.core;

import cn.iocoder.yudao.module.iot.core.protocol.bacnet.config.BACnetProperties;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetDeviceInfo;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetObjectInfo;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * BACnet 客户端服务
 * 负责与 BACnet 设备通信
 *
 * @author yudao
 */
@Slf4j
public class BACnetClient {

    private final BACnetProperties properties;
    private LocalDevice localDevice;
    private final ConcurrentHashMap<Integer, RemoteDevice> remoteDevices = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    public BACnetClient(BACnetProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 BACnet 本地设备
     */
    public void initialize() throws Exception {
        if (initialized) {
            log.warn("BACnet 客户端已经初始化");
            return;
        }

        try {
            // 创建 IP 网络 - 使用 bacnet4j 6.x API
            IpNetworkBuilder builder = new IpNetworkBuilder();

            // 设置本地绑定地址和端口
            if (properties.getLocalBindAddress() != null && !properties.getLocalBindAddress().isEmpty()) {
                // 绑定到指定地址
                builder.withLocalBindAddress(properties.getLocalBindAddress());
            }

            // 设置端口
            builder.withPort(properties.getPort());

            // 设置广播地址
            if (properties.getBroadcastAddress() != null && !properties.getBroadcastAddress().isEmpty()) {
                builder.withBroadcast(properties.getBroadcastAddress(), 0xFFFF);
            }

            IpNetwork network = builder.build();

            // 创建本地设备
            DefaultTransport transport = new DefaultTransport(network);
            localDevice = new LocalDevice(properties.getDeviceId(), transport);
            localDevice.getEventHandler().addListener(new DeviceEventAdapter());

            // 初始化设备
            localDevice.initialize();
            initialized = true;
            log.info("BACnet 本地设备初始化成功，设备 ID: {}, 端口: {}",
                    properties.getDeviceId(), properties.getPort());
        } catch (Exception e) {
            log.error("BACnet 本地设备初始化失败", e);
            throw e;
        }
    }

    /**
     * 发现网络中的 BACnet 设备
     */
    public List<BACnetDeviceInfo> discoverDevices() throws Exception {
        if (!initialized) {
            throw new IllegalStateException("BACnet 客户端未初始化");
        }

        List<BACnetDeviceInfo> deviceInfoList = new ArrayList<>();

        try {
            // 执行设备发现
            log.info("开始发现 BACnet 设备...");
            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            // 等待设备响应
            TimeUnit.MILLISECONDS.sleep(properties.getDiscoveryTimeout());

            // 获取发现的设备
            for (RemoteDevice device : localDevice.getRemoteDevices()) {
                BACnetDeviceInfo deviceInfo = convertToDeviceInfo(device);
                deviceInfoList.add(deviceInfo);
                remoteDevices.put(device.getInstanceNumber(), device);
            }

            log.info("发现 {} 个 BACnet 设备", deviceInfoList.size());
        } catch (Exception e) {
            log.error("发现 BACnet 设备失败", e);
            throw e;
        }

        return deviceInfoList;
    }

    /**
     * 获取设备详细信息
     */
    public BACnetDeviceInfo getDeviceInfo(Integer instanceNumber) throws Exception {
        RemoteDevice device = remoteDevices.get(instanceNumber);
        if (device == null) {
            // 尝试从本地设备获取
            device = localDevice.getRemoteDevice(instanceNumber).get();
            if (device != null) {
                remoteDevices.put(instanceNumber, device);
            }
        }

        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + instanceNumber);
        }

        // 获取扩展设备信息
        DiscoveryUtils.getExtendedDeviceInformation(localDevice, device);
        return convertToDeviceInfo(device);
    }

    /**
     * 读取对象属性值
     */
    public Object readProperty(Integer deviceInstanceNumber, ObjectType objectType,
                              Integer objectInstanceNumber, PropertyIdentifier propertyId) throws Exception {
        RemoteDevice device = remoteDevices.get(deviceInstanceNumber);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceInstanceNumber);
        }

        ObjectIdentifier objectIdentifier = new ObjectIdentifier(objectType, objectInstanceNumber);
        ReadPropertyRequest request = new ReadPropertyRequest(objectIdentifier, propertyId);

        ReadPropertyAck ack = (ReadPropertyAck) localDevice.send(device, request).get();
        return ack.getValue();
    }

    /**
     * 获取设备的所有对象
     */
    public List<BACnetObjectInfo> getDeviceObjects(Integer instanceNumber) throws Exception {
        RemoteDevice device = remoteDevices.get(instanceNumber);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + instanceNumber);
        }

        List<BACnetObjectInfo> objectInfoList = new ArrayList<>();

        try {
            // 读取设备的对象列表
            ObjectIdentifier deviceOid = new ObjectIdentifier(ObjectType.device, instanceNumber);
            ReadPropertyRequest request = new ReadPropertyRequest(deviceOid, PropertyIdentifier.objectList);

            // 这里简化处理，实际应该遍历对象列表
            // 可以通过扩展实现更详细的对象信息获取

        } catch (Exception e) {
            log.error("获取设备对象列表失败: {}", instanceNumber, e);
            throw e;
        }

        return objectInfoList;
    }

    /**
     * 关闭 BACnet 客户端
     */
    public void shutdown() {
        if (localDevice != null) {
            try {
                localDevice.terminate();
                initialized = false;
                remoteDevices.clear();
                log.info("BACnet 本地设备已关闭");
            } catch (Exception e) {
                log.error("关闭 BACnet 本地设备失败", e);
            }
        }
    }

    /**
     * 转换为设备信息 DTO
     */
    private BACnetDeviceInfo convertToDeviceInfo(RemoteDevice device) {
        BACnetDeviceInfo info = new BACnetDeviceInfo();
        info.setInstanceNumber(device.getInstanceNumber());

        // 安全获取设备信息，某些信息可能需要先读取设备属性才能获得
        try {
            info.setDeviceName(device.getName() != null ? device.getName() : "Unknown");
            info.setIpAddress(device.getAddress() != null ? device.getAddress().toString() : "Unknown");

            // 在 bacnet4j 6.x 中，这些属性可能需要通过读取设备对象获取
            // 如果直接访问失败，设置默认值
            try {
                info.setVendorName(device.getVendorName() != null ? device.getVendorName() : "Unknown");
            } catch (Exception e) {
                info.setVendorName("Unknown");
            }

            try {
                info.setModelName(device.getModelName() != null ? device.getModelName() : "Unknown");
            } catch (Exception e) {
                info.setModelName("Unknown");
            }

            info.setOnline(true);
            info.setLastCommunicationTime(System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("获取设备信息部分失败: {}", e.getMessage());
        }

        return info;
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 写入对象属性值
     */
    public void writeProperty(Integer deviceInstanceNumber,
                             ObjectType objectType,
                             Integer objectInstanceNumber,
                             PropertyIdentifier propertyId,
                             Object value,
                             Integer priority) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("BACnet 客户端未初始化");
        }

        RemoteDevice device = remoteDevices.get(deviceInstanceNumber);
        if (device == null) {
            device = localDevice.getRemoteDevice(deviceInstanceNumber).get();
            if (device != null) {
                remoteDevices.put(deviceInstanceNumber, device);
            }
        }

        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceInstanceNumber);
        }

        try {
            com.serotonin.bacnet4j.type.primitive.ObjectIdentifier objectIdentifier =
                    new com.serotonin.bacnet4j.type.primitive.ObjectIdentifier(objectType, objectInstanceNumber);

            // 转换值为 BACnet 编码值
            com.serotonin.bacnet4j.type.Encodable encodableValue = convertToEncodable(value);

            // 构建写入请求
            com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest request;
            if (priority != null) {
                request = new com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest(
                        objectIdentifier,
                        propertyId,
                        null, // arrayIndex
                        encodableValue,
                        new com.serotonin.bacnet4j.type.primitive.UnsignedInteger(priority)
                );
            } else {
                request = new com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest(
                        objectIdentifier,
                        propertyId,
                        null, // arrayIndex
                        encodableValue,
                        null // priority
                );
            }

            // 发送写入请求
            localDevice.send(device, request).get();

            log.info("[writeProperty][写入属性成功] device={}, objectType={}, objectInstance={}, property={}, value={}",
                    deviceInstanceNumber, objectType, objectInstanceNumber, propertyId, value);

        } catch (Exception e) {
            log.error("[writeProperty][写入属性失败] device={}, objectType={}, objectInstance={}, property={}",
                    deviceInstanceNumber, objectType, objectInstanceNumber, propertyId, e);
            throw e;
        }
    }

    /**
     * 转换 Java 对象为 BACnet 编码值
     */
    private com.serotonin.bacnet4j.type.Encodable convertToEncodable(Object value) {
        if (value == null) {
            return new com.serotonin.bacnet4j.type.primitive.Null();
        }

        if (value instanceof java.lang.Boolean) {
            // BACnet4J的Boolean类使用静态常量TRUE和FALSE
            return ((java.lang.Boolean) value) ?
                com.serotonin.bacnet4j.type.primitive.Boolean.TRUE :
                com.serotonin.bacnet4j.type.primitive.Boolean.FALSE;
        } else if (value instanceof Integer) {
            return new com.serotonin.bacnet4j.type.primitive.UnsignedInteger((Integer) value);
        } else if (value instanceof Long) {
            return new com.serotonin.bacnet4j.type.primitive.UnsignedInteger(((Long) value).intValue());
        } else if (value instanceof Float) {
            return new com.serotonin.bacnet4j.type.primitive.Real((Float) value);
        } else if (value instanceof Double) {
            return new com.serotonin.bacnet4j.type.primitive.Double((Double) value);
        } else if (value instanceof String) {
            return new com.serotonin.bacnet4j.type.primitive.CharacterString((String) value);
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + value.getClass());
        }
    }

    /**
     * 获取本地设备
     */
    public LocalDevice getLocalDevice() {
        return localDevice;
    }

}
