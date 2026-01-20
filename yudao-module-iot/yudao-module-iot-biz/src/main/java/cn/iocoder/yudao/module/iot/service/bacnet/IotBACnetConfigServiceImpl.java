package cn.iocoder.yudao.module.iot.service.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetConfigConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetPropertyMappingMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotThingModelService thingModelService;

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
        // IotBACnetDeviceConfigDO existingByInstance =
        // deviceConfigMapper.selectByInstanceNumber(createReqVO.getInstanceNumber());
        // if (existingByInstance != null) {
        // throw exception(BACNET_INSTANCE_NUMBER_EXISTS);
        // }

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
        IotBACnetDeviceConfigDO existingByInstance = deviceConfigMapper
                .selectByInstanceNumber(updateReqVO.getInstanceNumber());
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
                createReqVO.getPropertyIdentifier());
        if (existing != null) {
            throw exception(BACNET_PROPERTY_MAPPING_EXISTS);
        }

        // 插入当前请求的映射
        IotBACnetPropertyMappingDO mapping = IotBACnetConfigConvert.INSTANCE.convert(createReqVO);
        propertyMappingMapper.insert(mapping);
        Long createdId = mapping.getId();

        // 自动创建其他物模型属性的映射
        autoCreateRelatedPropertyMappings(createReqVO);

        return createdId;
    }

    /**
     * 自动创建该设备其他物模型属性的 BACnet 映射
     * <p>
     * 当创建一个属性映射时，会自动为同一设备的所有其他物模型属性创建相应的映射。
     * 新创建的映射会复用原映射的配置（如 objectType、propertyIdentifier、accessMode 等），
     * 只有 thingModelId、identifier 和 objectInstance 会根据各自的物模型属性进行设置。
     *
     * @param createReqVO 原始创建请求
     */
    private void autoCreateRelatedPropertyMappings(IotBACnetPropertyMappingSaveReqVO createReqVO) {
        // 获取设备信息
        IotDeviceDO device = deviceService.getDevice(createReqVO.getDeviceId());
        if (device == null || device.getProductId() == null) {
            log.warn("[autoCreateRelatedPropertyMappings][设备 {} 不存在或无产品信息]", createReqVO.getDeviceId());
            return;
        }

        // 获取产品的所有物模型属性（type = 1 表示属性类型）
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        if (thingModels == null || thingModels.isEmpty()) {
            log.info("[autoCreateRelatedPropertyMappings][产品 {} 没有物模型属性]", device.getProductId());
            return;
        }

        // 获取该设备已存在的所有属性映射，找出已映射的 identifier
        List<IotBACnetPropertyMappingDO> existingMappings = propertyMappingMapper
                .selectListByDeviceId(createReqVO.getDeviceId());
        Set<String> mappedIdentifiers = existingMappings.stream()
                .map(IotBACnetPropertyMappingDO::getIdentifier)
                .collect(Collectors.toSet());

        // 找出当前设备已使用的最大 objectInstance
        int maxObjectInstance = existingMappings.stream()
                .filter(m -> createReqVO.getObjectType().equals(m.getObjectType()))
                .mapToInt(IotBACnetPropertyMappingDO::getObjectInstance)
                .max()
                .orElse(createReqVO.getObjectInstance() - 1);

        int nextObjectInstance = maxObjectInstance + 1;

        // 遍历所有物模型属性，为未映射的属性创建映射
        for (IotThingModelDO thingModel : thingModels) {
            // 跳过已映射的属性
            if (mappedIdentifiers.contains(thingModel.getIdentifier())) {
                continue;
            }

            // 校验该 objectInstance 是否已被使用
            IotBACnetPropertyMappingDO existingByInstance = propertyMappingMapper.selectByDeviceObjectProperty(
                    createReqVO.getDeviceId(),
                    createReqVO.getObjectType(),
                    nextObjectInstance,
                    createReqVO.getPropertyIdentifier());
            if (existingByInstance != null) {
                // 如果已被使用，继续寻找下一个可用的 objectInstance
                nextObjectInstance++;
                continue;
            }

            // 创建新的映射
            IotBACnetPropertyMappingDO newMapping = new IotBACnetPropertyMappingDO();
            newMapping.setDeviceConfigId(createReqVO.getDeviceConfigId());
            newMapping.setDeviceId(createReqVO.getDeviceId());
            newMapping.setThingModelId(thingModel.getId());
            newMapping.setIdentifier(thingModel.getIdentifier());
            newMapping.setObjectType(createReqVO.getObjectType());
            newMapping.setObjectInstance(nextObjectInstance);
            newMapping.setPropertyIdentifier(createReqVO.getPropertyIdentifier());
            newMapping.setPropertyArrayIndex(createReqVO.getPropertyArrayIndex());
            newMapping.setDataType(createReqVO.getDataType());
            newMapping.setUnitConversion(createReqVO.getUnitConversion());
            newMapping.setValueMapping(createReqVO.getValueMapping());
            newMapping.setAccessMode(createReqVO.getAccessMode());
            newMapping.setPriority(createReqVO.getPriority());
            newMapping.setPollingEnabled(createReqVO.getPollingEnabled());
            newMapping.setCovEnabled(createReqVO.getCovEnabled());
            newMapping.setStatus(createReqVO.getStatus());
            newMapping.setSort(createReqVO.getSort());

            propertyMappingMapper.insert(newMapping);
            log.info("[autoCreateRelatedPropertyMappings][自动创建属性映射: 设备={}, 属性={}, objectInstance={}]",
                    createReqVO.getDeviceId(), thingModel.getIdentifier(), nextObjectInstance);

            nextObjectInstance++;
        }
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
                updateReqVO.getPropertyIdentifier());
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
