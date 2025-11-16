package cn.iocoder.yudao.module.iot.service.energy.area;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyAreaMapper;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源区域 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyAreaServiceImpl implements IotEnergyAreaService {

    @Resource
    private IotEnergyAreaMapper areaMapper;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Override
    public Long createArea(IotEnergyAreaSaveReqVO createReqVO) {
        // 校验建筑是否存在
        buildingService.getBuilding(createReqVO.getBuildingId());

        // 校验区域编码唯一性
        validateAreaCodeUnique(null, createReqVO.getAreaCode());

        // 插入
        IotEnergyAreaDO area = BeanUtils.toBean(createReqVO, IotEnergyAreaDO.class);
        areaMapper.insert(area);

        // 返回
        return area.getId();
    }

    @Override
    public void updateArea(IotEnergyAreaSaveReqVO updateReqVO) {
        // 校验存在
        validateAreaExists(updateReqVO.getId());

        // 校验建筑是否存在
        buildingService.getBuilding(updateReqVO.getBuildingId());

        // 校验区域编码唯一性
        validateAreaCodeUnique(updateReqVO.getId(), updateReqVO.getAreaCode());

        // 更新
        IotEnergyAreaDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyAreaDO.class);
        areaMapper.updateById(updateObj);
    }

    @Override
    public void deleteArea(Long id) {
        // 校验存在
        validateAreaExists(id);

        // TODO 校验是否存在楼层、房间等子数据

        // 删除
        areaMapper.deleteById(id);
    }

    /**
     * 校验区域是否存在
     */
    private void validateAreaExists(Long id) {
        if (areaMapper.selectById(id) == null) {
            throw exception(ENERGY_AREA_NOT_EXISTS);
        }
    }

    /**
     * 校验区域编码唯一性
     */
    private void validateAreaCodeUnique(Long id, String areaCode) {
        IotEnergyAreaDO area = areaMapper.selectByAreaCode(areaCode);
        if (area == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的区域
        if (id == null) {
            throw exception(ENERGY_AREA_CODE_EXISTS);
        }
        if (!area.getId().equals(id)) {
            throw exception(ENERGY_AREA_CODE_EXISTS);
        }
    }

    @Override
    public IotEnergyAreaDO getArea(Long id) {
        return areaMapper.selectById(id);
    }

    @Override
    public List<IotEnergyAreaDO> getAreaList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return areaMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyAreaDO> getAreaPage(IotEnergyAreaPageReqVO pageReqVO) {
        return areaMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyAreaDO> getAreaListByBuildingId(Long buildingId) {
        return areaMapper.selectListByBuildingId(buildingId);
    }

    @Override
    public List<IotEnergyAreaDO> getAreaListByStatus(Integer status) {
        return areaMapper.selectListByStatus(status);
    }

}
