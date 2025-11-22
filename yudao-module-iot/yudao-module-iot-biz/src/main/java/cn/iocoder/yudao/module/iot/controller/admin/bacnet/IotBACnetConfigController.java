package cn.iocoder.yudao.module.iot.controller.admin.bacnet;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config.IotBACnetDeviceConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping.IotBACnetPropertyMappingSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetConfigConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDeviceConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetPropertyMappingDO;
import cn.iocoder.yudao.module.iot.service.bacnet.IotBACnetConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * BACnet 配置 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - BACnet 配置")
@RestController
@RequestMapping("/iot/bacnet/config")
@Validated
public class IotBACnetConfigController {

    @Resource
    private IotBACnetConfigService bacnetConfigService;

    // ========== 设备配置管理 ==========

    @PostMapping("/device/create")
    @Operation(summary = "创建 BACnet 设备配置")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:create')")
    public CommonResult<Long> createDeviceConfig(@Valid @RequestBody IotBACnetDeviceConfigSaveReqVO createReqVO) {
        return success(bacnetConfigService.createDeviceConfig(createReqVO));
    }

    @PutMapping("/device/update")
    @Operation(summary = "更新 BACnet 设备配置")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:update')")
    public CommonResult<Boolean> updateDeviceConfig(@Valid @RequestBody IotBACnetDeviceConfigSaveReqVO updateReqVO) {
        bacnetConfigService.updateDeviceConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/device/delete")
    @Operation(summary = "删除 BACnet 设备配置")
    @Parameter(name = "id", description = "配置编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:delete')")
    public CommonResult<Boolean> deleteDeviceConfig(@RequestParam("id") Long id) {
        bacnetConfigService.deleteDeviceConfig(id);
        return success(true);
    }

    @GetMapping("/device/get")
    @Operation(summary = "获得 BACnet 设备配置")
    @Parameter(name = "id", description = "配置编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:query')")
    public CommonResult<IotBACnetDeviceConfigRespVO> getDeviceConfig(@RequestParam("id") Long id) {
        IotBACnetDeviceConfigDO config = bacnetConfigService.getDeviceConfig(id);
        return success(IotBACnetConfigConvert.INSTANCE.convert(config));
    }

    @GetMapping("/device/get-by-device")
    @Operation(summary = "根据设备编号获得 BACnet 设备配置")
    @Parameter(name = "deviceId", description = "设备编号", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:query')")
    public CommonResult<IotBACnetDeviceConfigRespVO> getDeviceConfigByDeviceId(@RequestParam("deviceId") Long deviceId) {
        IotBACnetDeviceConfigDO config = bacnetConfigService.getDeviceConfigByDeviceId(deviceId);
        return success(IotBACnetConfigConvert.INSTANCE.convert(config));
    }

    @GetMapping("/device/page")
    @Operation(summary = "获得 BACnet 设备配置分页")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:config:query')")
    public CommonResult<PageResult<IotBACnetDeviceConfigRespVO>> getDeviceConfigPage(@Valid IotBACnetDeviceConfigPageReqVO pageReqVO) {
        PageResult<IotBACnetDeviceConfigDO> pageResult = bacnetConfigService.getDeviceConfigPage(pageReqVO);
        return success(IotBACnetConfigConvert.INSTANCE.convertPage(pageResult));
    }

    // ========== 属性映射管理 ==========

    @PostMapping("/mapping/create")
    @Operation(summary = "创建 BACnet 属性映射配置")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:create')")
    public CommonResult<Long> createPropertyMapping(@Valid @RequestBody IotBACnetPropertyMappingSaveReqVO createReqVO) {
        return success(bacnetConfigService.createPropertyMapping(createReqVO));
    }

    @PutMapping("/mapping/update")
    @Operation(summary = "更新 BACnet 属性映射配置")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:update')")
    public CommonResult<Boolean> updatePropertyMapping(@Valid @RequestBody IotBACnetPropertyMappingSaveReqVO updateReqVO) {
        bacnetConfigService.updatePropertyMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/mapping/delete")
    @Operation(summary = "删除 BACnet 属性映射配置")
    @Parameter(name = "id", description = "映射编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:delete')")
    public CommonResult<Boolean> deletePropertyMapping(@RequestParam("id") Long id) {
        bacnetConfigService.deletePropertyMapping(id);
        return success(true);
    }

    @GetMapping("/mapping/get")
    @Operation(summary = "获得 BACnet 属性映射配置")
    @Parameter(name = "id", description = "映射编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:query')")
    public CommonResult<IotBACnetPropertyMappingRespVO> getPropertyMapping(@RequestParam("id") Long id) {
        IotBACnetPropertyMappingDO mapping = bacnetConfigService.getPropertyMapping(id);
        return success(IotBACnetConfigConvert.INSTANCE.convert(mapping));
    }

    @GetMapping("/mapping/page")
    @Operation(summary = "获得 BACnet 属性映射配置分页")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:query')")
    public CommonResult<PageResult<IotBACnetPropertyMappingRespVO>> getPropertyMappingPage(@Valid IotBACnetPropertyMappingPageReqVO pageReqVO) {
        PageResult<IotBACnetPropertyMappingDO> pageResult = bacnetConfigService.getPropertyMappingPage(pageReqVO);
        return success(IotBACnetConfigConvert.INSTANCE.convertMappingPage(pageResult));
    }

    @GetMapping("/mapping/list-by-device")
    @Operation(summary = "获得设备的所有属性映射配置")
    @Parameter(name = "deviceId", description = "设备编号", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:mapping:query')")
    public CommonResult<List<IotBACnetPropertyMappingRespVO>> getPropertyMappingsByDeviceId(@RequestParam("deviceId") Long deviceId) {
        List<IotBACnetPropertyMappingDO> mappings = bacnetConfigService.getPropertyMappingsByDeviceId(deviceId);
        return success(IotBACnetConfigConvert.INSTANCE.convertMappingList(mappings));
    }

}
