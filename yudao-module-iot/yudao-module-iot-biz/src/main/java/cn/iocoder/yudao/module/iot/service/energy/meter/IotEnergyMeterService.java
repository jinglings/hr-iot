package cn.iocoder.yudao.module.iot.service.energy.meter;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源计量点 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyMeterService {

    /**
     * 创建计量点
     */
    Long createMeter(@Valid IotEnergyMeterSaveReqVO createReqVO);

    /**
     * 更新计量点
     */
    void updateMeter(@Valid IotEnergyMeterSaveReqVO updateReqVO);

    /**
     * 删除计量点
     */
    void deleteMeter(Long id);

    /**
     * 获得计量点
     */
    IotEnergyMeterDO getMeter(Long id);

    /**
     * 获得计量点列表
     */
    List<IotEnergyMeterDO> getMeterList(Collection<Long> ids);

    /**
     * 获得计量点分页
     */
    PageResult<IotEnergyMeterDO> getMeterPage(IotEnergyMeterPageReqVO pageReqVO);

    /**
     * 获得指定能源类型的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByEnergyTypeId(Long energyTypeId);

    /**
     * 获得指定设备的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByDeviceId(Long deviceId);

    /**
     * 获得指定建筑的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByBuildingId(Long buildingId);

    /**
     * 获得指定区域的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByAreaId(Long areaId);

    /**
     * 获得指定楼层的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByFloorId(Long floorId);

    /**
     * 获得指定房间的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByRoomId(Long roomId);

    /**
     * 获得指定父级的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByParentId(Long parentId);

    /**
     * 获得指定状态的计量点列表
     */
    List<IotEnergyMeterDO> getMeterListByStatus(Integer status);

}
