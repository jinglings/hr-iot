package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理后台 - IoT TDengine 备份记录创建 Request VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 备份记录创建 Request VO")
@Data
public class IotBackupRecordCreateReqVO {

    @Schema(description = "备份名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "backup_20250121_001")
    @NotBlank(message = "备份名称不能为空")
    private String backupName;

    @Schema(description = "备份类型：1-全量备份，2-数据库备份，3-表备份", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "备份类型不能为空")
    @InEnum(value = BackupTypeEnum.class, message = "备份类型必须是 {value}")
    private Integer backupType;

    @Schema(description = "备份范围（JSON格式）：数据库列表或表列表", example = "[\"ruoyi_vue_pro\"]")
    private String backupScope;

    @Schema(description = "备份模式：1-手动，2-自动定时", example = "1")
    @InEnum(value = BackupModeEnum.class, message = "备份模式必须是 {value}")
    private Integer backupMode;

    @Schema(description = "备份备注", example = "手动全量备份")
    private String remark;

}
