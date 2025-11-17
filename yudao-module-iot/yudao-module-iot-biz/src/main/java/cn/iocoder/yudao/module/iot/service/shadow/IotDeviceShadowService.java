package cn.iocoder.yudao.module.iot.service.shadow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowHistoryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateDesiredReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateReportedReqVO;

import java.util.Map;

/**
 * IoT 设备影子 Service 接口
 *
 * @author AI Assistant
 */
public interface IotDeviceShadowService {

    /**
     * 获取设备影子
     *
     * @param deviceId 设备ID
     * @return 设备影子
     */
    IotDeviceShadowRespVO getDeviceShadow(Long deviceId);

    /**
     * 更新期望状态（云端下发）
     *
     * @param reqVO 更新请求
     * @return 更新后的影子
     */
    IotDeviceShadowRespVO updateDesiredState(IotDeviceShadowUpdateDesiredReqVO reqVO);

    /**
     * 更新实际状态（设备上报）
     *
     * @param reqVO 更新请求
     * @return 更新后的影子
     */
    IotDeviceShadowRespVO updateReportedState(IotDeviceShadowUpdateReportedReqVO reqVO);

    /**
     * 删除期望状态中的某个属性
     *
     * @param deviceId 设备ID
     * @param property 属性名
     */
    void deleteDesiredProperty(Long deviceId, String property);

    /**
     * 获取差量状态（期望与实际的差异）
     *
     * @param deviceId 设备ID
     * @return 差量状态
     */
    Map<String, Object> getDeltaState(Long deviceId);

    /**
     * 获取设备影子变更历史
     *
     * @param deviceId 设备ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 变更历史分页
     */
    PageResult<IotDeviceShadowHistoryRespVO> getShadowHistory(Long deviceId, Integer pageNo, Integer pageSize);

    /**
     * 清除设备影子
     *
     * @param deviceId 设备ID
     */
    void clearDeviceShadow(Long deviceId);

}
