package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - IoT TDengine 恢复记录 Response VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 恢复记录 Response VO")
@Data
//@ExcelIgnoreUnannotated
public class IotRestoreRecordRespVO {

    @Schema(description = "恢复ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
//    @ExcelProperty("恢复ID")
    private Long id;

    @Schema(description = "备份记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
//    @ExcelProperty("备份记录ID")
    private Long backupId;

    @Schema(description = "备份名称", example = "backup_20250121_001")
    private String backupName;

    @Schema(description = "恢复类型：1-完整恢复，2-选择性恢复", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
//    @ExcelProperty("恢复类型")
    private Integer restoreType;

    @Schema(description = "恢复目标（JSON格式）", example = "[\"device_message\"]")
    private String restoreTarget;

    @Schema(description = "恢复状态：0-恢复中，1-成功，2-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
//    @ExcelProperty("恢复状态")
    private Integer restoreStatus;

    @Schema(description = "恢复开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
//    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "恢复结束时间")
//    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "恢复耗时（秒）", example = "1200")
//    @ExcelProperty("耗时(秒)")
    private Integer duration;

    @Schema(description = "恢复数据条数", example = "1000000")
//    @ExcelProperty("数据条数")
    private Long recordCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "恢复备注", example = "恢复到备份点")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
//    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
