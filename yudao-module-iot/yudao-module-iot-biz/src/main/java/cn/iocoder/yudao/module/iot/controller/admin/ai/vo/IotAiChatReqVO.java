package cn.iocoder.yudao.module.iot.controller.admin.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - IoT AI 聊天请求")
@Data
public class IotAiChatReqVO {

    @Schema(description = "对话编号 (为空则创建新对话)")
    private Long conversationId;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空")
    private String content;

}
