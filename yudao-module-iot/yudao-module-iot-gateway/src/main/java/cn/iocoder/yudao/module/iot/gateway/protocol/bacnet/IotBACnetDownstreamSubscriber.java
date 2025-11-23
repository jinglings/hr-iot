package cn.iocoder.yudao.module.iot.gateway.protocol.bacnet;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetPropertyMappingMapper;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * IoT 网关 BACnet 下行消息订阅器：处理属性写入命令
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotBACnetDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final BACnetDeviceManager bacnetDeviceManager;

    private final IotBACnetMasterProtocol masterProtocol;

    private final IotBACnetDeviceConfigMapper deviceConfigMapper;

    private final IotBACnetPropertyMappingMapper propertyMappingMapper;

    private final IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        if (!bacnetDeviceManager.isEnabled()) {
            log.info("[init][BACnet 协议未启用，跳过下行消息订阅]");
            return;
        }

        // 注册订阅器
        messageBus.register(this);
        log.info("[init][BACnet 下行消息订阅器初始化完成，服务器 ID: {}, Topic: {}]",
                masterProtocol.getServerId(), getTopic());
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(masterProtocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        try {
            // 处理不同类型的消息
            String method = message.getMethod();
            if (IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod().equals(method)) {
                handlePropertySet(message);
            } else {
                log.debug("[onMessage][不支持的消息方法，忽略] method={}", method);
            }

        } catch (Exception e) {
            log.error("[onMessage][处理 BACnet 下行消息失败] deviceId={}, method={}, messageId={}",
                    message.getDeviceId(), message.getMethod(), message.getId(), e);
        }
    }

    /**
     * 处理属性设置命令
     */
    @SuppressWarnings("unchecked")
    private void handlePropertySet(IotDeviceMessage message) {
        Long deviceId = message.getDeviceId();
        Object paramsObj = message.getParams();

        // 类型检查和转换
        if (!(paramsObj instanceof Map)) {
            log.warn("[handlePropertySet][参数类型错误] deviceId={}, paramsType={}",
                    deviceId, paramsObj != null ? paramsObj.getClass() : "null");
            return;
        }

        Map<String, Object> params = (Map<String, Object>) paramsObj;

        if (params == null || params.isEmpty()) {
            log.warn("[handlePropertySet][参数为空] deviceId={}", deviceId);
            return;
        }

        // 获取设备配置
        IotBACnetDeviceConfigDO config = deviceConfigMapper.selectByDeviceId(deviceId);
        if (config == null) {
            log.error("[handlePropertySet][设备配置不存在] deviceId={}", deviceId);
            return;
        }

        // 逐个写入属性
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String identifier = entry.getKey();
            Object value = entry.getValue();

            try {
                // 查找属性映射
                IotBACnetPropertyMappingDO mapping = propertyMappingMapper
                        .selectByDeviceIdAndIdentifier(deviceId, identifier);

                if (mapping == null) {
                    log.warn("[handlePropertySet][属性映射不存在，跳过] deviceId={}, identifier={}",
                            deviceId, identifier);
                    continue;
                }

                // 验证写入权限
                if (!isWritable(mapping.getAccessMode())) {
                    log.warn("[handlePropertySet][属性不可写，跳过] deviceId={}, identifier={}, accessMode={}",
                            deviceId, identifier, mapping.getAccessMode());
                    continue;
                }

                // 写入 BACnet 属性
                writeProperty(config, mapping, value);
                log.info("[handlePropertySet][写入成功] deviceId={}, identifier={}, value={}",
                        deviceId, identifier, value);

            } catch (Exception e) {
                log.error("[handlePropertySet][写入失败] deviceId={}, identifier={}, value={}",
                        deviceId, identifier, value, e);
            }
        }
    }

    /**
     * 写入 BACnet 属性
     */
    private void writeProperty(IotBACnetDeviceConfigDO config,
                               IotBACnetPropertyMappingDO mapping,
                               Object value) {
        try {
            ObjectType objectType = ObjectType.forName(mapping.getObjectType());
            PropertyIdentifier propertyId = PropertyIdentifier.forName(mapping.getPropertyIdentifier());

            // 应用值映射（枚举转换）
            value = applyValueMapping(value, mapping);

            // 应用单位转换
            value = applyUnitConversion(value, mapping);

            // 写入属性
            bacnetDeviceManager.writeProperty(
                    config.getInstanceNumber(),
                    objectType,
                    mapping.getObjectInstance(),
                    propertyId,
                    value,
                    mapping.getPriority()
            );

        } catch (Exception e) {
            throw new RuntimeException("写入 BACnet 属性失败: " + e.getMessage(), e);
        }
    }

    /**
     * 应用值映射（枚举类型转换）
     */
    private Object applyValueMapping(Object value, IotBACnetPropertyMappingDO mapping) {
        // TODO: 实现值映射
        // 如果配置了 valueMapping（JSON格式），根据映射表转换值
        // 例如: {"开": 1, "关": 0}
        if (mapping.getValueMapping() != null && !mapping.getValueMapping().isEmpty()) {
            log.debug("[applyValueMapping][应用值映射] value={}, mapping={}",
                    value, mapping.getValueMapping());
            // 可以使用 JSON 解析和查找
        }
        return value;
    }

    /**
     * 应用单位转换（反向转换）
     */
    private Object applyUnitConversion(Object value, IotBACnetPropertyMappingDO mapping) {
        // TODO: 实现单位转换
        // 如果配置了 unitConversion，应用反向转换公式
        // 例如读取时是 value * 0.1，写入时应该是 value / 0.1
        if (mapping.getUnitConversion() != null && !mapping.getUnitConversion().isEmpty()) {
            log.debug("[applyUnitConversion][应用单位转换] value={}, formula={}",
                    value, mapping.getUnitConversion());
            // 可以使用表达式引擎（如 MVEL、SpEL 等）
        }
        return value;
    }

    /**
     * 检查属性是否可写
     */
    private boolean isWritable(String accessMode) {
        if (accessMode == null) {
            return false;
        }
        // accessMode: r(只读), w(只写), rw(读写)
        return accessMode.contains("w");
    }

}
