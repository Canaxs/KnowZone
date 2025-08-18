package com.knowzone.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatResponse {
    private Long id;
    private Long groupId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime sentAt;
}
