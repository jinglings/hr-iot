package cn.iocoder.yudao.module.iot.dal.dataobject.ai;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT AI 消息 DO
 */
@TableName(value = "iot_ai_message", autoResultMap = true)
@KeySequence("iot_ai_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAiMessageDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 对话编号
     */
    private Long conversationId;

    /**
     * 角色: user/assistant/system/tool
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 工具调用信息 (JSON)
     */
    private String toolCalls;

    /**
     * 工具调用ID
     */
    private String toolCallId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 消耗Token数
     */
    private Integer tokens;

}
