package com.example.shopping_back.chat.service;

import com.example.shopping_back.chat.mapper.ChatMessageMapper;
import com.example.shopping_back.chat.model.ChatMessage;
import com.example.shopping_back.chat.dto.ChatMessageDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatMessageService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
        ensureSchema();
    }

    public ChatMessage createMessage(ChatMessageDto request) {
        if (request.getSenderId() == null) {
            throw new IllegalArgumentException("senderId must not be null when creating a chat message");
        }
        ChatMessage msg = new ChatMessage();
        msg.setCovId(request.getCovId());
        msg.setContent(request.getContent());
        msg.setSenderId(request.getSenderId());
        msg.setPriceValue(request.getPriceValue());
        msg.setCreateTime(new Date());
        msg.setIsRead(false);
        msg.setType(request.getType() == null || request.getType().isBlank() ? "CHAT_MESSAGE" : request.getType());
        chatMessageMapper.insert(msg);
        return msg;
    }

    private void ensureSchema() {
        chatMessageMapper.createChatMessageTable();
        if (chatMessageMapper.countChatMessageColumn("type") == 0) {
            chatMessageMapper.addTypeColumn();
        }
    }

    public ChatMessage sendMessageAndBroadcast(ChatMessageDto dto) {
        ChatMessage msg = createMessage(dto);
        messagingTemplate.convertAndSend("/topic/chat/" + dto.getCovId(), msg);
        return msg;
    }

    public List<ChatMessage> getMessages(Integer covId) {
        return chatMessageMapper.selectByCovId(covId);
    }

    public List<ChatMessage> getRecentMessages(Integer covId, int limit) {
        List<ChatMessage> messages = chatMessageMapper.findRecentByCovId(covId, limit);
        Collections.reverse(messages);
        return messages;
    }

    public ChatMessage getLastMessageByCovId(Integer covId) {
        return chatMessageMapper.findLastByCovId(covId);
    }

    public int countUnread(Integer covId, Integer userId) {
        return chatMessageMapper.countUnreadByCovId(covId, userId);
    }

}
