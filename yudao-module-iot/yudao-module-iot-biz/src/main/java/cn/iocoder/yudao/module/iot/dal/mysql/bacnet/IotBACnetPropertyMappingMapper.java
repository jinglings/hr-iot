package cn.iocoder.yudao.module.iot.dal.mysql.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BACnet 属性映射配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotBACnetPropertyMappingMapper extends BaseMapperX<IotBACnetPropertyMappingDO> {

    default PageResult<IotBACnetPropertyMappingDO> selectPage(IotBACnetPropertyMappingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotBACnetPropertyMappingDO>()
                .eqIfPresent(IotBACnetPropertyMappingDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotBACnetPropertyMappingDO::getDeviceConfigId, reqVO.getDeviceConfigId())
                .likeIfPresent(IotBACnetPropertyMappingDO::getIdentifier, reqVO.getIdentifier())
                .eqIfPresent(IotBACnetPropertyMappingDO::getObjectType, reqVO.getObjectType())
                .eqIfPresent(IotBACnetPropertyMappingDO::getStatus, reqVO.getStatus())
                .orderByAsc(IotBACnetPropertyMappingDO::getSort)
                .orderByDesc(IotBACnetPropertyMappingDO::getId));
    }

    default List<IotBACnetPropertyMappingDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotBACnetPropertyMappingDO::getDeviceId, deviceId);
    }

    default List<IotBACnetPropertyMappingDO> selectListByDeviceConfigId(Long deviceConfigId) {
        return selectList(IotBACnetPropertyMappingDO::getDeviceConfigId, deviceConfigId);
    }

    default IotBACnetPropertyMappingDO selectByDeviceIdAndIdentifier(Long deviceId, String identifier) {
        return selectOne(new LambdaQueryWrapperX<IotBACnetPropertyMappingDO>()
                .eq(IotBACnetPropertyMappingDO::getDeviceId, deviceId)
                .eq(IotBACnetPropertyMappingDO::getIdentifier, identifier));
    }

    default List<IotBACnetPropertyMappingDO> selectEnabledPollingMappings(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotBACnetPropertyMappingDO>()
                .eq(IotBACnetPropertyMappingDO::getDeviceId, deviceId)
                .eq(IotBACnetPropertyMappingDO::getStatus, 1) // 启用
                .eq(IotBACnetPropertyMappingDO::getPollingEnabled, true) // 轮询启用
                .orderByAsc(IotBACnetPropertyMappingDO::getSort));
    }

    default IotBACnetPropertyMappingDO selectByDeviceObjectProperty(Long deviceId, String objectType,
                                                                     Integer objectInstance, String propertyIdentifier) {
        return selectOne(new LambdaQueryWrapperX<IotBACnetPropertyMappingDO>()
                .eq(IotBACnetPropertyMappingDO::getDeviceId, deviceId)
                .eq(IotBACnetPropertyMappingDO::getObjectType, objectType)
                .eq(IotBACnetPropertyMappingDO::getObjectInstance, objectInstance)
                .eq(IotBACnetPropertyMappingDO::getPropertyIdentifier, propertyIdentifier));
    }

}
