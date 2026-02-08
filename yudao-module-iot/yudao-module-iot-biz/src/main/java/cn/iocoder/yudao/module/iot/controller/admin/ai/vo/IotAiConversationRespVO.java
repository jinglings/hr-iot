package cn.iocoder.yudao.module.iot.controller.admin.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT AI 对话")
@Data
public class IotAiConversationRespVO {

    @Schema(description = "对话编号")
    private Long id;

    @Schema(description = "对话标题")
    private String title;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
