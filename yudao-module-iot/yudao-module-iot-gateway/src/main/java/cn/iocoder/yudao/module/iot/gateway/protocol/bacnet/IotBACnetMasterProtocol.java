package cn.iocoder.yudao.module.iot.gateway.protocol.bacnet;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.util.BACnetUtils;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetPropertyMappingMapper;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 BACnet 主站协议：主动读取 BACnet 设备数据
 *
 * @author 芋道源码
 */
@Slf4j
public class IotBACnetMasterProtocol {

    private final BACnetDeviceManager bacnetDeviceManager;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageProducer deviceMessageProducer;

    private final IotBACnetDeviceConfigMapper deviceConfigMapper;

    private final IotBACnetPropertyMappingMapper propertyMappingMapper;

    @Getter
    private final String serverId;

    /**
     * 轮询调度器
     */
    private ScheduledExecutorService pollingScheduler;

    public IotBACnetMasterProtocol(BACnetDeviceManager bacnetDeviceManager,
            IotDeviceService deviceService,
            IotDeviceMessageProducer deviceMessageProducer,
            IotBACnetDeviceConfigMapper deviceConfigMapper,
            IotBACnetPropertyMappingMapper propertyMappingMapper) {
        this.bacnetDeviceManager = bacnetDeviceManager;
        this.deviceService = deviceService;
        this.deviceMessageProducer = deviceMessageProducer;
        this.deviceConfigMapper = deviceConfigMapper;
        this.propertyMappingMapper = propertyMappingMapper;
        // 生成服务器 ID（使用配置的 BACnet 端口）
        this.serverId = IotDeviceMessageUtils.generateServerId(bacnetDeviceManager.getPort());
    }

