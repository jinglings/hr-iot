package cn.iocoder.yudao.module.iot.service.energy.area;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源区域 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyAreaService {

    /**
     * 创建能源区域
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArea(@Valid IotEnergyAreaSaveReqVO createReqVO);

    /**
     * 更新能源区域
     *
     * @param updateReqVO 更新信息
     */
    void updateArea(@Valid IotEnergyAreaSaveReqVO updateReqVO);

    /**
     * 删除能源区域
     *
     * @param id 编号
     */
    void deleteArea(Long id);

    /**
     * 获得能源区域
     *
     * @param id 编号
     * @return 能源区域
     */
    IotEnergyAreaDO getArea(Long id);

    /**
     * 获得能源区域列表
     *
     * @param ids 编号
     * @return 能源区域列表
     */
    List<IotEnergyAreaDO> getAreaList(Collection<Long> ids);

    /**
     * 获得能源区域分页
     *
     * @param pageReqVO 分页查询
     * @return 能源区域分页
     */
    PageResult<IotEnergyAreaDO> getAreaPage(IotEnergyAreaPageReqVO pageReqVO);

    /**
     * 获得能源区域列表，根据建筑ID
     *
     * @param buildingId 建筑ID
     * @return 能源区域列表
     */
    List<IotEnergyAreaDO> getAreaListByBuildingId(Long buildingId);

    /**
     * 获得能源区域列表，根据状态
     *
     * @param status 状态
     * @return 能源区域列表
     */
    List<IotEnergyAreaDO> getAreaListByStatus(Integer status);

}
