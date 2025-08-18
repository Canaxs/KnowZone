package com.knowzone.service.impl;

import com.knowzone.config.security.CustomUserDetails;
import com.knowzone.dto.GroupRequest;
import com.knowzone.dto.GroupResponse;
import com.knowzone.dto.RegionResponse;
import com.knowzone.persistence.entity.Group;
import com.knowzone.persistence.entity.GroupMember;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.GroupRepository;
import com.knowzone.persistence.repository.GroupMemberRepository;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Override
    public List<GroupResponse> getAllActiveGroups() {
        return groupRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GroupResponse getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        return convertToResponse(group);
    }

    @Override
    public List<GroupResponse> getGroupsByRegion(Long regionId) {
        return groupRepository.findByRegionIdAndIsActiveTrue(regionId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getNearbyGroups(Double latitude, Double longitude, Double radiusKm) {
        double searchRadius = radiusKm != null ? radiusKm : 5.0;
        
        return groupRepository.findNearbyGroupsByHaversine(latitude, longitude, searchRadius)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getActiveGroupsNow() {
        LocalTime now = LocalTime.now();
        return groupRepository.findByIsActiveTrue().stream()
                .filter(group -> now.isAfter(group.getStartTime()) && now.isBefore(group.getEndTime()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GroupResponse createGroup(GroupRequest groupRequest) {
        Group group = Group.builder()
                .name(groupRequest.getName())
                .description(groupRequest.getDescription())
                .maxMembers(groupRequest.getMaxMembers())
                .startTime(groupRequest.getStartTime())
                .endTime(groupRequest.getEndTime())
                .isActive(true)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        return convertToResponse(savedGroup);
    }

    @Override
    public GroupResponse updateGroup(Long id, GroupRequest groupRequest) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        
        group.setName(groupRequest.getName());
        group.setDescription(groupRequest.getDescription());
        group.setMaxMembers(groupRequest.getMaxMembers());
        group.setStartTime(groupRequest.getStartTime());
        group.setEndTime(groupRequest.getEndTime());
        
        Group updatedGroup = groupRepository.save(group);
        return convertToResponse(updatedGroup);
    }

    @Override
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        group.setIsActive(false);
        groupRepository.save(group);
    }

    @Override
    public void addMemberToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Grup kapasitesi kontrolü
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new RuntimeException("Group is full");
        }

        // Kullanıcı zaten üye mi kontrol et
        Optional<GroupMember> existingMember = groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("User is already a member of this group");
        }

        // Yeni üye ekle
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        groupMemberRepository.save(member);

        // Grup üye sayısını güncelle
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        groupRepository.save(group);
    }

    @Override
    public void removeMemberFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        
        Optional<GroupMember> member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId);
        if (member.isPresent()) {
            groupMemberRepository.delete(member.get());
            
            // Grup üye sayısını güncelle
            group.setCurrentMembers(group.getCurrentMembers() - 1);
            groupRepository.save(group);
        }
    }

    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    @Override
    public List<GroupResponse> getGroupsByUser(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(GroupMember::getGroup)
                .filter(Group::getIsActive)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private GroupResponse convertToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .region(convertRegionToResponse(group.getRegion()))
                .maxMembers(group.getMaxMembers())
                .currentMembers(group.getCurrentMembers())
                .startTime(group.getStartTime())
                .endTime(group.getEndTime())
                .isActive(group.getIsActive())
                .groupCreationType(group.getGroupCreationType())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    private RegionResponse convertRegionToResponse(com.knowzone.persistence.entity.Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .latitude(region.getLatitude())
                .longitude(region.getLongitude())
                .radiusKm(region.getRadiusKm())
                .country(region.getCountry())
                .city(region.getCity())
                .timezone(region.getTimezone())
                .isActive(region.getIsActive())
                .createdAt(region.getCreatedAt())
                .build();
    }


}
