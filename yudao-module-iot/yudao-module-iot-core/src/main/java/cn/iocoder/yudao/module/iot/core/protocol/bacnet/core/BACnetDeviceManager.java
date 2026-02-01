package cn.iocoder.yudao.module.iot.core.protocol.bacnet.core;

import cn.iocoder.yudao.module.iot.core.protocol.bacnet.config.BACnetProperties;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetDeviceInfo;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetObjectInfo;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BACnet 设备管理器
 * 提供设备管理和通信的统一接口
 *
 * @author yudao
 */
@Slf4j
public class BACnetDeviceManager {

    private final BACnetProperties properties;
    private BACnetClient bacnetClient;
    private final ConcurrentHashMap<Integer, BACnetDeviceInfo> deviceCache = new ConcurrentHashMap<>();

    public BACnetDeviceManager(BACnetProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 BACnet 设备管理器
     */
    @PostConstruct
    public void init() {
        if (!properties.getEnabled()) {
            log.info("BACnet 协议未启用，跳过初始化");
            return;
        }

        try {
            bacnetClient = new BACnetClient(properties);
            bacnetClient.initialize();
            log.info("BACnet 设备管理器初始化成功");

            // 初始发现设备
            discoverAndCacheDevices();
        } catch (Exception e) {
            log.error("BACnet 设备管理器初始化失败", e);
        }
    }

    /**
     * 发现并缓存设备
     */
    public List<BACnetDeviceInfo> discoverAndCacheDevices() {
        if (!isEnabled()) {
            throw new IllegalStateException("BACnet 协议未启用");
        }

        try {
            List<BACnetDeviceInfo> devices = bacnetClient.discoverDevices();
            // 更新缓存
            deviceCache.clear();
            for (BACnetDeviceInfo device : devices) {
                deviceCache.put(device.getInstanceNumber(), device);
            }
            log.info("已缓存 {} 个 BACnet 设备", deviceCache.size());
            return devices;
        } catch (Exception e) {
            log.error("发现设备失败", e);
            throw new RuntimeException("发现设备失败", e);
        }
    }

    /**
     * 获取设备信息
     */
    public BACnetDeviceInfo getDeviceInfo(Integer instanceNumber) {
        if (!isEnabled()) {
            throw new IllegalStateException("BACnet 协议未启用");
        }

        // 先从缓存获取
        BACnetDeviceInfo cachedDevice = deviceCache.get(instanceNumber);
        if (cachedDevice != null) {
            return cachedDevice;
        }

        // 缓存未命中，从网络获取
        try {
            BACnetDeviceInfo device = bacnetClient.getDeviceInfo(instanceNumber);
            deviceCache.put(instanceNumber, device);
            return device;
        } catch (Exception e) {
            log.error("获取设备信息失败: {}", instanceNumber, e);
            throw new RuntimeException("获取设备信息失败", e);
        }
    }

    /**
     * 获取所有已缓存的设备
     */
    public List<BACnetDeviceInfo> getAllCachedDevices() {
        return List.copyOf(deviceCache.values());
    }

    /**
     * 读取设备属性
     */
    public Object readDeviceProperty(Integer deviceInstanceNumber, ObjectType objectType,
            Integer objectInstanceNumber, PropertyIdentifier propertyId) {
        if (!isEnabled()) {
            throw new IllegalStateException("BACnet 协议未启用");
        }

        try {
            return bacnetClient.readProperty(deviceInstanceNumber, objectType, objectInstanceNumber, propertyId);
        } catch (Exception e) {
            log.error("读取设备属性失败: device={}, objectType={}, objectInstance={}, property={}",
                    deviceInstanceNumber, objectType, objectInstanceNumber, propertyId, e);
            throw new RuntimeException("读取设备属性失败", e);
        }
    }

    /**
     * 获取设备的所有对象
     */
    public List<BACnetObjectInfo> getDeviceObjects(Integer instanceNumber) {
        if (!isEnabled()) {
            throw new IllegalStateException("BACnet 协议未启用");
        }

        try {
            return bacnetClient.getDeviceObjects(instanceNumber);
        } catch (Exception e) {
            log.error("获取设备对象失败: {}", instanceNumber, e);
            throw new RuntimeException("获取设备对象失败", e);
        }
    }

    /**
     * 检查 BACnet 是否启用
     */
    public boolean isEnabled() {
        return properties.getEnabled() && bacnetClient != null && bacnetClient.isInitialized();
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        if (bacnetClient != null) {
            bacnetClient.shutdown();
            deviceCache.clear();
            log.info("BACnet 设备管理器已关闭");
        }
    }

    /**
     * 写入设备属性
     *
     * @param deviceInstanceNumber BACnet 设备实例号
     * @param objectType           对象类型
     * @param objectInstanceNumber 对象实例号
     * @param propertyId           属性标识符
     * @param value                属性值
     * @param priority             写入优先级（1-16，可选）
     */
    public void writeProperty(Integer deviceInstanceNumber,
            com.serotonin.bacnet4j.type.enumerated.ObjectType objectType,
            Integer objectInstanceNumber,
            com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier propertyId,
            Object value,
            Integer priority) {
        if (!isEnabled()) {
            throw new IllegalStateException("BACnet 协议未启用");
        }

        try {
            bacnetClient.writeProperty(deviceInstanceNumber, objectType, objectInstanceNumber,
                    propertyId, value, priority);
            log.info("[writeProperty][写入属性成功] device={}, objectType={}, objectInstance={}, property={}, value={}",
                    deviceInstanceNumber, objectType, objectInstanceNumber, propertyId, value);
        } catch (Exception e) {
            log.error("[writeProperty][写入属性失败] device={}, objectType={}, objectInstance={}, property={}",
                    deviceInstanceNumber, objectType, objectInstanceNumber, propertyId, e);
            throw new RuntimeException("写入属性失败", e);
        }
    }

    /**
     * 获取 BACnet 客户端
     */
    public BACnetClient getBacnetClient() {
        return bacnetClient;
    }

    /**
     * 获取 BACnet 监听端口
     */
    /**
     * 是否启用轮询采集
     */
    public boolean isPollingEnabled() {
        return Boolean.TRUE.equals(properties.getPollingEnabled());
    }

    public Integer getPort() {
        return properties.getPort();
    }

}
