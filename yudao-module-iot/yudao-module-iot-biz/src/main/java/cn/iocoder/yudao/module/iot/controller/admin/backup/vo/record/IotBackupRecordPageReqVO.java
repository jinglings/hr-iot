package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupStatusEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - IoT TDengine 备份记录分页 Request VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 备份记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotBackupRecordPageReqVO extends PageParam {

    @Schema(description = "备份名称", example = "backup_20250121")
    private String backupName;

    @Schema(description = "备份类型：1-全量备份，2-数据库备份，3-表备份", example = "1")
    @InEnum(value = BackupTypeEnum.class, message = "备份类型必须是 {value}")
    private Integer backupType;

    @Schema(description = "备份模式：1-手动，2-自动定时", example = "1")
    @InEnum(value = BackupModeEnum.class, message = "备份模式必须是 {value}")
    private Integer backupMode;

    @Schema(description = "备份状态：0-备份中，1-成功，2-失败", example = "1")
    @InEnum(value = BackupStatusEnum.class, message = "备份状态必须是 {value}")
    private Integer backupStatus;

    @Schema(description = "备份开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

}
