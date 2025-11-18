package cn.iocoder.yudao.module.iot.dal.mysql.shadow;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.shadow.IotDeviceShadowHistoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 设备影子变更历史 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotDeviceShadowHistoryMapper extends BaseMapperX<IotDeviceShadowHistoryDO> {

    /**
     * 查询设备的影子变更历史
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 变更历史列表
     */
    default List<IotDeviceShadowHistoryDO> selectListByDeviceId(Long deviceId,
                                                                LocalDateTime startTime,
                                                                LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotDeviceShadowHistoryDO>()
                .eq(IotDeviceShadowHistoryDO::getDeviceId, deviceId)
                .betweenIfPresent(IotDeviceShadowHistoryDO::getCreateTime, startTime, endTime)
                .orderByDesc(IotDeviceShadowHistoryDO::getCreateTime));
    }

    /**
     * 分页查询设备影子变更历史
     *
     * @param deviceId 设备ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    default PageResult<IotDeviceShadowHistoryDO> selectPageByDeviceId(Long deviceId, Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<IotDeviceShadowHistoryDO>()
                .eq(IotDeviceShadowHistoryDO::getDeviceId, deviceId)
                .orderByDesc(IotDeviceShadowHistoryDO::getCreateTime));
    }

}
