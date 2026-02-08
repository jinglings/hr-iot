import request from '@/config/axios'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { getAccessToken } from '@/utils/auth'
import { config } from '@/config/axios/config'

// 对话 VO
export interface AiConversationVO {
  id: number
  title: string
  messageCount: number
  createTime: Date
}

// 消息 VO
export interface AiMessageVO {
  id: number
  conversationId: number
  role: 'user' | 'assistant'
  content: string
  createTime: Date
}

export const IotAiApi = {
  // 流式聊天
  chatStream: (
    conversationId: number | null,
    content: string,
    ctrl: AbortController,
    onMessage: (event: any) => void,
    onError: (err: any) => void,
    onClose: () => void
  ) => {
    const token = getAccessToken()
    return fetchEventSource(`${config.base_url}/iot/ai/chat/stream`, {
      method: 'post',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      openWhenHidden: true,
      body: JSON.stringify({ conversationId, content }),
      onmessage: onMessage,
      onerror: onError,
      onclose: onClose,
      signal: ctrl.signal
    })
  },

  // 获取对话列表
  getConversationList: async () => {
    return await request.get({ url: '/iot/ai/conversation/list' })
  },

  // 删除对话
  deleteConversation: async (id: number) => {
    return await request.delete({ url: `/iot/ai/conversation/delete?id=${id}` })
  },

  // 获取消息列表
  getMessageList: async (conversationId: number) => {
    return await request.get({ url: `/iot/ai/message/list?conversationId=${conversationId}` })
  }
}
