package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 计量点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyMeterMapper extends BaseMapperX<IotEnergyMeterDO> {

    default PageResult<IotEnergyMeterDO> selectPage(IotEnergyMeterPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyMeterDO>()
                .likeIfPresent(IotEnergyMeterDO::getMeterName, reqVO.getMeterName())
                .likeIfPresent(IotEnergyMeterDO::getMeterCode, reqVO.getMeterCode())
                .eqIfPresent(IotEnergyMeterDO::getEnergyTypeId, reqVO.getEnergyTypeId())
                .eqIfPresent(IotEnergyMeterDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotEnergyMeterDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotEnergyMeterDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IotEnergyMeterDO::getFloorId, reqVO.getFloorId())
                .eqIfPresent(IotEnergyMeterDO::getRoomId, reqVO.getRoomId())
                .eqIfPresent(IotEnergyMeterDO::getMeterLevel, reqVO.getMeterLevel())
                .eqIfPresent(IotEnergyMeterDO::getParentId, reqVO.getParentId())
                .eqIfPresent(IotEnergyMeterDO::getIsVirtual, reqVO.getIsVirtual())
                .eqIfPresent(IotEnergyMeterDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyMeterDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyMeterDO::getSort)
                .orderByDesc(IotEnergyMeterDO::getId));
    }

    default List<IotEnergyMeterDO> selectList() {
        return selectList(new LambdaQueryWrapperX<IotEnergyMeterDO>()
                .orderByAsc(IotEnergyMeterDO::getSort)
                .orderByDesc(IotEnergyMeterDO::getId));
    }

    default IotEnergyMeterDO selectByMeterCode(String meterCode) {
        return selectOne(IotEnergyMeterDO::getMeterCode, meterCode);
    }

    default List<IotEnergyMeterDO> selectListByEnergyTypeId(Long energyTypeId) {
        return selectList(IotEnergyMeterDO::getEnergyTypeId, energyTypeId);
    }

    default List<IotEnergyMeterDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotEnergyMeterDO::getDeviceId, deviceId);
    }

    default List<IotEnergyMeterDO> selectListByBuildingId(Long buildingId) {
        return selectList(IotEnergyMeterDO::getBuildingId, buildingId);
    }

    default List<IotEnergyMeterDO> selectListByAreaId(Long areaId) {
        return selectList(IotEnergyMeterDO::getAreaId, areaId);
    }

    default List<IotEnergyMeterDO> selectListByFloorId(Long floorId) {
        return selectList(IotEnergyMeterDO::getFloorId, floorId);
    }

    default List<IotEnergyMeterDO> selectListByRoomId(Long roomId) {
        return selectList(IotEnergyMeterDO::getRoomId, roomId);
    }

    default List<IotEnergyMeterDO> selectListByParentId(Long parentId) {
        return selectList(IotEnergyMeterDO::getParentId, parentId);
    }

    default List<IotEnergyMeterDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyMeterDO::getStatus, status);
    }

}
