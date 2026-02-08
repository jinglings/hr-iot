package cn.iocoder.yudao.module.iot.controller.admin.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT AI 消息")
@Data
public class IotAiMessageRespVO {

    @Schema(description = "消息编号")
    private Long id;

    @Schema(description = "对话编号")
    private Long conversationId;

    @Schema(description = "角色: user/assistant")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
