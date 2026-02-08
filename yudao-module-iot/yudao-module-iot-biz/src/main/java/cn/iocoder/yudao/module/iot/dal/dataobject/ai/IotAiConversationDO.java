package cn.iocoder.yudao.module.iot.dal.dataobject.ai;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT AI 对话 DO
 */
@TableName("iot_ai_conversation")
@KeySequence("iot_ai_conversation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAiConversationDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 模型标识
     */
    private String model;

    /**
     * 消息数量
     */
    private Integer messageCount;

}
