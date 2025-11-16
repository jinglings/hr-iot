package cn.iocoder.yudao.module.iot.service.energy.floor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyFloorMapper;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源楼层 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyFloorServiceImpl implements IotEnergyFloorService {

    @Resource
    private IotEnergyFloorMapper floorMapper;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Override
    public Long createFloor(IotEnergyFloorSaveReqVO createReqVO) {
        // 校验建筑是否存在
        buildingService.getBuilding(createReqVO.getBuildingId());

        // 校验区域是否存在（如果指定了区域）
        if (createReqVO.getAreaId() != null) {
            areaService.getArea(createReqVO.getAreaId());
        }

        // 校验楼层编码唯一性
        validateFloorCodeUnique(null, createReqVO.getFloorCode());

        // 插入
        IotEnergyFloorDO floor = BeanUtils.toBean(createReqVO, IotEnergyFloorDO.class);
        floorMapper.insert(floor);

        // 返回
        return floor.getId();
    }

    @Override
    public void updateFloor(IotEnergyFloorSaveReqVO updateReqVO) {
        // 校验存在
        validateFloorExists(updateReqVO.getId());

        // 校验建筑是否存在
        buildingService.getBuilding(updateReqVO.getBuildingId());

        // 校验区域是否存在（如果指定了区域）
        if (updateReqVO.getAreaId() != null) {
            areaService.getArea(updateReqVO.getAreaId());
        }

        // 校验楼层编码唯一性
        validateFloorCodeUnique(updateReqVO.getId(), updateReqVO.getFloorCode());

        // 更新
        IotEnergyFloorDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyFloorDO.class);
        floorMapper.updateById(updateObj);
    }

    @Override
    public void deleteFloor(Long id) {
        // 校验存在
        validateFloorExists(id);

        // TODO 校验是否存在房间等子数据

        // 删除
        floorMapper.deleteById(id);
    }

    /**
     * 校验楼层是否存在
     */
    private void validateFloorExists(Long id) {
        if (floorMapper.selectById(id) == null) {
            throw exception(ENERGY_FLOOR_NOT_EXISTS);
        }
    }

    /**
     * 校验楼层编码唯一性
     */
    private void validateFloorCodeUnique(Long id, String floorCode) {
        IotEnergyFloorDO floor = floorMapper.selectByFloorCode(floorCode);
        if (floor == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的楼层
        if (id == null) {
            throw exception(ENERGY_FLOOR_CODE_EXISTS);
        }
        if (!floor.getId().equals(id)) {
            throw exception(ENERGY_FLOOR_CODE_EXISTS);
        }
    }

    @Override
    public IotEnergyFloorDO getFloor(Long id) {
        return floorMapper.selectById(id);
    }

    @Override
    public List<IotEnergyFloorDO> getFloorList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return floorMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyFloorDO> getFloorPage(IotEnergyFloorPageReqVO pageReqVO) {
        return floorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyFloorDO> getFloorListByBuildingId(Long buildingId) {
        return floorMapper.selectListByBuildingId(buildingId);
    }

    @Override
    public List<IotEnergyFloorDO> getFloorListByAreaId(Long areaId) {
        return floorMapper.selectListByAreaId(areaId);
    }

    @Override
    public List<IotEnergyFloorDO> getFloorListByStatus(Integer status) {
        return floorMapper.selectListByStatus(status);
    }

}
