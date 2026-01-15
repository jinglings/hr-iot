package cn.iocoder.yudao.module.iot.service.device.debug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDebugLogDO;

import javax.validation.Valid;

/**
 * IoT 设备调试 Service 接口
 *
 * @author AI
 */
public interface IotDeviceDebugService {

    /**
     * 模拟属性上报
     *
     * @param reqVO 请求参数
     * @return 调试结果
     */
    IotDeviceDebugResultVO simulatePropertyReport(@Valid IotDevicePropertyReportReqVO reqVO);

    /**
     * 下发属性设置
     *
     * @param deviceId   设备ID
     * @param identifier 属性标识
     * @param value      属性值
     * @return 调试结果
     */
    IotDeviceDebugResultVO sendPropertySet(Long deviceId, String identifier, String value);

    /**
     * 调用设备服务
     *
     * @param reqVO 请求参数
     * @return 调试结果
     */
    IotDeviceDebugResultVO invokeService(@Valid IotDeviceServiceInvokeReqVO reqVO);

    /**
     * 获取调试日志分页
     *
     * @param pageReqVO 分页参数
     * @return 调试日志分页
     */
    PageResult<IotDeviceDebugLogDO> getDebugLogPage(IotDeviceDebugLogPageReqVO pageReqVO);

    /**
     * 清理过期调试日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int days);

}
