package cn.iocoder.yudao.module.iot.service.bacnet;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryBindReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordPageReqVO;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetConfigConvert;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetDiscoveryConvert;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetDeviceInfo;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDiscoveryRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDiscoveryRecordMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * BACnet 设备发现 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotBACnetDiscoveryServiceImpl implements IotBACnetDiscoveryService {

    @Resource
    private BACnetDeviceManager bacnetDeviceManager;

    @Resource
    private IotBACnetDiscoveryRecordMapper discoveryRecordMapper;

    @Resource
    private IotBACnetDeviceConfigMapper deviceConfigMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public List<IotBACnetDiscoveryRecordDO> discoverDevicesNow() {
        return discoverAndRecordDevices();
    }

    @Override
    @Scheduled(cron = "${yudao.iot.bacnet.discovery-cron:0 */5 * * * ?}")  // 默认每5分钟
    public void scheduleDeviceDiscovery() {
        if (!bacnetDeviceManager.isEnabled()) {
            log.debug("[scheduleDeviceDiscovery][BACnet 未启用，跳过设备发现]");
            return;
        }

        log.info("[scheduleDeviceDiscovery][开始定时发现 BACnet 设备]");
        discoverAndRecordDevices();
    }

    /**
     * 发现并记录设备
     */
    private List<IotBACnetDiscoveryRecordDO> discoverAndRecordDevices() {
        try {
            // 1. 调用 BACnetDeviceManager 发现设备
            List<BACnetDeviceInfo> devices = bacnetDeviceManager.discoverAndCacheDevices();
            log.info("[discoverAndRecordDevices][发现 {} 个 BACnet 设备]", devices.size());

            // 2. 遍历设备，保存或更新发现记录
            List<IotBACnetDiscoveryRecordDO> records = new ArrayList<>();
            for (BACnetDeviceInfo deviceInfo : devices) {
                IotBACnetDiscoveryRecordDO record = discoveryRecordMapper.selectByInstanceNumber(
                        deviceInfo.getInstanceNumber()
                );

                if (record == null) {
                    // 新设备，创建记录
                    record = IotBACnetDiscoveryConvert.INSTANCE.convert(deviceInfo);
                    // 获取对象列表（如果支持）
                    try {
                        // TODO: 实现获取对象列表的逻辑
                        // List<BACnetObjectInfo> objects = bacnetDeviceManager.getDeviceObjects(deviceInfo.getInstanceNumber());
                        // record.setObjectList(JSON.toJSONString(objects));
                    } catch (Exception e) {
                        log.warn("[discoverAndRecordDevices][获取设备对象列表失败] instanceNumber={}",
                                deviceInfo.getInstanceNumber(), e);
                    }
                    discoveryRecordMapper.insert(record);
                    log.info("[discoverAndRecordDevices][发现新设备] instanceNumber={}, name={}",
                            deviceInfo.getInstanceNumber(), deviceInfo.getDeviceName());
                } else {
                    // 已有设备，更新最后发现时间和在线状态
                    record.setLastSeenTime(LocalDateTime.now());
                    record.setStatus(1); // 在线
                    // 更新设备信息（可能已变更）
                    record.setDeviceName(deviceInfo.getDeviceName());
                    record.setIpAddress(deviceInfo.getIpAddress());
                    record.setVendorName(deviceInfo.getVendorName());
                    record.setModelName(deviceInfo.getModelName());
                    discoveryRecordMapper.updateById(record);
                }

                records.add(record);
            }

            // 3. 标记超过10分钟未发现的设备为离线
            discoveryRecordMapper.markOfflineDevices(LocalDateTime.now().minusMinutes(10));

            return records;
        } catch (Exception e) {
            log.error("[discoverAndRecordDevices][发现设备失败]", e);
            throw new RuntimeException("发现 BACnet 设备失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindDevice(IotBACnetDiscoveryBindReqVO bindReqVO) {
        // 1. 校验发现记录存在
        IotBACnetDiscoveryRecordDO discoveryRecord = discoveryRecordMapper.selectById(bindReqVO.getDiscoveryRecordId());
        if (discoveryRecord == null) {
            throw exception(BACNET_DISCOVERY_RECORD_NOT_EXISTS);
        }

        // 2. 校验设备未被绑定
        if (Boolean.TRUE.equals(discoveryRecord.getIsBound())) {
            throw exception(BACNET_DISCOVERY_ALREADY_BOUND);
        }

        Long deviceId;

        // 3. 绑定到已有设备或创建新设备
        if (bindReqVO.getDeviceId() != null) {
            // 绑定到已有设备
            deviceId = bindReqVO.getDeviceId();
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                throw exception(DEVICE_NOT_EXISTS);
            }
        } else if (StrUtil.isNotBlank(bindReqVO.getDeviceName()) && bindReqVO.getProductId() != null) {
            // 创建新设备
            // TODO: 调用 deviceService.createDevice() 创建设备
            // 这里需要构建 IotDeviceSaveReqVO
            throw new UnsupportedOperationException("创建新设备功能待实现");
        } else {
            throw exception(BACNET_BIND_PARAM_ERROR);
        }

        // 4. 创建 BACnet 设备配置
        IotBACnetDeviceConfigDO config = IotBACnetDeviceConfigDO.builder()
                .deviceId(deviceId)
                .instanceNumber(discoveryRecord.getInstanceNumber())
                .ipAddress(discoveryRecord.getIpAddress())
                .port(discoveryRecord.getPort())
                .vendorId(discoveryRecord.getVendorId())
                .pollingEnabled(true)
                .pollingInterval(5000)
                .readTimeout(5000)
                .retryCount(3)
                .status(1)
                .build();
        deviceConfigMapper.insert(config);

        // 5. 更新发现记录为已绑定
        discoveryRecord.setIsBound(true);
        discoveryRecord.setBoundDeviceId(deviceId);
        discoveryRecordMapper.updateById(discoveryRecord);

        // 6. 如果启用智能映射，自动创建属性映射
        if (Boolean.TRUE.equals(bindReqVO.getEnableSmartMapping())) {
            // TODO: 实现智能映射逻辑
            log.info("[bindDevice][智能映射功能待实现] deviceId={}", deviceId);
        }

        log.info("[bindDevice][绑定 BACnet 设备成功] discoveryRecordId={}, deviceId={}",
                bindReqVO.getDiscoveryRecordId(), deviceId);

        return deviceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscoveryRecord(Long id) {
        // 校验存在
        IotBACnetDiscoveryRecordDO record = discoveryRecordMapper.selectById(id);
        if (record == null) {
            throw exception(BACNET_DISCOVERY_RECORD_NOT_EXISTS);
        }

        // 如果已绑定，不允许删除
        if (Boolean.TRUE.equals(record.getIsBound())) {
            throw exception(BACNET_DISCOVERY_ALREADY_BOUND);
        }

        // 删除
        discoveryRecordMapper.deleteById(id);
    }

    @Override
    public IotBACnetDiscoveryRecordDO getDiscoveryRecord(Long id) {
        return discoveryRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotBACnetDiscoveryRecordDO> getDiscoveryRecordPage(IotBACnetDiscoveryRecordPageReqVO pageReqVO) {
        return discoveryRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotBACnetDiscoveryRecordDO> getUnboundDevices() {
        return discoveryRecordMapper.selectUnboundDevices();
    }

    @Override
    public List<IotBACnetDiscoveryRecordDO> getBoundDevices() {
        return discoveryRecordMapper.selectBoundDevices();
    }

}
