package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug.IotDeviceDebugLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDebugLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * IoT 设备调试日志 Mapper
 *
 * @author AI
 */
@Mapper
public interface IotDeviceDebugLogMapper extends BaseMapperX<IotDeviceDebugLogDO> {

    /**
     * 分页查询调试日志
     */
    default PageResult<IotDeviceDebugLogDO> selectPage(IotDeviceDebugLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDebugLogDO>()
                .eqIfPresent(IotDeviceDebugLogDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceDebugLogDO::getDirection, reqVO.getDirection())
                .eqIfPresent(IotDeviceDebugLogDO::getType, reqVO.getType())
                .eqIfPresent(IotDeviceDebugLogDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotDeviceDebugLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceDebugLogDO::getId));
    }

    /**
     * 清理指定时间前的调试日志
     */
    default int deleteBeforeTime(LocalDateTime time) {
        return delete(new LambdaQueryWrapperX<IotDeviceDebugLogDO>()
                .lt(IotDeviceDebugLogDO::getCreateTime, time));
    }

}
