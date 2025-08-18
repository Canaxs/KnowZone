package com.knowzone.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRequest {
    private Long groupId;
    private Long userId;
    private String message;
    private String messageType; // "CHAT", "JOIN", "LEAVE"
}
