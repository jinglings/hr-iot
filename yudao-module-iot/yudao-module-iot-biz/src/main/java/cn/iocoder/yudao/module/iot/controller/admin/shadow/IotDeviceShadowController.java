package cn.iocoder.yudao.module.iot.controller.admin.shadow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowHistoryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateDesiredReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.shadow.vo.IotDeviceShadowUpdateReportedReqVO;
import cn.iocoder.yudao.module.iot.service.shadow.IotDeviceShadowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备影子 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 设备影子")
@RestController
@RequestMapping("/iot/device-shadow")
@Validated
public class IotDeviceShadowController {

    @Resource
    private IotDeviceShadowService deviceShadowService;

    @GetMapping("/get")
    @Operation(summary = "获取设备影子")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceShadowRespVO> getDeviceShadow(@RequestParam("deviceId") Long deviceId) {
        return success(deviceShadowService.getDeviceShadow(deviceId));
    }

    @PutMapping("/update-desired")
    @Operation(summary = "更新期望状态（云端下发）")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<IotDeviceShadowRespVO> updateDesiredState(
            @Valid @RequestBody IotDeviceShadowUpdateDesiredReqVO reqVO) {
        return success(deviceShadowService.updateDesiredState(reqVO));
    }

    @PutMapping("/update-reported")
    @Operation(summary = "更新实际状态（设备上报）")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<IotDeviceShadowRespVO> updateReportedState(
            @Valid @RequestBody IotDeviceShadowUpdateReportedReqVO reqVO) {
        return success(deviceShadowService.updateReportedState(reqVO));
    }

    @DeleteMapping("/delete-desired-property")
    @Operation(summary = "删除期望状态中的某个属性")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @Parameter(name = "property", description = "属性名", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> deleteDesiredProperty(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("property") String property) {
        deviceShadowService.deleteDesiredProperty(deviceId, property);
        return success(true);
    }

    @GetMapping("/get-delta")
    @Operation(summary = "获取差量状态（期望与实际的差异）")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<Map<String, Object>> getDeltaState(@RequestParam("deviceId") Long deviceId) {
        return success(deviceShadowService.getDeltaState(deviceId));
    }

    @GetMapping("/history")
    @Operation(summary = "获取设备影子变更历史")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页数量", example = "10")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceShadowHistoryRespVO>> getShadowHistory(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return success(deviceShadowService.getShadowHistory(deviceId, pageNo, pageSize));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清除设备影子")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> clearDeviceShadow(@RequestParam("deviceId") Long deviceId) {
        deviceShadowService.clearDeviceShadow(deviceId);
        return success(true);
    }

}
