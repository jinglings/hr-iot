package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 区域 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyAreaMapper extends BaseMapperX<IotEnergyAreaDO> {

    default PageResult<IotEnergyAreaDO> selectPage(IotEnergyAreaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyAreaDO>()
                .likeIfPresent(IotEnergyAreaDO::getAreaName, reqVO.getAreaName())
                .likeIfPresent(IotEnergyAreaDO::getAreaCode, reqVO.getAreaCode())
                .eqIfPresent(IotEnergyAreaDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotEnergyAreaDO::getAreaType, reqVO.getAreaType())
                .eqIfPresent(IotEnergyAreaDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyAreaDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyAreaDO::getSort)
                .orderByDesc(IotEnergyAreaDO::getId));
    }

    default List<IotEnergyAreaDO> selectListByBuildingId(Long buildingId) {
        return selectList(IotEnergyAreaDO::getBuildingId, buildingId);
    }

    default List<IotEnergyAreaDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyAreaDO::getStatus, status);
    }

    default IotEnergyAreaDO selectByAreaCode(String areaCode) {
        return selectOne(IotEnergyAreaDO::getAreaCode, areaCode);
    }

}
