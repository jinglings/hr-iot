package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagDO;
import cn.iocoder.yudao.module.iot.service.device.tag.IotDeviceTagService;
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
 * 管理后台 - IoT 设备标签
 *
 * @author AI
 */
@Tag(name = "管理后台 - IoT 设备标签")
@RestController
@RequestMapping("/iot/device-tag")
@Validated
public class IotDeviceTagController {

    @Resource
    private IotDeviceTagService tagService;

    @PostMapping("/create")
    @Operation(summary = "创建设备标签")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody IotDeviceTagCreateReqVO createReqVO) {
        return success(tagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备标签")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody IotDeviceTagUpdateReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备标签")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:query')")
    public CommonResult<IotDeviceTagRespVO> getTag(@RequestParam("id") Long id) {
        IotDeviceTagDO tag = tagService.getTag(id);
        return success(BeanUtils.toBean(tag, IotDeviceTagRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备标签分页")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:query')")
    public CommonResult<PageResult<IotDeviceTagRespVO>> getTagPage(@Valid IotDeviceTagPageReqVO pageReqVO) {
        PageResult<IotDeviceTagDO> pageResult = tagService.getTagPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceTagRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备标签列表")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:query')")
    public CommonResult<List<IotDeviceTagRespVO>> getTagList() {
        List<IotDeviceTagDO> list = tagService.getTagList();
        return success(BeanUtils.toBean(list, IotDeviceTagRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得设备标签简单列表")
    @PreAuthorize("@ss.hasPermission('iot:device-tag:query')")
    public CommonResult<List<IotDeviceTagRespVO>> getTagSimpleList() {
        List<IotDeviceTagDO> list = tagService.getTagList();
        return success(BeanUtils.toBean(list, IotDeviceTagRespVO.class));
    }

    @PostMapping("/bind")
    @Operation(summary = "为设备绑定标签")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> bindTagsToDevice(@Valid @RequestBody IotDeviceTagBindReqVO bindReqVO) {
        tagService.bindTagsToDevice(bindReqVO);
        return success(true);
    }

    @PostMapping("/unbind")
    @Operation(summary = "为设备解绑标签")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> unbindTagsFromDevice(@Valid @RequestBody IotDeviceTagBindReqVO bindReqVO) {
        tagService.unbindTagsFromDevice(bindReqVO);
        return success(true);
    }

    @PostMapping("/batch-bind")
    @Operation(summary = "批量为设备绑定标签")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> batchBindTags(@Valid @RequestBody IotDeviceTagBatchBindReqVO batchBindReqVO) {
        tagService.batchBindTags(batchBindReqVO);
        return success(true);
    }

    @PostMapping("/batch-unbind")
    @Operation(summary = "批量解绑设备标签")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> batchUnbindTags(@Valid @RequestBody IotDeviceTagBatchBindReqVO batchBindReqVO) {
        tagService.batchUnbindTags(batchBindReqVO);
        return success(true);
    }

    @GetMapping("/by-device")
    @Operation(summary = "获取设备的所有标签")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotDeviceTagRespVO>> getTagsByDevice(@RequestParam("deviceId") Long deviceId) {
        List<IotDeviceTagDO> list = tagService.getTagsByDeviceId(deviceId);
        return success(BeanUtils.toBean(list, IotDeviceTagRespVO.class));
    }

}
