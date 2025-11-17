package cn.iocoder.yudao.module.iot.service.shadow;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowHistoryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateDesiredReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateReportedReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.shadow.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.shadow.IotDeviceShadowHistoryDO;
import cn.iocoder.yudao.module.iot.dal.mysql.shadow.IotDeviceShadowHistoryMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.shadow.IotDeviceShadowMapper;
import cn.iocoder.yudao.module.iot.enums.shadow.ShadowChangeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * IoT 设备影子 Service 实现类
 *
 * @author AI Assistant
 */
@Slf4j
@Service
public class IotDeviceShadowServiceImpl implements IotDeviceShadowService {

    @Resource
    private IotDeviceShadowMapper shadowMapper;
    @Resource
    private IotDeviceShadowHistoryMapper historyMapper;

    @Override
    public IotDeviceShadowRespVO getDeviceShadow(Long deviceId) {
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(deviceId);
        if (shadow == null) {
            // 如果影子不存在，创建一个空影子
            shadow = createEmptyShadow(deviceId);
        }
        return convertToRespVO(shadow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotDeviceShadowRespVO updateDesiredState(IotDeviceShadowUpdateDesiredReqVO reqVO) {
        log.info("[updateDesiredState][更新设备期望状态: deviceId={}, desired={}]",
                reqVO.getDeviceId(), reqVO.getDesired());

        // 1. 获取或创建影子
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(reqVO.getDeviceId());
        if (shadow == null) {
            shadow = createEmptyShadow(reqVO.getDeviceId());
        }

        // 2. 版本号校验（乐观锁）
        if (reqVO.getVersion() != null && !reqVO.getVersion().equals(shadow.getVersion())) {
            throw new RuntimeException("版本号冲突，请刷新后重试");
        }

        // 3. 合并期望状态
        Map<String, Object> currentDesired = parseJson(shadow.getDesiredState());
        Map<String, Object> newDesired = new HashMap<>(currentDesired);
        newDesired.putAll(reqVO.getDesired());

        // 4. 保存变更前状态
        String beforeState = shadow.getDesiredState();

        // 5. 更新影子
        shadow.setDesiredState(JSONUtil.toJsonStr(newDesired));
        shadow.setVersion(shadow.getVersion() + 1);
        shadow.setDesiredVersion(shadow.getDesiredVersion() + 1);
        shadow.setLastDesiredTime(LocalDateTime.now());
        shadowMapper.updateById(shadow);

        // 6. 记录变更历史
        recordHistory(shadow, ShadowChangeTypeEnum.DESIRED, beforeState,
                shadow.getDesiredState(), computeDelta(currentDesired, newDesired));

        log.info("[updateDesiredState][期望状态更新成功: deviceId={}, version={}]",
                reqVO.getDeviceId(), shadow.getVersion());

        return convertToRespVO(shadow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotDeviceShadowRespVO updateReportedState(IotDeviceShadowUpdateReportedReqVO reqVO) {
        log.info("[updateReportedState][设备上报实际状态: deviceId={}, reported={}]",
                reqVO.getDeviceId(), reqVO.getReported());

        // 1. 获取或创建影子
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(reqVO.getDeviceId());
        if (shadow == null) {
            shadow = createEmptyShadow(reqVO.getDeviceId());
        }

        // 2. 合并实际状态
        Map<String, Object> currentReported = parseJson(shadow.getReportedState());
        Map<String, Object> newReported = new HashMap<>(currentReported);
        newReported.putAll(reqVO.getReported());

        // 3. 保存变更前状态
        String beforeState = shadow.getReportedState();

        // 4. 更新影子
        shadow.setReportedState(JSONUtil.toJsonStr(newReported));
        shadow.setVersion(shadow.getVersion() + 1);
        shadow.setReportedVersion(shadow.getReportedVersion() + 1);
        shadow.setLastReportedTime(LocalDateTime.now());
        shadowMapper.updateById(shadow);

        // 5. 记录变更历史
        recordHistory(shadow, ShadowChangeTypeEnum.REPORTED, beforeState,
                shadow.getReportedState(), computeDelta(currentReported, newReported));

        // 6. 检查是否需要清除已满足的期望状态
        clearSatisfiedDesired(shadow, newReported);

        log.info("[updateReportedState][实际状态更新成功: deviceId={}, version={}]",
                reqVO.getDeviceId(), shadow.getVersion());

        return convertToRespVO(shadow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDesiredProperty(Long deviceId, String property) {
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(deviceId);
        if (shadow == null) {
            return;
        }

        Map<String, Object> desired = parseJson(shadow.getDesiredState());
        if (desired.remove(property) != null) {
            shadow.setDesiredState(JSONUtil.toJsonStr(desired));
            shadow.setVersion(shadow.getVersion() + 1);
            shadow.setDesiredVersion(shadow.getDesiredVersion() + 1);
            shadowMapper.updateById(shadow);

            log.info("[deleteDesiredProperty][删除期望状态属性: deviceId={}, property={}]",
                    deviceId, property);
        }
    }

    @Override
    public Map<String, Object> getDeltaState(Long deviceId) {
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(deviceId);
        if (shadow == null) {
            return new HashMap<>();
        }

        Map<String, Object> desired = parseJson(shadow.getDesiredState());
        Map<String, Object> reported = parseJson(shadow.getReportedState());

        return computeDelta(reported, desired);
    }

    @Override
    public PageResult<IotDeviceShadowHistoryRespVO> getShadowHistory(Long deviceId, Integer pageNo, Integer pageSize) {
        PageResult<IotDeviceShadowHistoryDO> pageResult =
                historyMapper.selectPageByDeviceId(deviceId, pageNo, pageSize);

        return new PageResult<>(
                pageResult.getList().stream().map(this::convertHistoryToRespVO).toList(),
                pageResult.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearDeviceShadow(Long deviceId) {
        IotDeviceShadowDO shadow = shadowMapper.selectByDeviceId(deviceId);
        if (shadow != null) {
            shadowMapper.deleteById(shadow.getId());
            log.info("[clearDeviceShadow][清除设备影子: deviceId={}]", deviceId);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建空影子
     */
    private IotDeviceShadowDO createEmptyShadow(Long deviceId) {
        IotDeviceShadowDO shadow = IotDeviceShadowDO.builder()
                .deviceId(deviceId)
                .desiredState("{}")
                .reportedState("{}")
                .metadata("{}")
                .version(1)
                .desiredVersion(0)
                .reportedVersion(0)
                .build();
        shadowMapper.insert(shadow);
        log.info("[createEmptyShadow][创建空设备影子: deviceId={}]", deviceId);
        return shadow;
    }

    /**
     * 计算差量状态（delta = desired - reported）
     * 即：期望有但实际没有，或者期望与实际值不同的属性
     */
    private Map<String, Object> computeDelta(Map<String, Object> reported, Map<String, Object> desired) {
        Map<String, Object> delta = new HashMap<>();

        for (Map.Entry<String, Object> entry : desired.entrySet()) {
            String key = entry.getKey();
            Object desiredValue = entry.getValue();
            Object reportedValue = reported.get(key);

            // 如果实际状态中没有此属性，或者值不同，则加入差量
            if (reportedValue == null || !desiredValue.equals(reportedValue)) {
                delta.put(key, desiredValue);
            }
        }

        return delta;
    }

    /**
     * 清除已满足的期望状态
     * 当设备上报的实际状态与期望状态一致时，自动清除该期望状态
     */
    private void clearSatisfiedDesired(IotDeviceShadowDO shadow, Map<String, Object> newReported) {
        Map<String, Object> desired = parseJson(shadow.getDesiredState());
        if (desired.isEmpty()) {
            return;
        }

        boolean hasChanges = false;
        for (String key : new HashMap<>(desired).keySet()) {
            Object desiredValue = desired.get(key);
            Object reportedValue = newReported.get(key);

            // 如果期望值与实际值一致，清除该期望
            if (desiredValue != null && desiredValue.equals(reportedValue)) {
                desired.remove(key);
                hasChanges = true;
                log.info("[clearSatisfiedDesired][清除已满足的期望状态: deviceId={}, property={}]",
                        shadow.getDeviceId(), key);
            }
        }

        if (hasChanges) {
            shadow.setDesiredState(JSONUtil.toJsonStr(desired));
            shadow.setVersion(shadow.getVersion() + 1);
            shadowMapper.updateById(shadow);
        }
    }

    /**
     * 记录变更历史
     */
    private void recordHistory(IotDeviceShadowDO shadow, ShadowChangeTypeEnum changeType,
                              String beforeState, String afterState, Map<String, Object> delta) {
        IotDeviceShadowHistoryDO history = IotDeviceShadowHistoryDO.builder()
                .deviceId(shadow.getDeviceId())
                .shadowId(shadow.getId())
                .changeType(changeType.getType())
                .beforeState(beforeState)
                .afterState(afterState)
                .deltaState(JSONUtil.toJsonStr(delta))
                .version(shadow.getVersion())
                .build();
        historyMapper.insert(history);
    }

    /**
     * 解析JSON字符串为Map
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return new HashMap<>();
        }
        try {
            return JSONUtil.toBean(json, Map.class);
        } catch (Exception e) {
            log.error("[parseJson][JSON解析失败: json={}]", json, e);
            return new HashMap<>();
        }
    }

    /**
     * 转换为响应VO
     */
    private IotDeviceShadowRespVO convertToRespVO(IotDeviceShadowDO shadow) {
        IotDeviceShadowRespVO respVO = new IotDeviceShadowRespVO();
        respVO.setId(shadow.getId());
        respVO.setDeviceId(shadow.getDeviceId());
        respVO.setDesired(parseJson(shadow.getDesiredState()));
        respVO.setReported(parseJson(shadow.getReportedState()));
        respVO.setDelta(computeDelta(
                parseJson(shadow.getReportedState()),
                parseJson(shadow.getDesiredState())
        ));
        respVO.setMetadata(parseJson(shadow.getMetadata()));
        respVO.setVersion(shadow.getVersion());
        respVO.setDesiredVersion(shadow.getDesiredVersion());
        respVO.setReportedVersion(shadow.getReportedVersion());
        respVO.setLastDesiredTime(shadow.getLastDesiredTime());
        respVO.setLastReportedTime(shadow.getLastReportedTime());
        respVO.setCreateTime(shadow.getCreateTime());
        return respVO;
    }

    /**
     * 转换历史为响应VO
     */
    private IotDeviceShadowHistoryRespVO convertHistoryToRespVO(IotDeviceShadowHistoryDO history) {
        IotDeviceShadowHistoryRespVO respVO = new IotDeviceShadowHistoryRespVO();
        respVO.setId(history.getId());
        respVO.setDeviceId(history.getDeviceId());
        respVO.setShadowId(history.getShadowId());
        respVO.setChangeType(history.getChangeType());
        respVO.setBeforeState(parseJson(history.getBeforeState()));
        respVO.setAfterState(parseJson(history.getAfterState()));
        respVO.setDeltaState(parseJson(history.getDeltaState()));
        respVO.setVersion(history.getVersion());
        respVO.setCreateTime(history.getCreateTime());
        return respVO;
    }

}
