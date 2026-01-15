package cn.iocoder.yudao.module.iot.dal.mysql.scada;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaAlarmHistoryDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SCADA 告警历史记录 Mapper
 *
 * Part of SCADA-011: Create SCADA Mappers
 *
 * @author HR-IoT Team
 */
@Mapper
public interface IotScadaAlarmHistoryMapper extends BaseMapperX<IotScadaAlarmHistoryDO> {

    /**
     * 查询活动告警（未关闭的）
     *
     * @return 活动告警列表
     */
    default List<IotScadaAlarmHistoryDO> selectActiveAlarms() {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .in(IotScadaAlarmHistoryDO::getStatus, 1, 2) // 活动或已确认
                .orderByDesc(IotScadaAlarmHistoryDO::getPriority)
                .orderByDesc(IotScadaAlarmHistoryDO::getTriggeredAt));
    }

    /**
     * 根据告警配置 ID 查询历史记录
     *
     * @param alarmId 告警配置 ID
     * @param limit   限制数量
     * @return 告警历史记录列表
     */
    default List<IotScadaAlarmHistoryDO> selectListByAlarmId(Long alarmId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getAlarmId, alarmId)
                .orderByDesc(IotScadaAlarmHistoryDO::getTriggeredAt)
                .last(limit != null ? "LIMIT " + limit : ""));
    }

    /**
     * 根据 Tag ID 查询告警历史
     *
     * @param tagId Tag ID
     * @param limit 限制数量
     * @return 告警历史记录列表
     */
    default List<IotScadaAlarmHistoryDO> selectListByTagId(String tagId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getTagId, tagId)
                .orderByDesc(IotScadaAlarmHistoryDO::getTriggeredAt)
                .last(limit != null ? "LIMIT " + limit : ""));
    }

    /**
     * 分页查询告警历史
     *
     * @param alarmName 告警名称（模糊匹配）
     * @param status    状态
     * @param priority  优先级
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<IotScadaAlarmHistoryDO> selectPage(String alarmName, Integer status, Integer priority,
            LocalDateTime startTime, LocalDateTime endTime,
            PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .likeIfPresent(IotScadaAlarmHistoryDO::getAlarmName, alarmName)
                .eqIfPresent(IotScadaAlarmHistoryDO::getStatus, status)
                .eqIfPresent(IotScadaAlarmHistoryDO::getPriority, priority)
                .geIfPresent(IotScadaAlarmHistoryDO::getTriggeredAt, startTime)
                .leIfPresent(IotScadaAlarmHistoryDO::getTriggeredAt, endTime)
                .orderByDesc(IotScadaAlarmHistoryDO::getTriggeredAt));
    }

    /**
     * 确认告警
     *
     * @param id             告警记录 ID
     * @param acknowledgedBy 确认人
     * @return 更新行数
     */
    default int acknowledgeAlarm(Long id, String acknowledgedBy) {
        return update(null, new LambdaUpdateWrapper<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getId, id)
                .eq(IotScadaAlarmHistoryDO::getStatus, 1) // 只能确认活动状态的
                .set(IotScadaAlarmHistoryDO::getStatus, 2)
                .set(IotScadaAlarmHistoryDO::getAcknowledgedAt, LocalDateTime.now())
                .set(IotScadaAlarmHistoryDO::getAcknowledgedBy, acknowledgedBy));
    }

    /**
     * 关闭告警
     *
     * @param id       告警记录 ID
     * @param closedBy 关闭人
     * @param notes    备注
     * @return 更新行数
     */
    default int closeAlarm(Long id, String closedBy, String notes) {
        return update(null, new LambdaUpdateWrapper<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getId, id)
                .set(IotScadaAlarmHistoryDO::getStatus, 4)
                .set(IotScadaAlarmHistoryDO::getClosedAt, LocalDateTime.now())
                .set(IotScadaAlarmHistoryDO::getClosedBy, closedBy)
                .set(IotScadaAlarmHistoryDO::getNotes, notes));
    }

    /**
     * 标记告警已恢复
     *
     * @param alarmId 告警配置 ID
     * @param tagId   Tag ID
     * @return 更新行数
     */
    default int markRecovered(Long alarmId, String tagId) {
        return update(null, new LambdaUpdateWrapper<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getAlarmId, alarmId)
                .eq(IotScadaAlarmHistoryDO::getTagId, tagId)
                .in(IotScadaAlarmHistoryDO::getStatus, 1, 2) // 活动或已确认
                .set(IotScadaAlarmHistoryDO::getStatus, 3)
                .set(IotScadaAlarmHistoryDO::getRecoveredAt, LocalDateTime.now()));
    }

    /**
     * 统计各状态告警数量
     *
     * @return 状态 -> 数量 映射
     */
    default Map<Integer, Long> selectCountGroupByStatus() {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<IotScadaAlarmHistoryDO>()
                .select("status", "COUNT(1) AS count")
                .groupBy("status"));
        return result.stream().collect(Collectors.toMap(
                map -> Integer.valueOf(map.get("status").toString()),
                map -> Long.valueOf(map.get("count").toString())));
    }

    /**
     * 统计各优先级告警数量
     *
     * @param activeOnly 是否只统计活动告警
     * @return 优先级 -> 数量 映射
     */
    default Map<Integer, Long> selectCountGroupByPriority(boolean activeOnly) {
        QueryWrapper<IotScadaAlarmHistoryDO> wrapper = new QueryWrapper<IotScadaAlarmHistoryDO>()
                .select("priority", "COUNT(1) AS count")
                .groupBy("priority");
        if (activeOnly) {
            wrapper.in("status", 1, 2);
        }
        List<Map<String, Object>> result = selectMaps(wrapper);
        return result.stream().collect(Collectors.toMap(
                map -> Integer.valueOf(map.get("priority").toString()),
                map -> Long.valueOf(map.get("count").toString())));
    }

    /**
     * 统计活动告警数量
     *
     * @return 活动告警数量
     */
    default Long selectActiveCount() {
        return selectCount(new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .in(IotScadaAlarmHistoryDO::getStatus, 1, 2));
    }

    /**
     * 查询需要确认的告警
     *
     * @return 需要确认的告警列表
     */
    default List<IotScadaAlarmHistoryDO> selectUnacknowledgedAlarms() {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmHistoryDO>()
                .eq(IotScadaAlarmHistoryDO::getStatus, 1) // 活动状态
                .orderByDesc(IotScadaAlarmHistoryDO::getPriority)
                .orderByAsc(IotScadaAlarmHistoryDO::getTriggeredAt));
    }

}
