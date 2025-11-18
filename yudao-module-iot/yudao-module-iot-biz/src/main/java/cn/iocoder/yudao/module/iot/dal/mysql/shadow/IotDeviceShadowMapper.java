package cn.iocoder.yudao.module.iot.dal.mysql.shadow;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.shadow.IotDeviceShadowDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 设备影子 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotDeviceShadowMapper extends BaseMapperX<IotDeviceShadowDO> {

    /**
     * 根据设备ID查询影子
     *
     * @param deviceId 设备ID
     * @return 设备影子
     */
    default IotDeviceShadowDO selectByDeviceId(Long deviceId) {
        return selectOne(IotDeviceShadowDO::getDeviceId, deviceId);
    }

}
