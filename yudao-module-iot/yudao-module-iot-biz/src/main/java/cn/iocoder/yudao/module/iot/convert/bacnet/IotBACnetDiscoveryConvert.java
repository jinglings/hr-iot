package cn.iocoder.yudao.module.iot.convert.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordRespVO;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.dto.BACnetDeviceInfo;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDiscoveryRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BACnet 设备发现 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface IotBACnetDiscoveryConvert {

    IotBACnetDiscoveryConvert INSTANCE = Mappers.getMapper(IotBACnetDiscoveryConvert.class);

    @Mapping(source = "instanceNumber", target = "instanceNumber")
    @Mapping(source = "deviceName", target = "deviceName")
    @Mapping(source = "ipAddress", target = "ipAddress")
    @Mapping(source = "vendorId", target = "vendorId")
    @Mapping(source = "vendorName", target = "vendorName")
    @Mapping(source = "modelName", target = "modelName")
    @Mapping(source = "firmwareRevision", target = "firmwareRevision")
    @Mapping(source = "applicationSoftwareVersion", target = "applicationSoftwareVersion")
    @Mapping(source = "location", target = "location")
    @Mapping(target = "port", constant = "47808")
    @Mapping(target = "discoveryTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastSeenTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "isBound", constant = "false")
    IotBACnetDiscoveryRecordDO convert(BACnetDeviceInfo bean);

    IotBACnetDiscoveryRecordRespVO convert(IotBACnetDiscoveryRecordDO bean);

    List<IotBACnetDiscoveryRecordRespVO> convertList(List<IotBACnetDiscoveryRecordDO> list);

    PageResult<IotBACnetDiscoveryRecordRespVO> convertPage(PageResult<IotBACnetDiscoveryRecordDO> page);

}