    @PostConstruct
    public void start() {
        if (!bacnetDeviceManager.isEnabled()) {
            log.info("[start][BACnet 协议未启用，跳过启动]");
            return;
        }

        // 防止重复启动
        if (pollingScheduler != null) {
            log.warn("[start][BACnet 主站协议已启动，跳过重复启动]");
            return;
        }

        // 获取启用轮询的设备配置
        List<IotBACnetDeviceConfigDO> configs = deviceConfigMapper.selectEnabledPollingConfigs();
        if (configs == null || configs.isEmpty()) {
            log.warn("[start][未配置启用轮询的 BACnet 设备]");
            return;
        }

        // 检查是否有重复的设备配置
        Map<Long, Long> deviceIdCount = configs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        IotBACnetDeviceConfigDO::getDeviceId,
                        java.util.stream.Collectors.counting()));
        deviceIdCount.forEach((deviceId, count) -> {
            if (count > 1) {
                log.warn("[start][发现重复的设备配置，设备 ID: {}, 数量: {}]", deviceId, count);
            }
        });

        // 去重：每个设备只启动一个轮询任务
        List<IotBACnetDeviceConfigDO> distinctConfigs = configs.stream()
                .collect(java.util.stream.Collectors.toMap(
                        IotBACnetDeviceConfigDO::getDeviceId,
                        c -> c,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(java.util.stream.Collectors.toList());

        // 创建轮询调度器
        int deviceCount = distinctConfigs.size();
        pollingScheduler = Executors.newScheduledThreadPool(
                Math.min(deviceCount, Runtime.getRuntime().availableProcessors()),
                r -> new Thread(r, "bacnet-polling-" + Thread.currentThread().getId()));

        // 为每个设备启动轮询任务
        for (IotBACnetDeviceConfigDO config : distinctConfigs) {
            startPollingForDevice(config);
        }

        log.info("[start][BACnet 主站协议启动成功，服务器 ID: {}, 设备数量: {}]", serverId, deviceCount);
    }

    @PreDestroy
    public void stop() {
        if (pollingScheduler != null) {
            try {
                pollingScheduler.shutdown();
                if (!pollingScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    pollingScheduler.shutdownNow();
                }
                log.info("[stop][BACnet 主站协议已停止]");
            } catch (InterruptedException e) {
                pollingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("[stop][BACnet 主站协议停止失败]", e);
            }
        }
    }

    /**
     * 为设备启动轮询任务
     */
    private void startPollingForDevice(IotBACnetDeviceConfigDO config) {
        // 获取该设备启用轮询的属性映射
        List<IotBACnetPropertyMappingDO> mappings = propertyMappingMapper
                .selectEnabledPollingMappings(config.getDeviceId());
        if (mappings == null || mappings.isEmpty()) {
            log.warn("[startPollingForDevice][设备未配置轮询属性映射，设备 ID: {}]", config.getDeviceId());
            return;
        }

        // 定时轮询任务
        pollingScheduler.scheduleAtFixedRate(
                () -> pollDevice(config, mappings),
                0, // 立即开始
                config.getPollingInterval(),
                TimeUnit.MILLISECONDS);

        log.info("[startPollingForDevice][启动设备轮询，设备 ID: {}, Instance: {}, 轮询项数量: {}, 轮询间隔: {}ms]",
                config.getDeviceId(), config.getInstanceNumber(), mappings.size(), config.getPollingInterval());
    }

    /**
     * 轮询设备
     */
    private void pollDevice(IotBACnetDeviceConfigDO config, List<IotBACnetPropertyMappingDO> mappings) {
        try {
            // 存储所有读取的属性值
            Map<String, Object> properties = new HashMap<>();

            // 遍历所有属性映射
            for (IotBACnetPropertyMappingDO mapping : mappings) {
                try {
                    Object value = readBACnetProperty(config, mapping);
                    if (value != null) {
                        // 数据转换（如果配置了单位转换）
                        value = convertValue(value, mapping);
                        properties.put(mapping.getIdentifier(), value);
                    }
                } catch (Exception e) {
                    log.error("[pollDevice][读取 BACnet 属性失败，设备 ID: {}, 标识符: {}, 对象类型: {}, 对象实例: {}]",
                            config.getDeviceId(), mapping.getIdentifier(), mapping.getObjectType(),
                            mapping.getObjectInstance(), e);
                }
            }

            // 如果成功读取到数据，发布到消息总线
            if (!properties.isEmpty()) {
                publishDeviceMessage(config, properties);
            }

        } catch (Exception e) {
            log.error("[pollDevice][轮询设备失败，设备 ID: {}]", config.getDeviceId(), e);
        }
    }

    /**
     * 读取 BACnet 属性
     */
    private Object readBACnetProperty(IotBACnetDeviceConfigDO config, IotBACnetPropertyMappingDO mapping) {
        try {
            ObjectType objectType = BACnetUtils.getObjectTypeByName(mapping.getObjectType());
            PropertyIdentifier propertyId = BACnetUtils.getPropertyIdentifierByName(mapping.getPropertyIdentifier());

            Object value = bacnetDeviceManager.readDeviceProperty(
                    config.getInstanceNumber(),
                    objectType,
                    mapping.getObjectInstance(),
                    propertyId);

            return convertBACnetValue(value, mapping.getDataType());
        } catch (Exception e) {
            throw new RuntimeException("读取 BACnet 属性失败", e);
        }
    }

    /**
     * 转换 BACnet 值为 Java 类型
     */
    private Object convertBACnetValue(Object value, String dataType) {
        if (value == null) {
            return null;
        }

        try {
            // BACnet 值通常是 Encodable 类型，需要提取实际值
            if (value instanceof com.serotonin.bacnet4j.type.primitive.Real) {
                return ((com.serotonin.bacnet4j.type.primitive.Real) value).floatValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.primitive.Double) {
                return ((com.serotonin.bacnet4j.type.primitive.Double) value).doubleValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.primitive.UnsignedInteger) {
                return ((com.serotonin.bacnet4j.type.primitive.UnsignedInteger) value).intValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.primitive.SignedInteger) {
                return ((com.serotonin.bacnet4j.type.primitive.SignedInteger) value).intValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.primitive.Boolean) {
                return ((com.serotonin.bacnet4j.type.primitive.Boolean) value).booleanValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.primitive.CharacterString) {
                return ((com.serotonin.bacnet4j.type.primitive.CharacterString) value).getValue();
            } else if (value instanceof com.serotonin.bacnet4j.type.enumerated.BinaryPV) {
                return ((com.serotonin.bacnet4j.type.enumerated.BinaryPV) value).intValue();
            }

            // 其他类型，尝试转换为字符串
            return value.toString();
        } catch (Exception e) {
            log.warn("[convertBACnetValue][BACnet 值转换失败] value={}, dataType={}", value, dataType, e);
            return value;
        }
    }

    /**
     * 应用单位转换
     */
    private Object convertValue(Object value, IotBACnetPropertyMappingDO mapping) {
        // TODO: 实现单位转换公式
        // 如果配置了 unitConversion，应用转换公式
        if (mapping.getUnitConversion() != null && !mapping.getUnitConversion().isEmpty()) {
            // 简单示例：value * 0.1
            // 实际实现可以使用表达式引擎（如 MVEL、SpEL 等）
            log.debug("[convertValue][应用单位转换] value={}, formula={}", value, mapping.getUnitConversion());
        }

        // TODO: 实现值映射（枚举类型）
        if (mapping.getValueMapping() != null && !mapping.getValueMapping().isEmpty()) {
            // 从 JSON 映射中查找对应值
            log.debug("[convertValue][应用值映射] value={}, mapping={}", value, mapping.getValueMapping());
        }

        return value;
    }

    /**
     * 发布设备消息到消息总线
     */
    private void publishDeviceMessage(IotBACnetDeviceConfigDO config, Map<String, Object> properties) {
        try {
            // 获取设备信息
            IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(config.getDeviceId());
            if (deviceInfo == null) {
                log.error("[publishDeviceMessage][设备不存在，设备 ID: {}]", config.getDeviceId());
                return;
            }

            // 构建设备消息
            IotDeviceMessage message = new IotDeviceMessage();
            message.setId(IotDeviceMessageUtils.generateMessageId());
            message.setReportTime(LocalDateTime.now());
            message.setDeviceId(config.getDeviceId());
            message.setTenantId(deviceInfo.getTenantId());
            message.setServerId(serverId);
            message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
            message.setParams(properties);

            // 发布消息
            deviceMessageProducer.sendDeviceMessage(message);

            log.debug("[publishDeviceMessage][发布 BACnet 设备消息，设备 ID: {}，属性数量: {}]",
                    config.getDeviceId(), properties.size());

        } catch (Exception e) {
            log.error("[publishDeviceMessage][发布设备消息失败，设备 ID: {}]", config.getDeviceId(), e);
        }
    }

}
