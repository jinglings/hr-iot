package cn.iocoder.yudao.module.iot.controller.admin.scada;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.scada.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaControlLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.scada.IotScadaControlLogMapper;
import cn.iocoder.yudao.module.iot.service.scada.ScadaBridgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * SCADA 控制器
 *
 * 提供 SCADA 可视化系统的 REST API 接口
 *
 * Part of SCADA-014: Create SCADA REST API Controller
 *
 * @author HR-IoT Team
 */
@Tag(name = "管理后台 - SCADA 可视化")
@RestController
@RequestMapping("/iot/scada")
@Validated
public class IotScadaController {

    @Resource
    private ScadaBridgeService scadaBridgeService;

    @Resource
    private IotScadaControlLogMapper controlLogMapper;

    // ========== 设备相关接口 ==========

    @GetMapping("/devices")
    @Operation(summary = "获取 SCADA 设备列表", description = "获取所有可用于 SCADA 可视化的设备")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaDeviceVO>> getDevices() {
        List<ScadaDeviceVO> devices = scadaBridgeService.getScadaDevices();
        return success(devices);
    }

    @GetMapping("/devices/{id}")
    @Operation(summary = "获取设备详情", description = "获取指定设备的 SCADA 详细信息")
    @Parameter(name = "id", description = "设备 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<ScadaDeviceVO> getDevice(@PathVariable("id") Long id) {
        ScadaDeviceVO device = scadaBridgeService.getScadaDevice(id);
        return success(device);
    }

    @GetMapping("/devices/{id}/realtime")
    @Operation(summary = "获取设备实时数据", description = "获取指定设备的最新属性值")
    @Parameter(name = "id", description = "设备 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<Map<String, Object>> getDeviceRealtime(@PathVariable("id") Long id) {
        Map<String, Object> realtime = scadaBridgeService.getDeviceRealtime(id);
        return success(realtime);
    }

    @PostMapping("/devices/realtime/batch")
    @Operation(summary = "批量获取设备实时数据", description = "批量获取多个设备的最新属性值")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<Map<Long, Map<String, Object>>> getDevicesRealtime(
            @RequestBody List<Long> deviceIds) {
        Map<Long, Map<String, Object>> result = scadaBridgeService.getDevicesRealtime(deviceIds);
        return success(result);
    }

    @GetMapping("/devices/{id}/history")
    @Operation(summary = "获取设备历史数据", description = "获取指定设备的历史属性数据")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaHistoryPointVO>> getDeviceHistory(
            @PathVariable("id") Long id,
            @Valid ScadaHistoryQueryReqVO reqVO) {
        reqVO.setDeviceId(id);
        List<ScadaHistoryPointVO> history = scadaBridgeService.getDeviceHistory(reqVO);
        return success(history);
    }

    @GetMapping("/devices/{id}/tags")
    @Operation(summary = "获取设备 Tag 映射", description = "获取指定设备的 SCADA Tag 映射配置")
    @Parameter(name = "id", description = "设备 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaTagMappingVO>> getDeviceTagMappings(@PathVariable("id") Long id) {
        List<ScadaTagMappingVO> tags = scadaBridgeService.getDeviceTagMappings(id);
        return success(tags);
    }

    // ========== 控制命令接口 ==========

    @PostMapping("/devices/{id}/control")
    @Operation(summary = "发送控制命令", description = "向指定设备发送控制命令")
    @Parameter(name = "id", description = "设备 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:control')")
    public CommonResult<ScadaControlCommandRespVO> sendControlCommand(
            @PathVariable("id") Long id,
            @Valid @RequestBody ScadaControlCommandReqVO reqVO) {
        reqVO.setDeviceId(id);
        ScadaControlCommandRespVO result = scadaBridgeService.handleControlCommand(reqVO);
        return success(result);
    }

