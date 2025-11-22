package cn.iocoder.yudao.module.iot.dal.mysql.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDiscoveryRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BACnet 设备发现记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotBACnetDiscoveryRecordMapper extends BaseMapperX<IotBACnetDiscoveryRecordDO> {

    default PageResult<IotBACnetDiscoveryRecordDO> selectPage(IotBACnetDiscoveryRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotBACnetDiscoveryRecordDO>()
                .eqIfPresent(IotBACnetDiscoveryRecordDO::getInstanceNumber, reqVO.getInstanceNumber())
                .likeIfPresent(IotBACnetDiscoveryRecordDO::getDeviceName, reqVO.getDeviceName())
                .likeIfPresent(IotBACnetDiscoveryRecordDO::getIpAddress, reqVO.getIpAddress())
                .eqIfPresent(IotBACnetDiscoveryRecordDO::getIsBound, reqVO.getIsBound())
                .eqIfPresent(IotBACnetDiscoveryRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotBACnetDiscoveryRecordDO::getDiscoveryTime, reqVO.getDiscoveryTime())
                .orderByDesc(IotBACnetDiscoveryRecordDO::getLastSeenTime)
                .orderByDesc(IotBACnetDiscoveryRecordDO::getId));
    }

    default IotBACnetDiscoveryRecordDO selectByInstanceNumber(Integer instanceNumber) {
        return selectOne(IotBACnetDiscoveryRecordDO::getInstanceNumber, instanceNumber);
    }

    default List<IotBACnetDiscoveryRecordDO> selectUnboundDevices() {
        return selectList(new LambdaQueryWrapperX<IotBACnetDiscoveryRecordDO>()
                .eq(IotBACnetDiscoveryRecordDO::getIsBound, false)
                .eq(IotBACnetDiscoveryRecordDO::getStatus, 1) // 在线
                .orderByDesc(IotBACnetDiscoveryRecordDO::getDiscoveryTime));
    }

    default List<IotBACnetDiscoveryRecordDO> selectBoundDevices() {
        return selectList(new LambdaQueryWrapperX<IotBACnetDiscoveryRecordDO>()
                .eq(IotBACnetDiscoveryRecordDO::getIsBound, true)
                .orderByDesc(IotBACnetDiscoveryRecordDO::getLastSeenTime));
    }

    /**
     * 标记长时间未见的设备为离线
     *
     * @param beforeTime 时间阈值
     */
    @Update("UPDATE iot_bacnet_discovery_record SET status = 0 " +
            "WHERE last_seen_time < #{beforeTime} AND status = 1 AND deleted = 0")
    void markOfflineDevices(@Param("beforeTime") LocalDateTime beforeTime);

}
