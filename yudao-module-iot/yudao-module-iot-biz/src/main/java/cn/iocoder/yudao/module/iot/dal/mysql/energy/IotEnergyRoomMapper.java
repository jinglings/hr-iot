package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 房间 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyRoomMapper extends BaseMapperX<IotEnergyRoomDO> {

    default PageResult<IotEnergyRoomDO> selectPage(IotEnergyRoomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyRoomDO>()
                .likeIfPresent(IotEnergyRoomDO::getRoomName, reqVO.getRoomName())
                .likeIfPresent(IotEnergyRoomDO::getRoomCode, reqVO.getRoomCode())
                .eqIfPresent(IotEnergyRoomDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotEnergyRoomDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IotEnergyRoomDO::getFloorId, reqVO.getFloorId())
                .eqIfPresent(IotEnergyRoomDO::getRoomType, reqVO.getRoomType())
                .eqIfPresent(IotEnergyRoomDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyRoomDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyRoomDO::getSort)
                .orderByDesc(IotEnergyRoomDO::getId));
    }

    default List<IotEnergyRoomDO> selectListByFloorId(Long floorId) {
        return selectList(IotEnergyRoomDO::getFloorId, floorId);
    }

    default List<IotEnergyRoomDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyRoomDO::getStatus, status);
    }

    default IotEnergyRoomDO selectByRoomCode(String roomCode) {
        return selectOne(IotEnergyRoomDO::getRoomCode, roomCode);
    }

}
