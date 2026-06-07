package com.example.shopping_back.chat.controller;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.chat.service.ChatMessageService;
import com.example.shopping_back.chat.service.ConversationService;
import com.example.shopping_back.chat.model.Conversation;
import com.example.shopping_back.chat.dto.ChatMessageDto;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class ChatWebSocketController {

    private final AuthService authService;
    private final ConversationService conversationService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(AuthService authService,
                                   ConversationService conversationService,
                                   ChatMessageService chatMessageService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.authService = authService;
        this.conversationService = conversationService;
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    //即时消息处理websocket
    @MessageMapping("/chat/{covId}")
    public void handleChatMessage(
            @DestinationVariable("covId") Integer covId,
            @Payload ChatMessageDto chatMessageDto,
            @Header(name = "Authorization", required = false) String authorization) {
        AuthUserView currentUser = authService.me(bearerToken(authorization));
        chatMessageDto.setSenderId(currentUser.getUserId());
        chatMessageDto.setCovId(covId);
        Conversation conversation = conversationService.getConversation(covId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        authorizeConversationAccess(currentUser, conversation);
        chatMessageDto.setSenderId(currentUser.getUserId());
        chatMessageDto.setType("CHAT_MESSAGE");
        chatMessageService.sendMessageAndBroadcast(chatMessageDto);
        conversationService.updateLastActiveTime(covId);
    }

    @MessageMapping("/chat/read")
    public void handleReadReceipt(@Payload Map<String, Object> payload, Principal principal) {
        Integer covId = (Integer) payload.get("covId");
        Integer readerId = (Integer) payload.get("readerId");
        Integer targetUserId = conversationService.getOtherParticipantId(covId, readerId);
        System.out.println("【调试】准备推送给用户ID: " + targetUserId);
        System.out.println("【调试】推送目标路径: /queue/chat/read-status");
        conversationService.markAllAsRead(covId, readerId);
        messagingTemplate.convertAndSendToUser(
            targetUserId.toString(), 
            "/queue/chat/read-status", 
            Map.of("type", "STATUS_UPDATE", "covId", covId)
        );
    }

    private static void authorizeConversationAccess(AuthUserView currentUser, Conversation conversation) {
        Integer userId = currentUser.getUserId();
        if (!userId.equals(conversation.getBuyerId()) && !userId.equals(conversation.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden to access this conversation");
        }
    }

    private static String bearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String trimmed = authorization.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }

}
