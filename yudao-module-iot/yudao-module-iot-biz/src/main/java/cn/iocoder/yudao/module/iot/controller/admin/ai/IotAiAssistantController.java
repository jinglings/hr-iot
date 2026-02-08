package cn.iocoder.yudao.module.iot.controller.admin.ai;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ai.vo.IotAiChatReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ai.vo.IotAiConversationRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ai.vo.IotAiMessageRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiConversationDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiMessageDO;
import cn.iocoder.yudao.module.iot.service.ai.IotAiAssistantService;
import cn.iocoder.yudao.module.iot.service.ai.IotAiConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT AI 助手")
@RestController
@RequestMapping("/iot/ai")
@Validated
@RequiredArgsConstructor
public class IotAiAssistantController {

    private final IotAiAssistantService aiAssistantService;
    private final IotAiConversationService conversationService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息（流式）")
    @PreAuthorize("@ss.hasPermission('iot:ai:chat')")
    public SseEmitter chatStream(@Valid @RequestBody IotAiChatReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(120_000L);
        aiAssistantService.chat(userId, reqVO.getConversationId(), reqVO.getContent(), emitter);
        return emitter;
    }

    @GetMapping("/conversation/list")
    @Operation(summary = "获取对话列表")
    @PreAuthorize("@ss.hasPermission('iot:ai:chat')")
    public CommonResult<List<IotAiConversationRespVO>> getConversationList() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<IotAiConversationDO> list = conversationService.getConversationList(userId);
        return success(list.stream().map(this::convertConversation).collect(Collectors.toList()));
    }

    @DeleteMapping("/conversation/delete")
    @Operation(summary = "删除对话")
    @PreAuthorize("@ss.hasPermission('iot:ai:chat')")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        conversationService.deleteConversation(userId, id);
        return success(true);
    }

    @GetMapping("/message/list")
    @Operation(summary = "获取消息列表")
    @PreAuthorize("@ss.hasPermission('iot:ai:chat')")
    public CommonResult<List<IotAiMessageRespVO>> getMessageList(
            @RequestParam("conversationId") Long conversationId) {
        List<IotAiMessageDO> list = conversationService.getMessageList(conversationId);
        return success(list.stream()
                .filter(m -> "user".equals(m.getRole()) || "assistant".equals(m.getRole()))
                .map(this::convertMessage)
                .collect(Collectors.toList()));
    }

    private IotAiConversationRespVO convertConversation(IotAiConversationDO conversation) {
        IotAiConversationRespVO vo = new IotAiConversationRespVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        vo.setMessageCount(conversation.getMessageCount());
        vo.setCreateTime(conversation.getCreateTime());
        return vo;
    }

    private IotAiMessageRespVO convertMessage(IotAiMessageDO message) {
        IotAiMessageRespVO vo = new IotAiMessageRespVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

}
