package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源管理 - 能源类型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyTypeMapper extends BaseMapperX<IotEnergyTypeDO> {

    default PageResult<IotEnergyTypeDO> selectPage(IotEnergyTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyTypeDO>()
                .likeIfPresent(IotEnergyTypeDO::getEnergyName, reqVO.getEnergyName())
                .likeIfPresent(IotEnergyTypeDO::getEnergyCode, reqVO.getEnergyCode())
                .eqIfPresent(IotEnergyTypeDO::getParentId, reqVO.getParentId())
                .eqIfPresent(IotEnergyTypeDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyTypeDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotEnergyTypeDO::getSort)
                .orderByDesc(IotEnergyTypeDO::getId));
    }

    default List<IotEnergyTypeDO> selectList() {
        return selectList(new LambdaQueryWrapperX<IotEnergyTypeDO>()
                .orderByAsc(IotEnergyTypeDO::getSort)
                .orderByDesc(IotEnergyTypeDO::getId));
    }

    default List<IotEnergyTypeDO> selectListByParentId(Long parentId) {
        return selectList(IotEnergyTypeDO::getParentId, parentId);
    }

    default List<IotEnergyTypeDO> selectListByStatus(Integer status) {
        return selectList(IotEnergyTypeDO::getStatus, status);
    }

    default IotEnergyTypeDO selectByEnergyCode(String energyCode) {
        return selectOne(IotEnergyTypeDO::getEnergyCode, energyCode);
    }

}
