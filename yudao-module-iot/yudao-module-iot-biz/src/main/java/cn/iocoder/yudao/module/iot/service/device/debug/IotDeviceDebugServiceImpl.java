package cn.iocoder.yudao.module.iot.service.device.debug;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDebugLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceDebugLogMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

/**
 * IoT 设备调试 Service 实现类
 *
 * @author AI
 */
@Service
@Validated
public class IotDeviceDebugServiceImpl implements IotDeviceDebugService {

    @Resource
    private IotDeviceDebugLogMapper debugLogMapper;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    public IotDeviceDebugResultVO simulatePropertyReport(IotDevicePropertyReportReqVO reqVO) {
        long startTime = System.currentTimeMillis();

        // 1. 校验设备存在
        IotDeviceDO device = validateDeviceExists(reqVO.getDeviceId());

        // 2. 构造消息
        String messageId = IdUtil.fastSimpleUUID();
        String payload = JSONUtil.createObj()
                .set("identifier", reqVO.getIdentifier())
                .set("value", parseValue(reqVO.getValue()))
                .set("timestamp", System.currentTimeMillis())
                .toString();

        // 3. TODO: 发送到消息总线（模拟设备上报）
        // 此处需要集成现有的消息处理框架
        // messageService.publishPropertyReport(device.getProductKey(),
        // device.getDeviceName(), payload);

        int latency = (int) (System.currentTimeMillis() - startTime);

        // 4. 记录日志
        saveDebugLog(device, IotDeviceDebugLogDO.DIRECTION_UP,
                IotDeviceDebugLogDO.TYPE_PROPERTY_REPORT,
                reqVO.getIdentifier(), payload, null, 1, null, latency);

        return IotDeviceDebugResultVO.success(messageId, payload, latency);
    }

    @Override
    public IotDeviceDebugResultVO sendPropertySet(Long deviceId, String identifier, String value) {
        long startTime = System.currentTimeMillis();

        // 1. 校验设备存在
        IotDeviceDO device = validateDeviceExists(deviceId);

        // 2. 构造消息
        String messageId = IdUtil.fastSimpleUUID();
        String payload = JSONUtil.createObj()
                .set("identifier", identifier)
                .set("value", parseValue(value))
                .set("timestamp", System.currentTimeMillis())
                .toString();

        // 3. TODO: 发送到设备（下行消息）
        // 此处需要集成现有的设备通信框架
//         deviceCommandService.sendPropertySet(device, identifier, value);

        int latency = (int) (System.currentTimeMillis() - startTime);

        // 4. 记录日志
        saveDebugLog(device, IotDeviceDebugLogDO.DIRECTION_DOWN,
                IotDeviceDebugLogDO.TYPE_PROPERTY_SET,
                identifier, payload, null, 1, null, latency);

        return IotDeviceDebugResultVO.success(messageId, payload, latency);
    }

    @Override
    public IotDeviceDebugResultVO invokeService(IotDeviceServiceInvokeReqVO reqVO) {
        long startTime = System.currentTimeMillis();

        // 1. 校验设备存在
        IotDeviceDO device = validateDeviceExists(reqVO.getDeviceId());

        // 2. 构造消息
        String messageId = IdUtil.fastSimpleUUID();
        String payload = JSONUtil.createObj()
                .set("identifier", reqVO.getIdentifier())
                .set("inputParams", reqVO.getInputParams())
                .set("timestamp", System.currentTimeMillis())
                .toString();

        // 3. TODO: 调用设备服务
        // 此处需要集成现有的设备服务调用框架
        // Object result = deviceCommandService.invokeService(device,
        // reqVO.getIdentifier(), reqVO.getInputParams());

        int latency = (int) (System.currentTimeMillis() - startTime);

        // 4. 记录日志
        saveDebugLog(device, IotDeviceDebugLogDO.DIRECTION_DOWN,
                IotDeviceDebugLogDO.TYPE_SERVICE_INVOKE,
                reqVO.getIdentifier(), payload, null, 1, null, latency);

        return IotDeviceDebugResultVO.success(messageId, payload, latency);
    }

    @Override
    public PageResult<IotDeviceDebugLogDO> getDebugLogPage(IotDeviceDebugLogPageReqVO pageReqVO) {
        return debugLogMapper.selectPage(pageReqVO);
    }

    @Override
    public int cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        return debugLogMapper.deleteBeforeTime(expireTime);
    }

    // ========== 私有方法 ==========

    private IotDeviceDO validateDeviceExists(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    private void saveDebugLog(IotDeviceDO device, int direction, String type,
            String identifier, String payload, String result,
            int status, String errorMessage, int latency) {
        IotDeviceDebugLogDO log = IotDeviceDebugLogDO.builder()
                .deviceId(device.getId())
                .deviceKey(device.getDeviceName())
                .productKey(device.getProductKey())
                .direction(direction)
                .type(type)
                .identifier(identifier)
                .payload(payload)
                .result(result)
                .status(status)
                .errorMessage(errorMessage)
                .latency(latency)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(LocalDateTime.now())
                .build();
        debugLogMapper.insert(log);
    }

    private Object parseValue(String value) {
        // 尝试解析为数字或布尔值
        try {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            }
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value; // 保持字符串
        }
    }

}
