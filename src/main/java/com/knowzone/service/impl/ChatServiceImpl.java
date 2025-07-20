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
        message.setTimestamp(LocalDateTime.now());
        
        log.info("Processing message from user {} to user {}: {}", 
                message.getSenderId(), message.getReceiverId(), message.getContent());
        
        // Save message to database
        ChatMessage messageEntity = ChatMessage.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageType(MessageType.valueOf(message.getType().name()))
                .isRead(false)
                .build();
        
        chatMessageRepository.save(messageEntity);
        log.info("Message saved to database with ID: {}", messageEntity.getId());
        
        // Send to specific user if it's a private message
        if (message.getReceiverId() != null) {
            sendPrivateMessage(message.getReceiverId(), message);
        }
        
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
        ChatMessageDTO notification1 = new ChatMessageDTO();
        notification1.setMessageId(UUID.randomUUID().toString());
        notification1.setContent("New match! " + matchMessage);
        notification1.setType(MessageType.MATCH_NOTIFICATION);
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setSenderId(user1Id);
        notification1.setReceiverId(user2Id);
        
        ChatMessageDTO notification2 = new ChatMessageDTO();
        notification2.setMessageId(UUID.randomUUID().toString());
        notification2.setContent("New match! " + matchMessage);
        notification2.setType(MessageType.MATCH_NOTIFICATION);
        notification2.setTimestamp(LocalDateTime.now());
        notification2.setSenderId(user2Id);
        notification2.setReceiverId(user1Id);
        
        // Save notifications to database
        ChatMessage notification1Entity = ChatMessage.builder()
                .messageId(notification1.getMessageId())
                .senderId(notification1.getSenderId())
                .receiverId(notification1.getReceiverId())
                .content(notification1.getContent())
                .timestamp(notification1.getTimestamp())
                .messageType(MessageType.MATCH_NOTIFICATION)
                .isRead(false)
                .build();
        
        ChatMessage notification2Entity = ChatMessage.builder()
                .messageId(notification2.getMessageId())
                .senderId(notification2.getSenderId())
                .receiverId(notification2.getReceiverId())
                .content(notification2.getContent())
                .timestamp(notification2.getTimestamp())
                .messageType(MessageType.MATCH_NOTIFICATION)
                .isRead(false)
                .build();
        
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

    private ChatMessageDTO convertToDto(ChatMessage entity) {
        return new ChatMessageDTO(
                entity.getMessageId(),
                entity.getSenderId(),
                entity.getReceiverId(),
                entity.getContent(),
                entity.getTimestamp(),
                MessageType.valueOf(entity.getMessageType().name())
        );
    }
} 