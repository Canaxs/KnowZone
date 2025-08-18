package com.knowzone.service;

import com.knowzone.dto.GroupRequest;
import com.knowzone.dto.GroupResponse;
import com.knowzone.persistence.entity.GroupMember;

import java.util.List;

public interface GroupService {
    
    List<GroupResponse> getAllActiveGroups();
    
    GroupResponse getGroupById(Long id);
    
    List<GroupResponse> getGroupsByRegion(Long regionId);
    
    List<GroupResponse> getNearbyGroups(Double latitude, Double longitude, Double radiusKm);
    
    List<GroupResponse> getActiveGroupsNow();
    
    GroupResponse createGroup(GroupRequest groupRequest);
    
    GroupResponse updateGroup(Long id, GroupRequest groupRequest);
    
    void deleteGroup(Long id);
    
    void addMemberToGroup(Long groupId, Long userId);
    
    void removeMemberFromGroup(Long groupId, Long userId);
    
    List<GroupMember> getGroupMembers(Long groupId);
    
    List<GroupResponse> getGroupsByUser(Long userId);
}
