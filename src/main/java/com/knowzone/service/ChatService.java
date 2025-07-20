package com.knowzone.service;

import com.knowzone.dto.ChatMessageDTO;

import java.util.List;

public interface ChatService {
    ChatMessageDTO processMessage(ChatMessageDTO message);
    void sendPrivateMessage(Long userId, ChatMessageDTO message);
    void sendMatchNotification(Long user1Id, Long user2Id, String matchMessage);
    List<ChatMessageDTO> getChatHistory(Long user1Id, Long user2Id);
} 