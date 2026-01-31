package cn.iocoder.yudao.module.iot.controller.admin.backup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordRespVO;
import cn.iocoder.yudao.module.iot.service.backup.IotRestoreRecordService;
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
 * 管理后台 - IoT TDengine 恢复记录 Controller
 *
 * @author claude
 */
@Tag(name = "管理后台 - IoT TDengine 恢复记录")
@RestController
@RequestMapping("/iot/restore/record")
@Validated
public class IotRestoreRecordController {

    @Resource
    private IotRestoreRecordService restoreRecordService;

    @PostMapping("/execute")
    @Operation(summary = "执行恢复")
    @PreAuthorize("@ss.hasPermission('iot:restore:execute')")
    public CommonResult<Long> executeRestore(@Valid @RequestBody IotRestoreRecordCreateReqVO createReqVO) {
        return success(restoreRecordService.executeRestore(createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得恢复记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:restore:query')")
    public CommonResult<IotRestoreRecordRespVO> getRestoreRecord(@RequestParam("id") Long id) {
        IotRestoreRecordRespVO restore = restoreRecordService.getRestoreRecordPage(
                new IotRestoreRecordPageReqVO()).getList().stream()
                .filter(vo -> vo.getId().equals(id))
                .findFirst()
                .orElse(null);
        return success(restore);
    }

    @GetMapping("/page")
    @Operation(summary = "获得恢复记录分页")
    @PreAuthorize("@ss.hasPermission('iot:restore:query')")
    public CommonResult<PageResult<IotRestoreRecordRespVO>> getRestoreRecordPage(@Valid IotRestoreRecordPageReqVO pageReqVO) {
        PageResult<IotRestoreRecordRespVO> pageResult = restoreRecordService.getRestoreRecordPage(pageReqVO);
        return success(pageResult);
    }

}
