package com.knowzone.controller;

import com.knowzone.dto.ChatMessageDTO;
import com.knowzone.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket endpoints
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        log.info("Received message: {}", chatMessage.getContent());
        ChatMessageDTO processedMessage = chatService.processMessage(chatMessage);

        Long senderId = processedMessage.getSenderId();
        Long receiverId = processedMessage.getReceiverId();

        Long smallerId = Math.min(senderId, receiverId);
        Long largerId = Math.max(senderId, receiverId);

        String destination = String.format("/topic/chat-%d-%d", smallerId, largerId);

        messagingTemplate.convertAndSend(destination, processedMessage);
    }

    @MessageMapping("/chat/addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        // Add user ID in web socket session
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        log.info("User added to chat: {}", chatMessage.getSenderId());

        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId().toString(),
                "/queue/private",
                chatMessage
        );
    }

    // REST API endpoints
    @GetMapping("/api/chat/history/{user1Id}/{user2Id}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long user1Id,
                                                               @PathVariable Long user2Id) {
        log.info("Getting chat history between users {} and {}", user1Id, user2Id);
        List<ChatMessageDTO> chatHistory = chatService.getChatHistory(user1Id, user2Id);
        return ResponseEntity.ok(chatHistory);
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<ChatMessageDTO> sendChatMessage(@RequestBody ChatMessageDTO chatMessage) {
        log.info("Sending chat message from user {} to user {}", 
                chatMessage.getSenderId(), chatMessage.getReceiverId());
        ChatMessageDTO processedMessage = chatService.processMessage(chatMessage);
        return ResponseEntity.ok(processedMessage);
    }
} 