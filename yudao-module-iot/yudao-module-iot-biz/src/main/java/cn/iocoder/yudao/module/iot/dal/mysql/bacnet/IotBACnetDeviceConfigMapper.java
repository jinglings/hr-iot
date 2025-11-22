package cn.iocoder.yudao.module.iot.dal.mysql.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BACnet 设备配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotBACnetDeviceConfigMapper extends BaseMapperX<IotBACnetDeviceConfigDO> {

    default PageResult<IotBACnetDeviceConfigDO> selectPage(IotBACnetDeviceConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotBACnetDeviceConfigDO>()
                .eqIfPresent(IotBACnetDeviceConfigDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotBACnetDeviceConfigDO::getInstanceNumber, reqVO.getInstanceNumber())
                .likeIfPresent(IotBACnetDeviceConfigDO::getIpAddress, reqVO.getIpAddress())
                .eqIfPresent(IotBACnetDeviceConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotBACnetDeviceConfigDO::getId));
    }

    default IotBACnetDeviceConfigDO selectByDeviceId(Long deviceId) {
        return selectOne(IotBACnetDeviceConfigDO::getDeviceId, deviceId);
    }

    default IotBACnetDeviceConfigDO selectByInstanceNumber(Integer instanceNumber) {
        return selectOne(IotBACnetDeviceConfigDO::getInstanceNumber, instanceNumber);
    }

    default List<IotBACnetDeviceConfigDO> selectListByStatus(Integer status) {
        return selectList(IotBACnetDeviceConfigDO::getStatus, status);
    }

    default List<IotBACnetDeviceConfigDO> selectEnabledPollingConfigs() {
        return selectList(new LambdaQueryWrapperX<IotBACnetDeviceConfigDO>()
                .eq(IotBACnetDeviceConfigDO::getStatus, 1) // 启用
                .eq(IotBACnetDeviceConfigDO::getPollingEnabled, true)); // 轮询启用
    }

}
