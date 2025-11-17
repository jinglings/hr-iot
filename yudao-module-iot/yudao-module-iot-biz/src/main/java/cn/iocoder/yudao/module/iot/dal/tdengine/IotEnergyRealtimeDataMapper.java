package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo.IotEnergyDataQueryReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 能源实时数据 {@link IotEnergyRealtimeDataDO} Mapper 接口
 *
 * @author 芋道源码
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotEnergyRealtimeDataMapper {

    /**
     * 创建能源实时数据超级表
     */
    void createSTable();

    /**
     * 查询能源实时数据表是否存在
     *
     * @return 存在则返回表名；不存在则返回 null
     */
    String showSTable();

    /**
     * 插入能源实时数据
     *
     * 如果子表不存在，会自动创建子表
     *
     * @param data 能源实时数据
     */
    void insert(IotEnergyRealtimeDataDO data);

    /**
     * 批量插入能源实时数据
     *
     * @param dataList 能源实时数据列表
     */
    void insertBatch(@Param("list") List<IotEnergyRealtimeDataDO> dataList);

    /**
     * 获得能源实时数据分页
     *
     * @param page 分页对象
     * @param reqVO 查询条件
     * @return 能源实时数据列表
     */
    IPage<IotEnergyRealtimeDataDO> selectPage(IPage<IotEnergyRealtimeDataDO> page,
                                               @Param("reqVO") IotEnergyDataQueryReqVO reqVO);

    /**
     * 查询计量点的最新数据
     *
     * @param meterId 计量点ID
     * @return 最新数据
     */
    IotEnergyRealtimeDataDO selectLatestByMeterId(@Param("meterId") Long meterId);

    /**
     * 查询计量点在指定时间范围内的数据
     *
     * @param meterId 计量点ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据列表
     */
    List<IotEnergyRealtimeDataDO> selectListByMeterIdAndTimeRange(@Param("meterId") Long meterId,
                                                                   @Param("startTime") Long startTime,
                                                                   @Param("endTime") Long endTime);

    /**
     * 按时间范围统计能耗数据（聚合）
     *
     * @param meterId 计量点ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（如：1m, 1h, 1d）
     * @return 聚合数据列表
     */
    List<Map<String, Object>> selectAggregateDataByTimeRange(@Param("meterId") Long meterId,
                                                              @Param("startTime") Long startTime,
                                                              @Param("endTime") Long endTime,
                                                              @Param("interval") String interval);

    /**
     * 按建筑统计能耗数据
     *
     * @param buildingId 建筑ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> selectEnergyStatsByBuilding(@Param("buildingId") Long buildingId,
                                                     @Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime);

    /**
     * 按能源类型统计能耗数据
     *
     * @param energyTypeId 能源类型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    List<Map<String, Object>> selectEnergyStatsByType(@Param("energyTypeId") Long energyTypeId,
                                                       @Param("startTime") Long startTime,
                                                       @Param("endTime") Long endTime);

}