    @PostMapping("/devices/{id}/property")
    @Operation(summary = "设置设备属性", description = "设置指定设备的属性值")
    @Parameters({
            @Parameter(name = "id", description = "设备 ID", required = true, example = "1"),
            @Parameter(name = "property", description = "属性标识符", required = true, example = "status"),
            @Parameter(name = "value", description = "属性值", required = true, example = "true")
    })
    @PreAuthorize("@ss.hasPermission('iot:scada:control')")
    public CommonResult<Boolean> setDeviceProperty(
            @PathVariable("id") Long id,
            @RequestParam("property") String property,
            @RequestParam("value") String value) {
        boolean success = scadaBridgeService.setDeviceProperty(id, property, value);
        return success(success);
    }

    @PostMapping("/devices/{id}/service/{serviceName}")
    @Operation(summary = "调用设备服务", description = "调用指定设备的服务")
    @Parameters({
            @Parameter(name = "id", description = "设备 ID", required = true, example = "1"),
            @Parameter(name = "serviceName", description = "服务名称", required = true, example = "start")
    })
    @PreAuthorize("@ss.hasPermission('iot:scada:control')")
    public CommonResult<Boolean> invokeDeviceService(
            @PathVariable("id") Long id,
            @PathVariable("serviceName") String serviceName,
            @RequestBody(required = false) Map<String, Object> params) {
        boolean success = scadaBridgeService.invokeDeviceService(id, serviceName,
                params != null ? params : Map.of());
        return success(success);
    }

    // ========== Tag 接口 ==========

    @GetMapping("/tags/{tagName}")
    @Operation(summary = "获取 Tag 值", description = "根据 Tag 名称获取当前值")
    @Parameter(name = "tagName", description = "Tag 名称", required = true, example = "Pump1_Status")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<Object> getTagValue(@PathVariable("tagName") String tagName) {
        Object value = scadaBridgeService.getTagValue(tagName);
        return success(value);
    }

    @PostMapping("/tags/batch")
    @Operation(summary = "批量获取 Tag 值", description = "批量获取多个 Tag 的当前值")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<Map<String, Object>> getTagValues(@RequestBody List<String> tagNames) {
        Map<String, Object> values = scadaBridgeService.getTagValues(tagNames);
        return success(values);
    }

    // ========== 告警接口 ==========

    @GetMapping("/alarms")
    @Operation(summary = "获取活动告警列表", description = "获取所有活动状态的告警")
    @PreAuthorize("@ss.hasPermission('iot:scada:alarm:query')")
    public CommonResult<List<ScadaAlarmVO>> getActiveAlarms() {
        List<ScadaAlarmVO> alarms = scadaBridgeService.getActiveAlarms();
        return success(alarms);
    }

    @GetMapping("/devices/{id}/alarms")
    @Operation(summary = "获取设备活动告警", description = "获取指定设备的活动告警")
    @Parameter(name = "id", description = "设备 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:alarm:query')")
    public CommonResult<List<ScadaAlarmVO>> getDeviceActiveAlarms(@PathVariable("id") Long id) {
        List<ScadaAlarmVO> alarms = scadaBridgeService.getDeviceActiveAlarms(id);
        return success(alarms);
    }

    @PostMapping("/alarms/{id}/acknowledge")
    @Operation(summary = "确认告警", description = "确认指定的告警")
    @Parameter(name = "id", description = "告警历史记录 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:scada:alarm:ack')")
    public CommonResult<Boolean> acknowledgeAlarm(@PathVariable("id") Long id) {
        boolean success = scadaBridgeService.acknowledgeAlarm(id,
                cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname());
        return success(success);
    }

    @PostMapping("/alarms/{id}/close")
    @Operation(summary = "关闭告警", description = "关闭指定的告警")
    @Parameters({
            @Parameter(name = "id", description = "告警历史记录 ID", required = true, example = "1"),
            @Parameter(name = "notes", description = "备注", example = "已处理")
    })
    @PreAuthorize("@ss.hasPermission('iot:scada:alarm:close')")
    public CommonResult<Boolean> closeAlarm(
            @PathVariable("id") Long id,
            @RequestParam(value = "notes", required = false) String notes) {
        boolean success = scadaBridgeService.closeAlarm(id,
                cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname(), notes);
        return success(success);
    }

