package cn.iocoder.yudao.module.iot.service.bacnet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryBindReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDiscoveryRecordDO;

import javax.validation.Valid;
import java.util.List;

/**
 * BACnet 设备发现 Service 接口
 *
 * @author 芋道源码
 */
public interface IotBACnetDiscoveryService {

    /**
     * 立即执行设备发现
     *
     * @return 发现的设备列表
     */
    List<IotBACnetDiscoveryRecordDO> discoverDevicesNow();

    /**
     * 定时执行设备发现（由定时任务调用）
     */
    void scheduleDeviceDiscovery();

    /**
     * 绑定 BACnet 设备到 IoT 设备
     *
     * @param bindReqVO 绑定信息
     * @return 设备编号
     */
    Long bindDevice(@Valid IotBACnetDiscoveryBindReqVO bindReqVO);

    /**
     * 删除发现记录
     *
     * @param id 记录编号
     */
    void deleteDiscoveryRecord(Long id);

    /**
     * 获得发现记录
     *
     * @param id 记录编号
     * @return 发现记录
     */
    IotBACnetDiscoveryRecordDO getDiscoveryRecord(Long id);

    /**
     * 获得发现记录分页
     *
     * @param pageReqVO 分页查询
     * @return 发现记录分页
     */
    PageResult<IotBACnetDiscoveryRecordDO> getDiscoveryRecordPage(IotBACnetDiscoveryRecordPageReqVO pageReqVO);

    /**
     * 获得未绑定的设备列表
     *
     * @return 未绑定设备列表
     */
    List<IotBACnetDiscoveryRecordDO> getUnboundDevices();

    /**
     * 获得已绑定的设备列表
     *
     * @return 已绑定设备列表
     */
    List<IotBACnetDiscoveryRecordDO> getBoundDevices();

}
