package cn.iocoder.yudao.module.iot.dal.mysql.scada;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.scada.vo.ScadaControlLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaControlLogDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SCADA 控制操作日志 Mapper
 *
 * Part of SCADA-011: Create SCADA Mappers
 *
 * @author HR-IoT Team
 */
@Mapper
public interface IotScadaControlLogMapper extends BaseMapperX<IotScadaControlLogDO> {

    /**
     * 分页查询控制日志
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    default PageResult<IotScadaControlLogDO> selectPage(ScadaControlLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .eqIfPresent(IotScadaControlLogDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotScadaControlLogDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotScadaControlLogDO::getCommandName, reqVO.getCommandName())
                .eqIfPresent(IotScadaControlLogDO::getExecutionStatus, reqVO.getExecutionStatus())
                .eqIfPresent(IotScadaControlLogDO::getUserId, reqVO.getUserId())
                .likeIfPresent(IotScadaControlLogDO::getUserName, reqVO.getUserName())
                .eqIfPresent(IotScadaControlLogDO::getSource, reqVO.getSource())
                .geIfPresent(IotScadaControlLogDO::getCreateTime, reqVO.getStartTime())
                .leIfPresent(IotScadaControlLogDO::getCreateTime, reqVO.getEndTime())
                .orderByDesc(IotScadaControlLogDO::getId));
    }

    /**
     * 查询设备的控制日志
     *
     * @param deviceId 设备 ID
     * @param limit    限制数量
     * @return 控制日志列表
     */
    default List<IotScadaControlLogDO> selectListByDeviceId(Long deviceId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .eq(IotScadaControlLogDO::getDeviceId, deviceId)
                .orderByDesc(IotScadaControlLogDO::getId)
                .last(limit != null ? "LIMIT " + limit : ""));
    }

    /**
     * 查询用户的控制日志
     *
     * @param userId 用户 ID
     * @param limit  限制数量
     * @return 控制日志列表
     */
    default List<IotScadaControlLogDO> selectListByUserId(Long userId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .eq(IotScadaControlLogDO::getUserId, userId)
                .orderByDesc(IotScadaControlLogDO::getId)
                .last(limit != null ? "LIMIT " + limit : ""));
    }

    /**
     * 查询时间范围内的控制日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 控制日志列表
     */
    default List<IotScadaControlLogDO> selectListByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .ge(IotScadaControlLogDO::getCreateTime, startTime)
                .le(IotScadaControlLogDO::getCreateTime, endTime)
                .orderByDesc(IotScadaControlLogDO::getId));
    }

    /**
     * 统计执行状态分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 执行状态 -> 数量 映射
     */
    default Map<Integer, Long> selectCountGroupByStatus(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotScadaControlLogDO>()
                .select("execution_status", "COUNT(1) AS count")
                .ge(startTime != null, "create_time", startTime)
                .le(endTime != null, "create_time", endTime)
                .groupBy("execution_status"));
        return result.stream().collect(Collectors.toMap(
                map -> Integer.valueOf(map.get("execution_status").toString()),
                map -> Long.valueOf(map.get("count").toString())));
    }

    /**
     * 统计设备的控制次数
     *
     * @param limit 返回数量限制
     * @return 设备 ID -> 控制次数 映射
     */
    default Map<Long, Long> selectDeviceControlCounts(int limit) {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotScadaControlLogDO>()
                .select("device_id", "COUNT(1) AS count")
                .groupBy("device_id")
                .orderByDesc("count")
                .last("LIMIT " + limit));
        return result.stream().collect(Collectors.toMap(
                map -> Long.valueOf(map.get("device_id").toString()),
                map -> Long.valueOf(map.get("count").toString())));
    }

    /**
     * 统计指定时间范围内的控制日志数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 数量
     */
    default Long selectCountByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .geIfPresent(IotScadaControlLogDO::getCreateTime, startTime)
                .leIfPresent(IotScadaControlLogDO::getCreateTime, endTime));
    }

    /**
     * 查询失败的控制日志
     *
     * @param limit 限制数量
     * @return 失败的控制日志列表
     */
    default List<IotScadaControlLogDO> selectFailedLogs(Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotScadaControlLogDO>()
                .eq(IotScadaControlLogDO::getExecutionStatus, 0) // 失败状态
                .orderByDesc(IotScadaControlLogDO::getId)
                .last(limit != null ? "LIMIT " + limit : ""));
    }

}
