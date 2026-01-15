package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDebugLogDO;
import cn.iocoder.yudao.module.iot.service.device.debug.IotDeviceDebugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 设备调试
 *
 * @author AI
 */
@Tag(name = "管理后台 - IoT 设备调试")
@RestController
@RequestMapping("/iot/device-debug")
@Validated
public class IotDeviceDebugController {

    @Resource
    private IotDeviceDebugService debugService;

    @PostMapping("/property-report")
    @Operation(summary = "模拟属性上报")
    @PreAuthorize("@ss.hasPermission('iot:device:debug')")
    public CommonResult<IotDeviceDebugResultVO> simulatePropertyReport(
            @Valid @RequestBody IotDevicePropertyReportReqVO reqVO) {
        return success(debugService.simulatePropertyReport(reqVO));
    }

    @PostMapping("/property-set")
    @Operation(summary = "下发属性设置")
    @PreAuthorize("@ss.hasPermission('iot:device:debug')")
    public CommonResult<IotDeviceDebugResultVO> sendPropertySet(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("identifier") String identifier,
            @RequestParam("value") String value) {
        return success(debugService.sendPropertySet(deviceId, identifier, value));
    }

    @PostMapping("/service-invoke")
    @Operation(summary = "调用设备服务")
    @PreAuthorize("@ss.hasPermission('iot:device:debug')")
    public CommonResult<IotDeviceDebugResultVO> invokeService(
            @Valid @RequestBody IotDeviceServiceInvokeReqVO reqVO) {
        return success(debugService.invokeService(reqVO));
    }

    @GetMapping("/log/page")
    @Operation(summary = "获取调试日志分页")
    @PreAuthorize("@ss.hasPermission('iot:device:debug')")
    public CommonResult<PageResult<IotDeviceDebugLogRespVO>> getDebugLogPage(
            @Valid IotDeviceDebugLogPageReqVO pageReqVO) {
        PageResult<IotDeviceDebugLogDO> pageResult = debugService.getDebugLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceDebugLogRespVO.class));
    }

    @DeleteMapping("/log/clean")
    @Operation(summary = "清理过期调试日志")
    @Parameter(name = "days", description = "保留天数", required = true, example = "7")
    @PreAuthorize("@ss.hasPermission('iot:device:debug')")
    public CommonResult<Integer> cleanExpiredLogs(@RequestParam("days") Integer days) {
        return success(debugService.cleanExpiredLogs(days));
    }

}
