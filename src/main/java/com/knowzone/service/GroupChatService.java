package com.knowzone.service;

import com.knowzone.dto.GroupChatRequest;
import com.knowzone.dto.GroupChatResponse;

import java.util.List;

public interface GroupChatService {
    
    List<GroupChatResponse> getGroupChatHistory(Long groupId);
    
    List<GroupChatResponse> getRecentGroupChats(Long groupId, int limit);
    
    GroupChatResponse sendGroupMessage(GroupChatRequest groupChatRequest);
    
    void deleteGroupMessage(Long chatId);
    
    // WebSocket integration methods
    boolean joinGroup(Long userId, Long groupId);
    
    boolean leaveGroup(Long userId, Long groupId);
    
    List<Long> getGroupMemberIds(Long groupId);
    
    boolean isUserMemberOfGroup(Long userId, Long groupId);
}
