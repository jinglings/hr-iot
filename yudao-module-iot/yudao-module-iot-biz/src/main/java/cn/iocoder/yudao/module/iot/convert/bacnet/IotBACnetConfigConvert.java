package cn.iocoder.yudao.module.iot.convert.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * BACnet 配置 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface IotBACnetConfigConvert {

    IotBACnetConfigConvert INSTANCE = Mappers.getMapper(IotBACnetConfigConvert.class);

    // ========== 设备配置转换 ==========

    IotBACnetDeviceConfigDO convert(IotBACnetDeviceConfigSaveReqVO bean);

    IotBACnetDeviceConfigRespVO convert(IotBACnetDeviceConfigDO bean);

    List<IotBACnetDeviceConfigRespVO> convertList(List<IotBACnetDeviceConfigDO> list);

    PageResult<IotBACnetDeviceConfigRespVO> convertPage(PageResult<IotBACnetDeviceConfigDO> page);

    // ========== 属性映射转换 ==========

    IotBACnetPropertyMappingDO convert(IotBACnetPropertyMappingSaveReqVO bean);

    IotBACnetPropertyMappingRespVO convert(IotBACnetPropertyMappingDO bean);

    List<IotBACnetPropertyMappingRespVO> convertMappingList(List<IotBACnetPropertyMappingDO> list);

    PageResult<IotBACnetPropertyMappingRespVO> convertMappingPage(PageResult<IotBACnetPropertyMappingDO> page);

}
