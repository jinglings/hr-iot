package cn.iocoder.yudao.module.iot.service.energy.room;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyRoomMapper;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.floor.IotEnergyFloorService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源房间 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyRoomServiceImpl implements IotEnergyRoomService {

    @Resource
    private IotEnergyRoomMapper roomMapper;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Resource
    private IotEnergyFloorService floorService;

    @Override
    public Long createRoom(IotEnergyRoomSaveReqVO createReqVO) {
        // 校验建筑是否存在
        buildingService.getBuilding(createReqVO.getBuildingId());

        // 校验区域是否存在（如果指定了区域）
        if (createReqVO.getAreaId() != null) {
            areaService.getArea(createReqVO.getAreaId());
        }

        // 校验楼层是否存在
        floorService.getFloor(createReqVO.getFloorId());

        // 校验房间编码唯一性
        validateRoomCodeUnique(null, createReqVO.getRoomCode());

        // 插入
        IotEnergyRoomDO room = BeanUtils.toBean(createReqVO, IotEnergyRoomDO.class);
        roomMapper.insert(room);

        // 返回
        return room.getId();
    }

    @Override
    public void updateRoom(IotEnergyRoomSaveReqVO updateReqVO) {
        // 校验存在
        validateRoomExists(updateReqVO.getId());

        // 校验建筑是否存在
        buildingService.getBuilding(updateReqVO.getBuildingId());

        // 校验区域是否存在（如果指定了区域）
        if (updateReqVO.getAreaId() != null) {
            areaService.getArea(updateReqVO.getAreaId());
        }

        // 校验楼层是否存在
        floorService.getFloor(updateReqVO.getFloorId());

        // 校验房间编码唯一性
        validateRoomCodeUnique(updateReqVO.getId(), updateReqVO.getRoomCode());

        // 更新
        IotEnergyRoomDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyRoomDO.class);
        roomMapper.updateById(updateObj);
    }

    @Override
    public void deleteRoom(Long id) {
        // 校验存在
        validateRoomExists(id);

        // TODO 校验是否存在设备等关联数据

        // 删除
        roomMapper.deleteById(id);
    }

    /**
     * 校验房间是否存在
     */
    private void validateRoomExists(Long id) {
        if (roomMapper.selectById(id) == null) {
            throw exception(ENERGY_ROOM_NOT_EXISTS);
        }
    }

    /**
     * 校验房间编码唯一性
     */
    private void validateRoomCodeUnique(Long id, String roomCode) {
        IotEnergyRoomDO room = roomMapper.selectByRoomCode(roomCode);
        if (room == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的房间
        if (id == null) {
            throw exception(ENERGY_ROOM_CODE_EXISTS);
        }
        if (!room.getId().equals(id)) {
            throw exception(ENERGY_ROOM_CODE_EXISTS);
        }
    }

    @Override
    public IotEnergyRoomDO getRoom(Long id) {
        return roomMapper.selectById(id);
    }

    @Override
    public List<IotEnergyRoomDO> getRoomList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return roomMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyRoomDO> getRoomPage(IotEnergyRoomPageReqVO pageReqVO) {
        return roomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyRoomDO> getRoomListByFloorId(Long floorId) {
        return roomMapper.selectListByFloorId(floorId);
    }

    @Override
    public List<IotEnergyRoomDO> getRoomListByStatus(Integer status) {
        return roomMapper.selectListByStatus(status);
    }

}
