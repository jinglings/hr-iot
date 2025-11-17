package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 建筑 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyBuildingMapper extends BaseMapperX<IotEnergyBuildingDO> {

    default PageResult<IotEnergyBuildingDO> selectPage(IotEnergyBuildingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyBuildingDO>()
                .likeIfPresent(IotEnergyBuildingDO::getBuildingName, reqVO.getBuildingName())
                .likeIfPresent(IotEnergyBuildingDO::getBuildingCode, reqVO.getBuildingCode())
                .eqIfPresent(IotEnergyBuildingDO::getBuildingType, reqVO.getBuildingType())
                .eqIfPresent(IotEnergyBuildingDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyBuildingDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyBuildingDO::getSort)
                .orderByDesc(IotEnergyBuildingDO::getId));
    }

    default List<IotEnergyBuildingDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyBuildingDO::getStatus, status);
    }

    default IotEnergyBuildingDO selectByBuildingCode(String buildingCode) {
        return selectOne(IotEnergyBuildingDO::getBuildingCode, buildingCode);
    }

}