    @GetMapping("/alarms/statistics")
    @Operation(summary = "获取告警统计", description = "获取各状态告警的数量统计")
    @PreAuthorize("@ss.hasPermission('iot:scada:alarm:query')")
    public CommonResult<Map<Integer, Long>> getAlarmStatistics() {
        Map<Integer, Long> statistics = scadaBridgeService.getAlarmStatistics();
        return success(statistics);
    }

    // ========== 控制日志接口 ==========

    @GetMapping("/control-logs")
    @Operation(summary = "获取控制日志分页", description = "分页查询控制操作日志")
    @PreAuthorize("@ss.hasPermission('iot:scada:log:query')")
    public CommonResult<PageResult<ScadaControlLogRespVO>> getControlLogPage(
            @Valid ScadaControlLogPageReqVO reqVO) {
        PageResult<IotScadaControlLogDO> pageResult = controlLogMapper.selectPage(reqVO);
        // 转换为 VO
        List<ScadaControlLogRespVO> list = pageResult.getList().stream()
                .map(this::convertToControlLogVO)
                .collect(java.util.stream.Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/control-logs/statistics")
    @Operation(summary = "获取控制日志统计", description = "获取控制操作的成功/失败统计")
    @PreAuthorize("@ss.hasPermission('iot:scada:log:query')")
    public CommonResult<Map<Integer, Long>> getControlLogStatistics(
            @RequestParam(value = "startTime", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime endTime) {
        Map<Integer, Long> statistics = scadaBridgeService.getControlLogStatistics(startTime, endTime);
        return success(statistics);
    }

    // ========== 仪表板接口 ==========

    @GetMapping("/dashboards")
    @Operation(summary = "获取仪表板列表", description = "获取所有可用的 SCADA 仪表板")
    @PreAuthorize("@ss.hasPermission('iot:scada:dashboard:query')")
    public CommonResult<List<ScadaDashboardVO>> getDashboards() {
        List<ScadaDashboardVO> dashboards = scadaBridgeService.getDashboards();
        return success(dashboards);
    }

    @GetMapping("/dashboards/default")
    @Operation(summary = "获取默认仪表板", description = "获取默认的 SCADA 仪表板")
    @PreAuthorize("@ss.hasPermission('iot:scada:dashboard:query')")
    public CommonResult<ScadaDashboardVO> getDefaultDashboard() {
        ScadaDashboardVO dashboard = scadaBridgeService.getDefaultDashboard();
        return success(dashboard);
    }

    @GetMapping("/dashboards/{dashboardId}/url")
    @Operation(summary = "获取仪表板访问 URL", description = "生成 FUXA 仪表板的访问 URL")
    @Parameters({
            @Parameter(name = "dashboardId", description = "仪表板 ID", required = true, example = "uuid-xxx"),
            @Parameter(name = "token", description = "访问令牌", example = "jwt-token")
    })
    @PreAuthorize("@ss.hasPermission('iot:scada:dashboard:query')")
    public CommonResult<String> getFuxaUrl(
            @PathVariable("dashboardId") String dashboardId,
            @RequestParam(value = "token", required = false) String token) {
        String url = scadaBridgeService.generateFuxaUrl(dashboardId, token);
        return success(url);
    }

    // ========== 私有方法 ==========

    /**
     * 转换控制日志为 VO
     */
    private ScadaControlLogRespVO convertToControlLogVO(IotScadaControlLogDO log) {
        ScadaControlLogRespVO vo = new ScadaControlLogRespVO();
        vo.setId(log.getId());
        vo.setDeviceId(log.getDeviceId());
        vo.setDeviceName(log.getDeviceName());
        vo.setCommandName(log.getCommandName());
        vo.setCommandParams(log.getCommandParams());
        vo.setOldValue(log.getOldValue());
        vo.setNewValue(log.getNewValue());
        vo.setExecutionStatus(log.getExecutionStatus());
        vo.setExecutionStatusDesc(log.getExecutionStatus() == 1 ? "成功" : "失败");
        vo.setErrorMessage(log.getErrorMessage());
        vo.setUserId(log.getUserId());
        vo.setUserName(log.getUserName());
        vo.setClientIp(log.getClientIp());
        vo.setUserAgent(log.getUserAgent());
        vo.setExecutionTime(log.getExecutionTime());
        vo.setSource(log.getSource());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

}
