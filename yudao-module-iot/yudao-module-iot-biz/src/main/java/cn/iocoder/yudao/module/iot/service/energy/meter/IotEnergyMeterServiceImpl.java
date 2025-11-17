package cn.iocoder.yudao.module.iot.service.energy.meter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyMeterMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.energytype.IotEnergyTypeService;
import cn.iocoder.yudao.module.iot.service.energy.floor.IotEnergyFloorService;
import cn.iocoder.yudao.module.iot.service.energy.room.IotEnergyRoomService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源计量点 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyMeterServiceImpl implements IotEnergyMeterService {

    @Resource
    private IotEnergyMeterMapper meterMapper;

    @Resource
    private IotEnergyTypeService energyTypeService;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Resource
    private IotEnergyFloorService floorService;

    @Resource
    private IotEnergyRoomService roomService;

    @Override
    public Long createMeter(IotEnergyMeterSaveReqVO createReqVO) {
        // 校验能源类型是否存在
        energyTypeService.getEnergyType(createReqVO.getEnergyTypeId());

        // 校验设备是否存在（如果绑定了设备）
        if (createReqVO.getDeviceId() != null) {
            deviceService.getDevice(createReqVO.getDeviceId());
        }

        // 校验建筑是否存在（如果指定了建筑）
        if (createReqVO.getBuildingId() != null) {
            buildingService.getBuilding(createReqVO.getBuildingId());
        }

        // 校验区域是否存在（如果指定了区域）
        if (createReqVO.getAreaId() != null) {
            areaService.getArea(createReqVO.getAreaId());
        }

        // 校验楼层是否存在（如果指定了楼层）
        if (createReqVO.getFloorId() != null) {
            floorService.getFloor(createReqVO.getFloorId());
        }

        // 校验房间是否存在（如果指定了房间）
        if (createReqVO.getRoomId() != null) {
            roomService.getRoom(createReqVO.getRoomId());
        }

        // 校验父级计量点是否存在（如果指定了父级）
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            validateMeterExists(createReqVO.getParentId());
        }

        // 校验虚拟表必须有计算公式
        if (Boolean.TRUE.equals(createReqVO.getIsVirtual()) && StrUtil.isBlank(createReqVO.getCalcFormula())) {
            throw exception(ENERGY_METER_VIRTUAL_FORMULA_REQUIRED);
        }

        // 校验非虚拟表必须绑定设备
        if (!Boolean.TRUE.equals(createReqVO.getIsVirtual()) && createReqVO.getDeviceId() == null) {
            throw exception(ENERGY_METER_DEVICE_REQUIRED);
        }

        // 校验计量点编码唯一性
        validateMeterCodeUnique(null, createReqVO.getMeterCode());

        // 插入
        IotEnergyMeterDO meter = BeanUtils.toBean(createReqVO, IotEnergyMeterDO.class);
        meterMapper.insert(meter);

        // 返回
        return meter.getId();
    }

    @Override
    public void updateMeter(IotEnergyMeterSaveReqVO updateReqVO) {
        // 校验存在
        validateMeterExists(updateReqVO.getId());

        // 校验能源类型是否存在
        energyTypeService.getEnergyType(updateReqVO.getEnergyTypeId());

        // 校验设备是否存在（如果绑定了设备）
        if (updateReqVO.getDeviceId() != null) {
            deviceService.getDevice(updateReqVO.getDeviceId());
        }

        // 校验建筑是否存在（如果指定了建筑）
        if (updateReqVO.getBuildingId() != null) {
            buildingService.getBuilding(updateReqVO.getBuildingId());
        }

        // 校验区域是否存在（如果指定了区域）
        if (updateReqVO.getAreaId() != null) {
            areaService.getArea(updateReqVO.getAreaId());
        }

        // 校验楼层是否存在（如果指定了楼层）
        if (updateReqVO.getFloorId() != null) {
            floorService.getFloor(updateReqVO.getFloorId());
        }

        // 校验房间是否存在（如果指定了房间）
        if (updateReqVO.getRoomId() != null) {
            roomService.getRoom(updateReqVO.getRoomId());
        }

        // 校验父级计量点是否存在（如果指定了父级）
        if (updateReqVO.getParentId() != null && updateReqVO.getParentId() > 0) {
            validateMeterExists(updateReqVO.getParentId());

            // 校验不能设置自己为父级
            if (updateReqVO.getId().equals(updateReqVO.getParentId())) {
                throw exception(ENERGY_METER_PARENT_ERROR);
            }
        }

        // 校验虚拟表必须有计算公式
        if (Boolean.TRUE.equals(updateReqVO.getIsVirtual()) && StrUtil.isBlank(updateReqVO.getCalcFormula())) {
            throw exception(ENERGY_METER_VIRTUAL_FORMULA_REQUIRED);
        }

        // 校验非虚拟表必须绑定设备
        if (!Boolean.TRUE.equals(updateReqVO.getIsVirtual()) && updateReqVO.getDeviceId() == null) {
            throw exception(ENERGY_METER_DEVICE_REQUIRED);
        }

        // 校验计量点编码唯一性
        validateMeterCodeUnique(updateReqVO.getId(), updateReqVO.getMeterCode());

        // 更新
        IotEnergyMeterDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyMeterDO.class);
        meterMapper.updateById(updateObj);
    }

    @Override
    public void deleteMeter(Long id) {
        // 校验存在
        validateMeterExists(id);

        // 校验是否存在子计量点
        List<IotEnergyMeterDO> children = meterMapper.selectListByParentId(id);
        if (CollUtil.isNotEmpty(children)) {
            throw exception(ENERGY_METER_EXITS_CHILDREN);
        }

        // TODO 校验是否存在统计数据等关联数据

        // 删除
        meterMapper.deleteById(id);
    }

    /**
     * 校验计量点是否存在
     */
    private void validateMeterExists(Long id) {
        if (meterMapper.selectById(id) == null) {
            throw exception(ENERGY_METER_NOT_EXISTS);
        }
    }

    /**
     * 校验计量点编码唯一性
     */
    private void validateMeterCodeUnique(Long id, String meterCode) {
        IotEnergyMeterDO meter = meterMapper.selectByMeterCode(meterCode);
        if (meter == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的计量点
        if (id == null) {
            throw exception(ENERGY_METER_CODE_EXISTS);
        }
        if (!meter.getId().equals(id)) {
            throw exception(ENERGY_METER_CODE_EXISTS);
        }
    }

    @Override
    public IotEnergyMeterDO getMeter(Long id) {
        return meterMapper.selectById(id);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return meterMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyMeterDO> getMeterPage(IotEnergyMeterPageReqVO pageReqVO) {
        return meterMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByEnergyTypeId(Long energyTypeId) {
        return meterMapper.selectListByEnergyTypeId(energyTypeId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByDeviceId(Long deviceId) {
        return meterMapper.selectListByDeviceId(deviceId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByBuildingId(Long buildingId) {
        return meterMapper.selectListByBuildingId(buildingId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByAreaId(Long areaId) {
        return meterMapper.selectListByAreaId(areaId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByFloorId(Long floorId) {
        return meterMapper.selectListByFloorId(floorId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByRoomId(Long roomId) {
        return meterMapper.selectListByRoomId(roomId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByParentId(Long parentId) {
        return meterMapper.selectListByParentId(parentId);
    }

    @Override
    public List<IotEnergyMeterDO> getMeterListByStatus(Integer status) {
        return meterMapper.selectListByStatus(status);
    }

}
