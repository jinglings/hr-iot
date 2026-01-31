package cn.iocoder.yudao.module.iot.controller.admin.backup;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordRespVO;
import cn.iocoder.yudao.module.iot.service.backup.IotBackupExecutor;
import cn.iocoder.yudao.module.iot.service.backup.IotBackupRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.nio.file.Files;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT TDengine 备份记录 Controller
 *
 * @author claude
 */
@Tag(name = "管理后台 - IoT TDengine 备份记录")
@RestController
@RequestMapping("/iot/backup/record")
@Validated
public class IotBackupRecordController {

    @Resource
    private IotBackupRecordService backupRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建备份（手动备份）")
    @PreAuthorize("@ss.hasPermission('iot:backup:create')")
    public CommonResult<Long> createBackup(@Valid @RequestBody IotBackupRecordCreateReqVO createReqVO) {
        return success(backupRecordService.createBackup(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除备份")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:backup:delete')")
    public CommonResult<Boolean> deleteBackup(@RequestParam("id") Long id) {
        backupRecordService.deleteBackup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得备份记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:backup:query')")
    public CommonResult<IotBackupRecordRespVO> getBackupRecord(@RequestParam("id") Long id) {
        IotBackupRecordRespVO backup = backupRecordService.getBackupRecordPage(
                new IotBackupRecordPageReqVO()).getList().stream()
                .filter(vo -> vo.getId().equals(id))
                .findFirst()
                .orElse(null);
        return success(backup);
    }

    @GetMapping("/page")
    @Operation(summary = "获得备份记录分页")
    @PreAuthorize("@ss.hasPermission('iot:backup:query')")
    public CommonResult<PageResult<IotBackupRecordRespVO>> getBackupRecordPage(@Valid IotBackupRecordPageReqVO pageReqVO) {
        PageResult<IotBackupRecordRespVO> pageResult = backupRecordService.getBackupRecordPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/download")
    @Operation(summary = "下载备份文件")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:backup:download')")
    @ApiAccessLog(operateType = EXPORT)
    public ResponseEntity<byte[]> downloadBackup(@RequestParam("id") Long id) throws Exception {
        String filePath = backupRecordService.downloadBackup(id);
        File file = new File(filePath);

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(fileBytes);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证备份文件")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:backup:query')")
    public CommonResult<IotBackupExecutor.BackupMetadata> validateBackup(@RequestParam("id") Long id) {
        IotBackupExecutor.BackupMetadata metadata = backupRecordService.validateBackup(id);
        return success(metadata);
    }

}
