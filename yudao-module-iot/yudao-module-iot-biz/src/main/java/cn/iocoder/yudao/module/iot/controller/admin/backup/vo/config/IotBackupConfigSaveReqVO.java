package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 管理后台 - IoT TDengine 备份配置新增/修改 Request VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 备份配置新增/修改 Request VO")
@Data
public class IotBackupConfigSaveReqVO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日全量备份")
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "备份类型：1-全量备份，2-数据库备份，3-表备份", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "备份类型不能为空")
    @InEnum(value = BackupTypeEnum.class, message = "备份类型必须是 {value}")
    private Integer backupType;

    @Schema(description = "备份范围（JSON格式）", example = "[\"ruoyi_vue_pro\"]")
    private String backupScope;

    @Schema(description = "Cron表达式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 0 2 * * ?")
    @NotBlank(message = "Cron表达式不能为空")
    @Size(max = 100, message = "Cron表达式长度不能超过100个字符")
    private String cronExpression;

    @Schema(description = "保留天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "保留天数不能为空")
    @Min(value = 1, message = "保留天数至少为1天")
    @Max(value = 365, message = "保留天数不能超过365天")
    private Integer retentionDays;

    @Schema(description = "最大备份数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "最大备份数量不能为空")
    @Min(value = 1, message = "最大备份数量至少为1个")
    @Max(value = 100, message = "最大备份数量不能超过100个")
    private Integer maxBackupCount;

    @Schema(description = "是否启用压缩", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用压缩不能为空")
    private Boolean compressionEnabled;

    @Schema(description = "成功时是否通知", example = "false")
    private Boolean notifyOnSuccess;

    @Schema(description = "失败时是否通知", example = "true")
    private Boolean notifyOnFailure;

    @Schema(description = "通知邮箱列表（逗号分隔）", example = "admin@example.com,dev@example.com")
    @Size(max = 500, message = "通知邮箱列表长度不能超过500个字符")
    private String notifyEmails;

}
