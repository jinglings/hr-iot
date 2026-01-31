package cn.iocoder.yudao.module.iot.controller.admin.backup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.service.backup.IotBackupConfigService;
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
 * 管理后台 - IoT TDengine 备份配置 Controller
 *
 * @author claude
 */
@Tag(name = "管理后台 - IoT TDengine 备份配置")
@RestController
@RequestMapping("/iot/backup/config")
@Validated
public class IotBackupConfigController {

    @Resource
    private IotBackupConfigService backupConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建备份配置")
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<Long> createBackupConfig(@Valid @RequestBody IotBackupConfigSaveReqVO createReqVO) {
        return success(backupConfigService.createBackupConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新备份配置")
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<Boolean> updateBackupConfig(@Valid @RequestBody IotBackupConfigSaveReqVO updateReqVO) {
        backupConfigService.updateBackupConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除备份配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<Boolean> deleteBackupConfig(@RequestParam("id") Long id) {
        backupConfigService.deleteBackupConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得备份配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<IotBackupConfigRespVO> getBackupConfig(@RequestParam("id") Long id) {
        IotBackupConfigRespVO config = backupConfigService.getBackupConfigList().stream()
                .filter(vo -> vo.getId().equals(id))
                .findFirst()
                .orElse(null);
        return success(config);
    }

    @GetMapping("/list")
    @Operation(summary = "获得备份配置列表")
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<List<IotBackupConfigRespVO>> getBackupConfigList() {
        List<IotBackupConfigRespVO> list = backupConfigService.getBackupConfigList();
        return success(list);
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "启用/禁用备份配置")
    @PreAuthorize("@ss.hasPermission('iot:backup:config')")
    public CommonResult<Boolean> updateBackupConfigEnabled(
            @RequestParam("id") Long id,
            @RequestParam("enabled") Boolean enabled) {
        backupConfigService.updateBackupConfigEnabled(id, enabled);
        return success(true);
    }

    @PostMapping("/execute")
    @Operation(summary = "立即执行备份配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:backup:create')")
    public CommonResult<Long> executeBackupConfig(@RequestParam("id") Long id) {
        Long backupId = backupConfigService.executeBackupConfig(id);
        return success(backupId);
    }

}
