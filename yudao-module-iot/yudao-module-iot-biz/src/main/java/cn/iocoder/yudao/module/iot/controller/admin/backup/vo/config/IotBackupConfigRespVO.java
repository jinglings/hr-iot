package cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - IoT TDengine 备份配置 Response VO
 *
 * @author claude
 */
@Schema(description = "管理后台 - IoT TDengine 备份配置 Response VO")
@Data
public class IotBackupConfigRespVO {

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日全量备份")
    private String configName;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean enabled;

    @Schema(description = "备份类型：1-全量备份，2-数据库备份，3-表备份", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer backupType;

    @Schema(description = "备份范围（JSON格式）", example = "[\"ruoyi_vue_pro\"]")
    private String backupScope;

    @Schema(description = "Cron表达式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 0 2 * * ?")
    private String cronExpression;

    @Schema(description = "保留天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer retentionDays;

    @Schema(description = "最大备份数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer maxBackupCount;

    @Schema(description = "是否启用压缩", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean compressionEnabled;

    @Schema(description = "成功时是否通知", example = "false")
    private Boolean notifyOnSuccess;

    @Schema(description = "失败时是否通知", example = "true")
    private Boolean notifyOnFailure;

    @Schema(description = "通知邮箱列表（逗号分隔）", example = "admin@example.com")
    private String notifyEmails;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
