package com.knowzone.service.impl;

import com.knowzone.dto.ChatMessageDTO;
import com.knowzone.persistence.entity.ChatMessage;
import com.knowzone.enums.MessageType;
import com.knowzone.persistence.repository.ChatMessageRepository;
import com.knowzone.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessageDTO processMessage(ChatMessageDTO message) {
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(String.valueOf(LocalDateTime.now()));
        
        log.info("Processing message from user {} to user {}: {}", 
                message.getSenderId(), message.getReceiverId(), message.getContent());
        
        // Save message to database
        ChatMessage messageEntity = buildChatMessageEntity(message);
        chatMessageRepository.save(messageEntity);
        log.info("Message saved to database with ID: {} and content: {}", messageEntity.getId(), messageEntity.getContent());
        
        return message;
    }

    @Override
    public void sendPrivateMessage(Long userId, ChatMessageDTO message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/private",
                message
        );
        log.info("Private message sent to user {}: {}", userId, message.getContent());
    }

    @Override
    public void sendMatchNotification(Long user1Id, Long user2Id, String matchMessage) {
        ChatMessageDTO notification1 = buildChatMessageDTO(user1Id, user2Id, "New match! " + matchMessage, MessageType.MATCH_NOTIFICATION);
        ChatMessageDTO notification2 = buildChatMessageDTO(user2Id, user1Id, "New match! " + matchMessage, MessageType.MATCH_NOTIFICATION);
        
        // Save notifications to database
        ChatMessage notification1Entity = buildChatMessageEntity(notification1);
        ChatMessage notification2Entity = buildChatMessageEntity(notification2);
        
        chatMessageRepository.save(notification1Entity);
        chatMessageRepository.save(notification2Entity);
        
        // Send to both users
        messagingTemplate.convertAndSendToUser(user1Id.toString(), "/queue/notifications", notification1);
        messagingTemplate.convertAndSendToUser(user2Id.toString(), "/queue/notifications", notification2);
        
        log.info("Match notification sent to users {} and {}: {}", user1Id, user2Id, matchMessage);
    }

    @Override
    public List<ChatMessageDTO> getChatHistory(Long user1Id, Long user2Id) {
        log.info("Getting chat history between users {} and {}", user1Id, user2Id);
        
        List<ChatMessage> chatHistory = chatMessageRepository.findChatHistoryBetweenUsers(user1Id, user2Id);
        
        return chatHistory.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO buildChatMessageDTO(Long senderId, Long receiverId, String content, MessageType type) {
        ChatMessageDTO messageDTO = new ChatMessageDTO();
        messageDTO.setMessageId(UUID.randomUUID().toString());
        messageDTO.setSenderId(senderId);
        messageDTO.setReceiverId(receiverId);
        messageDTO.setContent(content);
        messageDTO.setTimestamp(String.valueOf(LocalDateTime.now()));
        messageDTO.setType(type);
        return messageDTO;
    }

    private ChatMessage buildChatMessageEntity(ChatMessageDTO messageDTO) {
        return ChatMessage.builder()
                .messageId(messageDTO.getMessageId())
                .senderId(messageDTO.getSenderId())
                .receiverId(messageDTO.getReceiverId())
                .content(messageDTO.getContent())
                .timestamp(LocalDateTime.parse(messageDTO.getTimestamp()))
                .messageType(MessageType.valueOf(messageDTO.getType().name()))
                .isRead(false)
                .build();
    }

    private ChatMessageDTO convertToDto(ChatMessage entity) {
        return new ChatMessageDTO(
                entity.getMessageId(),
                entity.getSenderId(),
                entity.getReceiverId(),
                entity.getContent(),
                entity.getTimestamp().toString(),
                MessageType.valueOf(entity.getMessageType().name())
        );
    }
} 