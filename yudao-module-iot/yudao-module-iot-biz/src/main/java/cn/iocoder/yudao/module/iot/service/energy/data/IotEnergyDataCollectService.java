package cn.iocoder.yudao.module.iot.service.energy.data;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

/**
 * IoT 能源数据采集 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyDataCollectService {

    /**
     * 处理设备属性上报消息，提取能源数据并存储
     *
     * @param device 设备信息
     * @param message 设备消息
     */
    void processDevicePropertyReport(IotDeviceDO device, IotDeviceMessage message);

}
