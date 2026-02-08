package cn.iocoder.yudao.module.iot.service.ai;

import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiConversationDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiMessageDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ai.IotAiConversationMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ai.IotAiMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class IotAiConversationServiceImpl implements IotAiConversationService {

    private final IotAiConversationMapper conversationMapper;
    private final IotAiMessageMapper messageMapper;

    @Override
    public Long createConversation(Long userId, String title, String model) {
        IotAiConversationDO conversation = IotAiConversationDO.builder()
                .userId(userId)
                .title(title)
                .model(model)
                .messageCount(0)
                .build();
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public List<IotAiConversationDO> getConversationList(Long userId) {
        return conversationMapper.selectListByUserId(userId);
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        IotAiConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new RuntimeException("对话不存在或无权限");
        }
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public IotAiConversationDO getConversation(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    @Override
    public void updateConversationTitle(Long conversationId, String title) {
        IotAiConversationDO update = new IotAiConversationDO();
        update.setId(conversationId);
        update.setTitle(title);
        conversationMapper.updateById(update);
    }

    @Override
    public void incrementMessageCount(Long conversationId, int delta) {
        IotAiConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            IotAiConversationDO update = new IotAiConversationDO();
            update.setId(conversationId);
            update.setMessageCount(conversation.getMessageCount() + delta);
            conversationMapper.updateById(update);
        }
    }

    @Override
    public void saveMessage(IotAiMessageDO message) {
        messageMapper.insert(message);
    }

    @Override
    public List<IotAiMessageDO> getMessageList(Long conversationId) {
        return messageMapper.selectListByConversationId(conversationId);
    }

}
