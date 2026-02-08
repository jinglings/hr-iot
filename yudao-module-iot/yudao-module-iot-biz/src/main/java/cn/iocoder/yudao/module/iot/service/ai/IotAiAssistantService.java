package cn.iocoder.yudao.module.iot.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * IoT AI 助手 Service
 */
public interface IotAiAssistantService {

    /**
     * 发送聊天消息 (流式)
     *
     * @param userId         用户ID
     * @param conversationId 对话ID (null则创建新对话)
     * @param content        用户消息内容
     * @param emitter        SSE emitter
     */
    void chat(Long userId, Long conversationId, String content, SseEmitter emitter);

}
