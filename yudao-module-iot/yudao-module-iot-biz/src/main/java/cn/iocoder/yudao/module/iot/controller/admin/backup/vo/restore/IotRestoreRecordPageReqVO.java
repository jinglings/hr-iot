package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.backup.RestoreStatusEnum;
import cn.iocoder.yudao.module.iot.enums.backup.RestoreTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - IoT TDengine 恢复记录分页 Request VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 恢复记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotRestoreRecordPageReqVO extends PageParam {

    @Schema(description = "备份记录ID", example = "123")
    private Long backupId;

    @Schema(description = "恢复类型：1-完整恢复，2-选择性恢复", example = "1")
    @InEnum(value = RestoreTypeEnum.class, message = "恢复类型必须是 {value}")
    private Integer restoreType;

    @Schema(description = "恢复状态：0-恢复中，1-成功，2-失败", example = "1")
    @InEnum(value = RestoreStatusEnum.class, message = "恢复状态必须是 {value}")
    private Integer restoreStatus;

    @Schema(description = "恢复开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

}
