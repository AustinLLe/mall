package com.example.shopping_back.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping_back.chat.dto.ChatMessageDto;
import com.example.shopping_back.chat.mapper.ChatMessageMapper;
import com.example.shopping_back.chat.model.ChatMessage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class ChatMessageServiceTest {

    private ChatMessageMapper chatMessageMapper;
    private SimpMessagingTemplate messagingTemplate;
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        chatMessageMapper = mock(ChatMessageMapper.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        chatMessageService = new ChatMessageService(chatMessageMapper);
        ReflectionTestUtils.setField(chatMessageService, "messagingTemplate", messagingTemplate);
    }

    @Test
    void createMessageUsesDefaultTypeAndMarksUnread() {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setCovId(12);
        dto.setSenderId(7);
        dto.setContent("你好");
        dto.setPriceValue(new BigDecimal("99.9"));
        dto.setType(" ");

        ChatMessage result = chatMessageService.createMessage(dto);

        assertNotNull(result);
        assertEquals(12, result.getCovId());
        assertEquals(7, result.getSenderId());
        assertEquals("你好", result.getContent());
        assertEquals("CHAT_MESSAGE", result.getType());
        assertFalse(result.getIsRead());
        verify(chatMessageMapper).insert(any(ChatMessage.class));
    }

    @Test
    void sendMessageAndBroadcastPublishesToChatTopic() {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setCovId(12);
        dto.setSenderId(7);
        dto.setContent("我想买这个");
        dto.setType("CHAT_MESSAGE");
        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

        ChatMessage result = chatMessageService.sendMessageAndBroadcast(dto);

        assertNotNull(result);
        assertEquals("我想买这个", result.getContent());
        verify(messagingTemplate).convertAndSend("/topic/chat/12", result);
    }
}
