package cn.iocoder.yudao.module.iot.service.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetConfigConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetPropertyMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * BACnet 配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotBACnetConfigServiceImpl implements IotBACnetConfigService {

    @Resource
    private IotBACnetDeviceConfigMapper deviceConfigMapper;

    @Resource
    private IotBACnetPropertyMappingMapper propertyMappingMapper;

    // ========== 设备配置管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeviceConfig(IotBACnetDeviceConfigSaveReqVO createReqVO) {
        // 校验设备是否已有配置
        IotBACnetDeviceConfigDO existingConfig = deviceConfigMapper.selectByDeviceId(createReqVO.getDeviceId());
        if (existingConfig != null) {
            throw exception(BACNET_DEVICE_CONFIG_EXISTS);
        }

        // 校验实例号是否已被使用
//        IotBACnetDeviceConfigDO existingByInstance = deviceConfigMapper.selectByInstanceNumber(createReqVO.getInstanceNumber());
//        if (existingByInstance != null) {
//            throw exception(BACNET_INSTANCE_NUMBER_EXISTS);
//        }

        // 插入
        IotBACnetDeviceConfigDO config = IotBACnetConfigConvert.INSTANCE.convert(createReqVO);
        deviceConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceConfig(IotBACnetDeviceConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateDeviceConfigExists(updateReqVO.getId());

        // 校验实例号是否被其他配置使用
        IotBACnetDeviceConfigDO existingByInstance = deviceConfigMapper.selectByInstanceNumber(updateReqVO.getInstanceNumber());
        if (existingByInstance != null && !existingByInstance.getId().equals(updateReqVO.getId())) {
            throw exception(BACNET_INSTANCE_NUMBER_EXISTS);
        }

        // 更新
        IotBACnetDeviceConfigDO updateObj = IotBACnetConfigConvert.INSTANCE.convert(updateReqVO);
        deviceConfigMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceConfig(Long id) {
        // 校验存在
        validateDeviceConfigExists(id);

        // 删除关联的属性映射
        List<IotBACnetPropertyMappingDO> mappings = propertyMappingMapper.selectListByDeviceConfigId(id);
        if (!mappings.isEmpty()) {
            mappings.forEach(mapping -> propertyMappingMapper.deleteById(mapping.getId()));
        }

        // 删除配置
        deviceConfigMapper.deleteById(id);
    }

    @Override
    public IotBACnetDeviceConfigDO getDeviceConfig(Long id) {
        return deviceConfigMapper.selectById(id);
    }

    @Override
    public IotBACnetDeviceConfigDO getDeviceConfigByDeviceId(Long deviceId) {
        return deviceConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public PageResult<IotBACnetDeviceConfigDO> getDeviceConfigPage(IotBACnetDeviceConfigPageReqVO pageReqVO) {
        return deviceConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotBACnetDeviceConfigDO> getEnabledPollingConfigs() {
        return deviceConfigMapper.selectEnabledPollingConfigs();
    }

    private void validateDeviceConfigExists(Long id) {
        if (deviceConfigMapper.selectById(id) == null) {
            throw exception(BACNET_DEVICE_CONFIG_NOT_EXISTS);
        }
    }

    // ========== 属性映射管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPropertyMapping(IotBACnetPropertyMappingSaveReqVO createReqVO) {
        // 校验设备配置存在
        validateDeviceConfigExists(createReqVO.getDeviceConfigId());

        // 校验是否已存在相同的映射（同一设备的同一BACnet对象属性）
        IotBACnetPropertyMappingDO existing = propertyMappingMapper.selectByDeviceObjectProperty(
                createReqVO.getDeviceId(),
                createReqVO.getObjectType(),
                createReqVO.getObjectInstance(),
                createReqVO.getPropertyIdentifier()
        );
        if (existing != null) {
            throw exception(BACNET_PROPERTY_MAPPING_EXISTS);
        }

        // 插入
        IotBACnetPropertyMappingDO mapping = IotBACnetConfigConvert.INSTANCE.convert(createReqVO);
        propertyMappingMapper.insert(mapping);
        return mapping.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePropertyMapping(IotBACnetPropertyMappingSaveReqVO updateReqVO) {
        // 校验存在
        validatePropertyMappingExists(updateReqVO.getId());

        // 校验是否已存在相同的映射
        IotBACnetPropertyMappingDO existing = propertyMappingMapper.selectByDeviceObjectProperty(
                updateReqVO.getDeviceId(),
                updateReqVO.getObjectType(),
                updateReqVO.getObjectInstance(),
                updateReqVO.getPropertyIdentifier()
        );
        if (existing != null && !existing.getId().equals(updateReqVO.getId())) {
            throw exception(BACNET_PROPERTY_MAPPING_EXISTS);
        }

        // 更新
        IotBACnetPropertyMappingDO updateObj = IotBACnetConfigConvert.INSTANCE.convert(updateReqVO);
        propertyMappingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePropertyMapping(Long id) {
        // 校验存在
        validatePropertyMappingExists(id);

        // 删除
        propertyMappingMapper.deleteById(id);
    }

    @Override
    public IotBACnetPropertyMappingDO getPropertyMapping(Long id) {
        return propertyMappingMapper.selectById(id);
    }

    @Override
    public PageResult<IotBACnetPropertyMappingDO> getPropertyMappingPage(IotBACnetPropertyMappingPageReqVO pageReqVO) {
        return propertyMappingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotBACnetPropertyMappingDO> getPropertyMappingsByDeviceId(Long deviceId) {
        return propertyMappingMapper.selectListByDeviceId(deviceId);
    }

    @Override
    public List<IotBACnetPropertyMappingDO> getEnabledPollingMappings(Long deviceId) {
        return propertyMappingMapper.selectEnabledPollingMappings(deviceId);
    }

    @Override
    public IotBACnetPropertyMappingDO getPropertyMappingByIdentifier(Long deviceId, String identifier) {
        return propertyMappingMapper.selectByDeviceIdAndIdentifier(deviceId, identifier);
    }

    private void validatePropertyMappingExists(Long id) {
        if (propertyMappingMapper.selectById(id) == null) {
            throw exception(BACNET_PROPERTY_MAPPING_NOT_EXISTS);
        }
    }

}
