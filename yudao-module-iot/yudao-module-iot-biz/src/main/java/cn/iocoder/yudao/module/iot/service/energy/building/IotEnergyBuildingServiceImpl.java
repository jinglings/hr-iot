package cn.iocoder.yudao.module.iot.service.energy.building;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyBuildingMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源建筑 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyBuildingServiceImpl implements IotEnergyBuildingService {

    @Resource
    private IotEnergyBuildingMapper buildingMapper;

    @Override
    public Long createBuilding(IotEnergyBuildingSaveReqVO createReqVO) {
        // 校验建筑编码唯一性
        validateBuildingCodeUnique(null, createReqVO.getBuildingCode());

        // 插入
        IotEnergyBuildingDO building = BeanUtils.toBean(createReqVO, IotEnergyBuildingDO.class);
        buildingMapper.insert(building);

        // 返回
        return building.getId();
    }

    @Override
    public void updateBuilding(IotEnergyBuildingSaveReqVO updateReqVO) {
        // 校验存在
        validateBuildingExists(updateReqVO.getId());

        // 校验建筑编码唯一性
        validateBuildingCodeUnique(updateReqVO.getId(), updateReqVO.getBuildingCode());

        // 更新
        IotEnergyBuildingDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyBuildingDO.class);
        buildingMapper.updateById(updateObj);
    }

    @Override
    public void deleteBuilding(Long id) {
        // 校验存在
        validateBuildingExists(id);

        // TODO 校验是否存在区域、楼层、房间等子数据

        // 删除
        buildingMapper.deleteById(id);
    }

    /**
     * 校验建筑是否存在
     */
    private void validateBuildingExists(Long id) {
        if (buildingMapper.selectById(id) == null) {
            throw exception(ENERGY_BUILDING_NOT_EXISTS);
        }
    }

    /**
     * 校验建筑编码唯一性
     */
    private void validateBuildingCodeUnique(Long id, String buildingCode) {
        IotEnergyBuildingDO building = buildingMapper.selectByBuildingCode(buildingCode);
        if (building == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的建筑
        if (id == null) {
            throw exception(ENERGY_BUILDING_CODE_EXISTS);
        }
        if (!building.getId().equals(id)) {
            throw exception(ENERGY_BUILDING_CODE_EXISTS);
        }
    }

    @Override
    public IotEnergyBuildingDO getBuilding(Long id) {
        return buildingMapper.selectById(id);
    }

    @Override
    public List<IotEnergyBuildingDO> getBuildingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return buildingMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyBuildingDO> getBuildingPage(IotEnergyBuildingPageReqVO pageReqVO) {
        return buildingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyBuildingDO> getBuildingListByStatus(Integer status) {
        return buildingMapper.selectListByStatus(status);
    }

}
