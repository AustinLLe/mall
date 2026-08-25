package com.example.shopping_back.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.chat.mapper.ChatMessageMapper;
import com.example.shopping_back.chat.mapper.ConversationMapper;
import com.example.shopping_back.chat.model.Conversation;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ConversationServiceTest {

    private ConversationMapper conversationMapper;
    private ChatMessageMapper chatMessageMapper;
    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationMapper = mock(ConversationMapper.class);
        chatMessageMapper = mock(ChatMessageMapper.class);
        conversationService = new ConversationService(
                conversationMapper,
                mock(ShopProductMapper.class),
                mock(AuthService.class),
                mock(ChatMessageService.class),
                chatMessageMapper);
    }

    @Test
    void deleteConversationRemovesMessagesThenConversation() {
        Conversation conversation = new Conversation();
        conversation.setCovId(12);
        when(conversationMapper.selectById(12)).thenReturn(conversation);

        conversationService.deleteConversation(12);

        verify(chatMessageMapper).deleteByCovId(12);
        verify(conversationMapper).deleteById(12);
    }

    @Test
    void deleteConversationRejectsMissingConversation() {
        when(conversationMapper.selectById(99)).thenReturn(null);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> conversationService.deleteConversation(99));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(chatMessageMapper, never()).deleteByCovId(99);
        verify(conversationMapper, never()).deleteById(99);
    }
}
