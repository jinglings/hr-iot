package cn.iocoder.yudao.module.iot.controller.admin.energy.room;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;
import cn.iocoder.yudao.module.iot.service.energy.room.IotEnergyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 能源房间")
@RestController
@RequestMapping("/iot/energy/room")
@Validated
public class IotEnergyRoomController {

    @Resource
    private IotEnergyRoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "创建能源房间")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:create')")
    public CommonResult<Long> createRoom(@Valid @RequestBody IotEnergyRoomSaveReqVO createReqVO) {
        return success(roomService.createRoom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源房间")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:update')")
    public CommonResult<Boolean> updateRoom(@Valid @RequestBody IotEnergyRoomSaveReqVO updateReqVO) {
        roomService.updateRoom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源房间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-room:delete')")
    public CommonResult<Boolean> deleteRoom(@RequestParam("id") Long id) {
        roomService.deleteRoom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源房间")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<IotEnergyRoomRespVO> getRoom(@RequestParam("id") Long id) {
        IotEnergyRoomDO room = roomService.getRoom(id);
        return success(BeanUtils.toBean(room, IotEnergyRoomRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源房间分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<PageResult<IotEnergyRoomRespVO>> getRoomPage(@Valid IotEnergyRoomPageReqVO pageReqVO) {
        PageResult<IotEnergyRoomDO> pageResult = roomService.getRoomPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyRoomRespVO.class));
    }

    @GetMapping("/list-by-floor")
    @Operation(summary = "获得指定楼层的能源房间列表")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<List<IotEnergyRoomRespVO>> getRoomListByFloor(@RequestParam("floorId") Long floorId) {
        List<IotEnergyRoomDO> list = roomService.getRoomListByFloorId(floorId);
        return success(BeanUtils.toBean(list, IotEnergyRoomRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源房间列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<List<IotEnergyRoomRespVO>> getSimpleRoomList() {
        List<IotEnergyRoomDO> list = roomService.getRoomListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, room ->
                new IotEnergyRoomRespVO().setId(room.getId()).setRoomName(room.getRoomName())));
    }

}
