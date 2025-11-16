package cn.iocoder.yudao.module.iot.service.energy.room;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源房间 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyRoomService {

    Long createRoom(@Valid IotEnergyRoomSaveReqVO createReqVO);

    void updateRoom(@Valid IotEnergyRoomSaveReqVO updateReqVO);

    void deleteRoom(Long id);

    IotEnergyRoomDO getRoom(Long id);

    List<IotEnergyRoomDO> getRoomList(Collection<Long> ids);

    PageResult<IotEnergyRoomDO> getRoomPage(IotEnergyRoomPageReqVO pageReqVO);

    List<IotEnergyRoomDO> getRoomListByFloorId(Long floorId);

    List<IotEnergyRoomDO> getRoomListByStatus(Integer status);

}
