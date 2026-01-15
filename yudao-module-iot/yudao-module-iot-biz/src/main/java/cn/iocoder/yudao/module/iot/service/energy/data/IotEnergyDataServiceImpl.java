package cn.iocoder.yudao.module.iot.service.energy.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo.IotEnergyDataQueryReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotEnergyRealtimeDataMapper;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IoT 能源数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyDataServiceImpl implements IotEnergyDataService {

    @Resource
    private IotEnergyRealtimeDataMapper realtimeDataMapper;

    @Resource
    private IotEnergyMeterService meterService;

    @Override
    public void saveRealtimeData(IotEnergyRealtimeDataDO data) {
        try {
            realtimeDataMapper.insert(data);
        } catch (Exception e) {
            log.error("[saveRealtimeData][保存能源实时数据失败，meterId({})]", data.getMeterId(), e);
            throw e;
        }
    }

    @Override
    public void saveRealtimeDataBatch(List<IotEnergyRealtimeDataDO> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            realtimeDataMapper.insertBatch(dataList);
        } catch (Exception e) {
            log.error("[saveRealtimeDataBatch][批量保存能源实时数据失败，数量({})]", dataList.size(), e);
            throw e;
        }
    }

    @Override
    public PageResult<IotEnergyRealtimeDataDO> getRealtimeDataPage(IotEnergyDataQueryReqVO reqVO) {
        // 确保子表存在
        ensureTableExistsForMeter(reqVO.getMeterId());

        IPage<IotEnergyRealtimeDataDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<IotEnergyRealtimeDataDO> result = realtimeDataMapper.selectPage(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public IotEnergyRealtimeDataDO getLatestDataByMeterId(Long meterId) {
        // 确保子表存在
        ensureTableExistsForMeter(meterId);

        return realtimeDataMapper.selectLatestByMeterId(meterId);
    }

    @Override
    public List<IotEnergyRealtimeDataDO> getDataListByMeterIdAndTimeRange(Long meterId, Long startTime, Long endTime) {
        // 确保子表存在
        ensureTableExistsForMeter(meterId);

        return realtimeDataMapper.selectListByMeterIdAndTimeRange(meterId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getAggregateDataByTimeRange(Long meterId, Long startTime, Long endTime, String interval) {
        // 确保子表存在
        ensureTableExistsForMeter(meterId);

        return realtimeDataMapper.selectAggregateDataByTimeRange(meterId, startTime, endTime, interval);
    }

    /**
     * 确保指定计量点的子表存在
     *
     * @param meterId 计量点ID
     */
    private void ensureTableExistsForMeter(Long meterId) {
        if (meterId == null) {
            return;
        }

        try {
            // 获取计量点信息
            IotEnergyMeterDO meter = meterService.getMeter(meterId);
            if (meter == null) {
                log.warn("[ensureTableExistsForMeter][计量点({})不存在]", meterId);
                return;
            }

            // 如果是虚拟表，不需要创建子表
            if (Boolean.TRUE.equals(meter.getIsVirtual())) {
                return;
            }

            // 确保子表存在
            realtimeDataMapper.ensureTableExists(
                    meterId,
                    meter.getEnergyTypeId(),
                    meter.getBuildingId(),
                    meter.getAreaId(),
                    meter.getFloorId(),
                    meter.getRoomId(),
                    meter.getTenantId()
            );
        } catch (Exception e) {
            log.error("[ensureTableExistsForMeter][确保计量点({})子表存在失败]", meterId, e);
            // 不抛出异常，让查询继续执行，如果表真的不存在会在查询时报错
        }
    }

    @Override
    public Map<String, Object> getEnergyStatsByBuilding(Long buildingId, Long startTime, Long endTime) {
        return realtimeDataMapper.selectEnergyStatsByBuilding(buildingId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getEnergyStatsByType(Long energyTypeId, Long startTime, Long endTime) {
        return realtimeDataMapper.selectEnergyStatsByType(energyTypeId, startTime, endTime);
    }

}
