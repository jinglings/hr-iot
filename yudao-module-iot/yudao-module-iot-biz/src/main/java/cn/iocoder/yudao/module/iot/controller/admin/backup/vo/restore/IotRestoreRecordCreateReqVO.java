package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.backup.RestoreTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - IoT TDengine 恢复记录创建 Request VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 恢复记录创建 Request VO")
@Data
public class IotRestoreRecordCreateReqVO {

    @Schema(description = "备份记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "备份记录ID不能为空")
    private Long backupId;

    @Schema(description = "恢复类型：1-完整恢复，2-选择性恢复", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "恢复类型不能为空")
    @InEnum(value = RestoreTypeEnum.class, message = "恢复类型必须是 {value}")
    private Integer restoreType;

    @Schema(description = "恢复目标（JSON格式）：选择性恢复时指定要恢复的表", example = "[\"device_message\"]")
    private String restoreTarget;

    @Schema(description = "恢复备注", example = "恢复到备份点")
    private String remark;

}
