package cn.iocoder.yudao.module.iot.service.ai;

import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiConversationDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiMessageDO;

import java.util.List;

/**
 * IoT AI 对话 Service
 */
public interface IotAiConversationService {

    /**
     * 创建对话
     */
    Long createConversation(Long userId, String title, String model);

    /**
     * 获取用户的对话列表
     */
    List<IotAiConversationDO> getConversationList(Long userId);

    /**
     * 删除对话
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 获取对话
     */
    IotAiConversationDO getConversation(Long conversationId);

    /**
     * 更新对话标题
     */
    void updateConversationTitle(Long conversationId, String title);

    /**
     * 增加消息计数
     */
    void incrementMessageCount(Long conversationId, int delta);

    /**
     * 保存消息
     */
    void saveMessage(IotAiMessageDO message);

    /**
     * 获取对话的消息列表
     */
    List<IotAiMessageDO> getMessageList(Long conversationId);

}
