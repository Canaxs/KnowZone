package com.knowzone.service.impl;

import com.knowzone.dto.GroupChatRequest;
import com.knowzone.dto.GroupChatResponse;
import com.knowzone.persistence.entity.Group;
import com.knowzone.persistence.entity.GroupChat;
import com.knowzone.persistence.entity.GroupMember;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.GroupChatRepository;
import com.knowzone.persistence.repository.GroupMemberRepository;
import com.knowzone.persistence.repository.GroupRepository;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatServiceImpl implements GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public List<GroupChatResponse> getGroupChatHistory(Long groupId) {
        return groupChatRepository.findByGroupIdOrderBySentAtDesc(groupId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupChatResponse> getRecentGroupChats(Long groupId, int limit) {
        return groupChatRepository.findByGroupIdOrderBySentAtDesc(groupId)
                .stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupChatResponse sendGroupMessage(GroupChatRequest groupChatRequest) {
        // Check if user is member of the group
        log.info("GroupId: "+groupChatRequest.getGroupId().toString()+ " UserId: "+groupChatRequest.getUserId());
        if (!isUserMemberOfGroup(groupChatRequest.getUserId(), groupChatRequest.getGroupId())) {
            throw new RuntimeException("User is not a member of this group");
        }
        
        Group group = groupRepository.findById(groupChatRequest.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupChatRequest.getGroupId()));
        
        User user = userRepository.findById(groupChatRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + groupChatRequest.getUserId()));

        GroupChat groupChat = GroupChat.builder()
                .group(group)
                .user(user)
                .message(groupChatRequest.getMessage())
                .build();
        
        GroupChat savedChat = groupChatRepository.save(groupChat);
        return convertToResponse(savedChat);
    }

    @Override
    public void deleteGroupMessage(Long chatId) {
        groupChatRepository.deleteById(chatId);
    }

    @Override
    @Transactional
    public boolean joinGroup(Long userId, Long groupId) {
        if (isUserMemberOfGroup(userId, groupId)) {
            log.warn("User {} is already a member of group {}", userId, groupId);
            return false;
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if group is full
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            log.warn("Group {} is full, cannot add user {}", groupId, userId);
            return false;
        }
        
        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        
        groupMemberRepository.save(groupMember);
        
        // Update current members count
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        groupRepository.save(group);
        
        log.info("User {} successfully joined group {}", userId, groupId);
        return true;
    }

    @Override
    @Transactional
    public boolean leaveGroup(Long userId, Long groupId) {
        if (!isUserMemberOfGroup(userId, groupId)) {
            log.warn("User {} is not a member of group {}", userId, groupId);
            return false;
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        
        // Remove user from group
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
        
        // Update current members count
        group.setCurrentMembers(group.getCurrentMembers() - 1);
        groupRepository.save(group);
        
        log.info("User {} successfully left group {}", userId, groupId);
        return true;
    }

    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(groupMember -> groupMember.getUser().getId())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private GroupChatResponse convertToResponse(GroupChat groupChat) {
        return GroupChatResponse.builder()
                .id(groupChat.getId())
                .groupId(groupChat.getGroup().getId())
                .userId(groupChat.getUser().getId())
                .userName(groupChat.getUser().getUsername())
                .message(groupChat.getMessage())
                .sentAt(groupChat.getSentAt())
                .build();
    }
}
