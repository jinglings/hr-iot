package cn.iocoder.yudao.module.iot.dal.mysql.scada;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaAlarmDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SCADA 告警配置 Mapper
 *
 * Part of SCADA-011: Create SCADA Mappers
 *
 * @author HR-IoT Team
 */
@Mapper
public interface IotScadaAlarmMapper extends BaseMapperX<IotScadaAlarmDO> {

    /**
     * 根据告警名称查询
     *
     * @param alarmName 告警名称
     * @return 告警配置
     */
    default IotScadaAlarmDO selectByAlarmName(String alarmName) {
        return selectOne(IotScadaAlarmDO::getAlarmName, alarmName);
    }

    /**
     * 根据 Tag ID 查询告警配置列表
     *
     * @param tagId Tag ID
     * @return 告警配置列表
     */
    default List<IotScadaAlarmDO> selectListByTagId(String tagId) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .eq(IotScadaAlarmDO::getTagId, tagId)
                .orderByAsc(IotScadaAlarmDO::getPriority));
    }

    /**
     * 查询启用的告警配置
     *
     * @return 告警配置列表
     */
    default List<IotScadaAlarmDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .eq(IotScadaAlarmDO::getEnabled, true)
                .orderByDesc(IotScadaAlarmDO::getPriority)
                .orderByAsc(IotScadaAlarmDO::getAlarmName));
    }

    /**
     * 根据告警类型查询
     *
     * @param alarmType 告警类型
     * @return 告警配置列表
     */
    default List<IotScadaAlarmDO> selectListByAlarmType(String alarmType) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .eq(IotScadaAlarmDO::getAlarmType, alarmType)
                .eq(IotScadaAlarmDO::getEnabled, true));
    }

    /**
     * 根据优先级查询
     *
     * @param priority 优先级
     * @return 告警配置列表
     */
    default List<IotScadaAlarmDO> selectListByPriority(Integer priority) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .eq(IotScadaAlarmDO::getPriority, priority)
                .eq(IotScadaAlarmDO::getEnabled, true));
    }

    /**
     * 统计启用的告警配置数量
     *
     * @return 数量
     */
    default Long selectEnabledCount() {
        return selectCount(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .eq(IotScadaAlarmDO::getEnabled, true));
    }

    /**
     * 根据 Tag ID 列表批量查询告警配置
     *
     * @param tagIds Tag ID 列表
     * @return 告警配置列表
     */
    default List<IotScadaAlarmDO> selectListByTagIds(List<String> tagIds) {
        return selectList(new LambdaQueryWrapperX<IotScadaAlarmDO>()
                .in(IotScadaAlarmDO::getTagId, tagIds)
                .eq(IotScadaAlarmDO::getEnabled, true));
    }

}
