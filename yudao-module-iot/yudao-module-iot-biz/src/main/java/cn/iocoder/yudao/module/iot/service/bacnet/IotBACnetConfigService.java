package cn.iocoder.yudao.module.iot.service.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;

import javax.validation.Valid;
import java.util.List;

/**
 * BACnet 配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotBACnetConfigService {

    // ========== 设备配置管理 ==========

    /**
     * 创建 BACnet 设备配置
     *
     * @param createReqVO 创建信息
     * @return 配置编号
     */
    Long createDeviceConfig(@Valid IotBACnetDeviceConfigSaveReqVO createReqVO);

    /**
     * 更新 BACnet 设备配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceConfig(@Valid IotBACnetDeviceConfigSaveReqVO updateReqVO);

    /**
     * 删除 BACnet 设备配置
     *
     * @param id 配置编号
     */
    void deleteDeviceConfig(Long id);

    /**
     * 获得 BACnet 设备配置
     *
     * @param id 配置编号
     * @return BACnet 设备配置
     */
    IotBACnetDeviceConfigDO getDeviceConfig(Long id);

    /**
     * 根据设备编号获得 BACnet 设备配置
     *
     * @param deviceId 设备编号
     * @return BACnet 设备配置
     */
    IotBACnetDeviceConfigDO getDeviceConfigByDeviceId(Long deviceId);

    /**
     * 获得 BACnet 设备配置分页
     *
     * @param pageReqVO 分页查询
     * @return BACnet 设备配置分页
     */
    PageResult<IotBACnetDeviceConfigDO> getDeviceConfigPage(IotBACnetDeviceConfigPageReqVO pageReqVO);

    /**
     * 获得所有启用轮询的设备配置
     *
     * @return BACnet 设备配置列表
     */
    List<IotBACnetDeviceConfigDO> getEnabledPollingConfigs();

    // ========== 属性映射管理 ==========

    /**
     * 创建 BACnet 属性映射配置
     *
     * @param createReqVO 创建信息
     * @return 映射编号
     */
    Long createPropertyMapping(@Valid IotBACnetPropertyMappingSaveReqVO createReqVO);

    /**
     * 更新 BACnet 属性映射配置
     *
     * @param updateReqVO 更新信息
     */
    void updatePropertyMapping(@Valid IotBACnetPropertyMappingSaveReqVO updateReqVO);

    /**
     * 删除 BACnet 属性映射配置
     *
     * @param id 映射编号
     */
    void deletePropertyMapping(Long id);

    /**
     * 获得 BACnet 属性映射配置
     *
     * @param id 映射编号
     * @return BACnet 属性映射配置
     */
    IotBACnetPropertyMappingDO getPropertyMapping(Long id);

    /**
     * 获得 BACnet 属性映射配置分页
     *
     * @param pageReqVO 分页查询
     * @return BACnet 属性映射配置分页
     */
    PageResult<IotBACnetPropertyMappingDO> getPropertyMappingPage(IotBACnetPropertyMappingPageReqVO pageReqVO);

    /**
     * 获得设备的所有属性映射配置
     *
     * @param deviceId 设备编号
     * @return BACnet 属性映射配置列表
     */
    List<IotBACnetPropertyMappingDO> getPropertyMappingsByDeviceId(Long deviceId);

    /**
     * 获得设备启用轮询的属性映射配置
     *
     * @param deviceId 设备编号
     * @return BACnet 属性映射配置列表
     */
    List<IotBACnetPropertyMappingDO> getEnabledPollingMappings(Long deviceId);

    /**
     * 根据设备和标识符获得属性映射配置
     *
     * @param deviceId 设备编号
     * @param identifier 属性标识符
     * @return BACnet 属性映射配置
     */
    IotBACnetPropertyMappingDO getPropertyMappingByIdentifier(Long deviceId, String identifier);

}
