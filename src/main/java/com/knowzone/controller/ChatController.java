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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // WebSocket endpoints
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO chatMessage) {
        log.info("Received message: {}", chatMessage.getContent());
        return chatService.processMessage(chatMessage);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(@Payload ChatMessageDTO chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        // Add user ID in web socket session
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        log.info("User added to chat: {}", chatMessage.getSenderId());
        return chatMessage;
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