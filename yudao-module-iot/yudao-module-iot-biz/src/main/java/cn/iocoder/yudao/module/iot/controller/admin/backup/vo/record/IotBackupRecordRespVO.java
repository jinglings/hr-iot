package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - IoT TDengine 备份记录 Response VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 备份记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotBackupRecordRespVO {

    @Schema(description = "备份ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("备份ID")
    private Long id;

    @Schema(description = "备份名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "backup_20250121_001")
    @ExcelProperty("备份名称")
    private String backupName;

    @Schema(description = "备份类型：1-全量备份，2-数据库备份，3-表备份", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("备份类型")
    private Integer backupType;

    @Schema(description = "备份范围（JSON格式）", example = "[\"ruoyi_vue_pro\"]")
    private String backupScope;

    @Schema(description = "备份模式：1-手动，2-自动定时", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("备份模式")
    private Integer backupMode;

    @Schema(description = "备份状态：0-备份中，1-成功，2-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("备份状态")
    private Integer backupStatus;

    @Schema(description = "备份文件路径", example = "/data/iot-backup/backups/20250121/backup_001.zip")
    private String filePath;

    @Schema(description = "备份文件大小（字节）", example = "2684354560")
    @ExcelProperty("文件大小")
    private Long fileSize;

    @Schema(description = "文件格式", example = "zip")
    private String fileFormat;

    @Schema(description = "备份开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "备份结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "备份耗时（秒）", example = "930")
    @ExcelProperty("耗时(秒)")
    private Integer duration;

    @Schema(description = "备份数据条数", example = "1000000")
    @ExcelProperty("数据条数")
    private Long recordCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "备份备注", example = "手动全量备份")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
