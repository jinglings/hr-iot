package cn.iocoder.yudao.module.iot.service.energy.virtualMeter;

/**
 * IoT 能源虚拟表计算 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyVirtualMeterService {

    /**
     * 计算虚拟表的值
     *
     * @param meterId 虚拟计量点ID
     * @return 计算结果
     */
    Double calculateVirtualMeterValue(Long meterId);

    /**
     * 计算并保存虚拟表的实时数据
     *
     * @param meterId 虚拟计量点ID
     */
    void calculateAndSaveVirtualMeterData(Long meterId);

}
