package cn.iocoder.yudao.module.iot.service.energy.floor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源楼层 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyFloorService {

    /**
     * 创建能源楼层
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFloor(@Valid IotEnergyFloorSaveReqVO createReqVO);

    /**
     * 更新能源楼层
     *
     * @param updateReqVO 更新信息
     */
    void updateFloor(@Valid IotEnergyFloorSaveReqVO updateReqVO);

    /**
     * 删除能源楼层
     *
     * @param id 编号
     */
    void deleteFloor(Long id);

    /**
     * 获得能源楼层
     *
     * @param id 编号
     * @return 能源楼层
     */
    IotEnergyFloorDO getFloor(Long id);

    /**
     * 获得能源楼层列表
     *
     * @param ids 编号
     * @return 能源楼层列表
     */
    List<IotEnergyFloorDO> getFloorList(Collection<Long> ids);

    /**
     * 获得能源楼层分页
     *
     * @param pageReqVO 分页查询
     * @return 能源楼层分页
     */
    PageResult<IotEnergyFloorDO> getFloorPage(IotEnergyFloorPageReqVO pageReqVO);

    /**
     * 获得能源楼层列表，根据建筑ID
     *
     * @param buildingId 建筑ID
     * @return 能源楼层列表
     */
    List<IotEnergyFloorDO> getFloorListByBuildingId(Long buildingId);

    /**
     * 获得能源楼层列表，根据区域ID
     *
     * @param areaId 区域ID
     * @return 能源楼层列表
     */
    List<IotEnergyFloorDO> getFloorListByAreaId(Long areaId);

    /**
     * 获得能源楼层列表，根据状态
     *
     * @param status 状态
     * @return 能源楼层列表
     */
    List<IotEnergyFloorDO> getFloorListByStatus(Integer status);

}
