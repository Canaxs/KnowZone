package com.knowzone.controller;

import com.knowzone.dto.GroupChatRequest;
import com.knowzone.dto.GroupChatResponse;
import com.knowzone.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/group-chats")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/group-chat/sendMessage")
    public void sendGroupMessage(@Payload GroupChatRequest groupChatRequest) {
        handleChatMessage(groupChatRequest);
    }

    private void handleChatMessage(GroupChatRequest groupChatRequest) {
        log.info("Received group message: {} for group: {}", 
                groupChatRequest.getMessage(), groupChatRequest.getGroupId());
        
        GroupChatResponse processedMessage = groupChatService.sendGroupMessage(groupChatRequest);

        String destination = String.format("/topic/group-%d", groupChatRequest.getGroupId());
        messagingTemplate.convertAndSend(destination, processedMessage);
        
        log.info("Group message sent to destination: {}", destination);
    }

    @MessageMapping("/group-chat/leaveGroup")
    public void leaveGroup(@Payload GroupChatRequest groupChatRequest,
                          SimpMessageHeaderAccessor headerAccessor) {
        Long userId = groupChatRequest.getUserId();
        Long groupId = groupChatRequest.getGroupId();
        
        log.info("User {} leaving group chat: {}", userId, groupId);
        
        boolean left = groupChatService.leaveGroup(userId, groupId);
        
        if (left) {
            // Remove user from session attributes
            headerAccessor.getSessionAttributes().remove("groupId");
            
            // Notify user that they left the group
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/group-left",
                    "Successfully left group: " + groupId
            );
            
            // Notify other group members
            String destination = String.format("/topic/group-%d", groupId);
            messagingTemplate.convertAndSend(destination, 
                    String.format("User %d left the group", userId));
        }
    }

    @GetMapping("/history/{groupId}")
    public ResponseEntity<List<GroupChatResponse>> getGroupChatHistory(@PathVariable Long groupId) {
        log.info("Getting chat history for group: {}", groupId);
        List<GroupChatResponse> chats = groupChatService.getGroupChatHistory(groupId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/recent/{groupId}")
    public ResponseEntity<List<GroupChatResponse>> getRecentGroupChats(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("Getting recent {} chats for group: {}", limit, groupId);
        List<GroupChatResponse> chats = groupChatService.getRecentGroupChats(groupId, limit);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/members/{groupId}")
    public ResponseEntity<List<Long>> getGroupMembers(@PathVariable Long groupId) {
        log.info("Getting members for group: {}", groupId);
        List<Long> memberIds = groupChatService.getGroupMemberIds(groupId);
        return ResponseEntity.ok(memberIds);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteGroupMessage(@PathVariable Long chatId) {
        log.info("Deleting group chat message: {}", chatId);
        groupChatService.deleteGroupMessage(chatId);
        return ResponseEntity.ok().build();
    }
}
