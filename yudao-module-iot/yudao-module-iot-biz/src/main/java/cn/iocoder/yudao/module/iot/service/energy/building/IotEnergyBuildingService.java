package cn.iocoder.yudao.module.iot.service.energy.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源建筑 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyBuildingService {

    /**
     * 创建能源建筑
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBuilding(@Valid IotEnergyBuildingSaveReqVO createReqVO);

    /**
     * 更新能源建筑
     *
     * @param updateReqVO 更新信息
     */
    void updateBuilding(@Valid IotEnergyBuildingSaveReqVO updateReqVO);

    /**
     * 删除能源建筑
     *
     * @param id 编号
     */
    void deleteBuilding(Long id);

    /**
     * 获得能源建筑
     *
     * @param id 编号
     * @return 能源建筑
     */
    IotEnergyBuildingDO getBuilding(Long id);

    /**
     * 获得能源建筑列表
     *
     * @param ids 编号
     * @return 能源建筑列表
     */
    List<IotEnergyBuildingDO> getBuildingList(Collection<Long> ids);

    /**
     * 获得能源建筑分页
     *
     * @param pageReqVO 分页查询
     * @return 能源建筑分页
     */
    PageResult<IotEnergyBuildingDO> getBuildingPage(IotEnergyBuildingPageReqVO pageReqVO);

    /**
     * 获得能源建筑列表，根据状态
     *
     * @param status 状态
     * @return 能源建筑列表
     */
    List<IotEnergyBuildingDO> getBuildingListByStatus(Integer status);

}
