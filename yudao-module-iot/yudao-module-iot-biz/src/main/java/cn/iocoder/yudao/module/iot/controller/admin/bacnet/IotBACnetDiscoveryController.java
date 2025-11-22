package cn.iocoder.yudao.module.iot.controller.admin.bacnet;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryBindReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery.IotBACnetDiscoveryRecordRespVO;
import cn.iocoder.yudao.module.iot.convert.bacnet.IotBACnetDiscoveryConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.bacnet.IotBACnetDiscoveryRecordDO;
import cn.iocoder.yudao.module.iot.service.bacnet.IotBACnetDiscoveryService;
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
 * BACnet 设备发现 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - BACnet 设备发现")
@RestController
@RequestMapping("/iot/bacnet/discovery")
@Validated
public class IotBACnetDiscoveryController {

    @Resource
    private IotBACnetDiscoveryService bacnetDiscoveryService;

    @PostMapping("/discover-now")
    @Operation(summary = "立即执行设备发现")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:execute')")
    public CommonResult<List<IotBACnetDiscoveryRecordRespVO>> discoverDevicesNow() {
        List<IotBACnetDiscoveryRecordDO> records = bacnetDiscoveryService.discoverDevicesNow();
        return success(IotBACnetDiscoveryConvert.INSTANCE.convertList(records));
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定 BACnet 设备")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:bind')")
    public CommonResult<Long> bindDevice(@Valid @RequestBody IotBACnetDiscoveryBindReqVO bindReqVO) {
        return success(bacnetDiscoveryService.bindDevice(bindReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除发现记录")
    @Parameter(name = "id", description = "记录编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:delete')")
    public CommonResult<Boolean> deleteDiscoveryRecord(@RequestParam("id") Long id) {
        bacnetDiscoveryService.deleteDiscoveryRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得发现记录")
    @Parameter(name = "id", description = "记录编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:query')")
    public CommonResult<IotBACnetDiscoveryRecordRespVO> getDiscoveryRecord(@RequestParam("id") Long id) {
        IotBACnetDiscoveryRecordDO record = bacnetDiscoveryService.getDiscoveryRecord(id);
        return success(IotBACnetDiscoveryConvert.INSTANCE.convert(record));
    }

    @GetMapping("/page")
    @Operation(summary = "获得发现记录分页")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:query')")
    public CommonResult<PageResult<IotBACnetDiscoveryRecordRespVO>> getDiscoveryRecordPage(@Valid IotBACnetDiscoveryRecordPageReqVO pageReqVO) {
        PageResult<IotBACnetDiscoveryRecordDO> pageResult = bacnetDiscoveryService.getDiscoveryRecordPage(pageReqVO);
        return success(IotBACnetDiscoveryConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/unbound-list")
    @Operation(summary = "获得未绑定的设备列表")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:query')")
    public CommonResult<List<IotBACnetDiscoveryRecordRespVO>> getUnboundDevices() {
        List<IotBACnetDiscoveryRecordDO> records = bacnetDiscoveryService.getUnboundDevices();
        return success(IotBACnetDiscoveryConvert.INSTANCE.convertList(records));
    }

    @GetMapping("/bound-list")
    @Operation(summary = "获得已绑定的设备列表")
    @PreAuthorize("@ss.hasPermission('iot:bacnet:discovery:query')")
    public CommonResult<List<IotBACnetDiscoveryRecordRespVO>> getBoundDevices() {
        List<IotBACnetDiscoveryRecordDO> records = bacnetDiscoveryService.getBoundDevices();
        return success(IotBACnetDiscoveryConvert.INSTANCE.convertList(records));
    }

}
