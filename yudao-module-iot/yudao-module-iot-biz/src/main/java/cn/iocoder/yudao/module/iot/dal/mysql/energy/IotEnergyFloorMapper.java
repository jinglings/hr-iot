package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 楼层 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyFloorMapper extends BaseMapperX<IotEnergyFloorDO> {

    default PageResult<IotEnergyFloorDO> selectPage(IotEnergyFloorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyFloorDO>()
                .likeIfPresent(IotEnergyFloorDO::getFloorName, reqVO.getFloorName())
                .likeIfPresent(IotEnergyFloorDO::getFloorCode, reqVO.getFloorCode())
                .eqIfPresent(IotEnergyFloorDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotEnergyFloorDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IotEnergyFloorDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyFloorDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyFloorDO::getFloorNumber)
                .orderByAsc(IotEnergyFloorDO::getSort)
                .orderByDesc(IotEnergyFloorDO::getId));
    }

    default List<IotEnergyFloorDO> selectListByBuildingId(Long buildingId) {
        return selectList(IotEnergyFloorDO::getBuildingId, buildingId);
    }

    default List<IotEnergyFloorDO> selectListByAreaId(Long areaId) {
        return selectList(IotEnergyFloorDO::getAreaId, areaId);
    }

    default List<IotEnergyFloorDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyFloorDO::getStatus, status);
    }

    default IotEnergyFloorDO selectByFloorCode(String floorCode) {
        return selectOne(IotEnergyFloorDO::getFloorCode, floorCode);
    }

}
