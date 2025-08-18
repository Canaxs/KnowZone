package com.knowzone.controller;

import com.knowzone.config.security.CustomUserDetails;
import com.knowzone.dto.GroupResponse;
import com.knowzone.persistence.entity.GroupMember;
import com.knowzone.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllActiveGroups() {
        log.info("Getting all active groups");
        List<GroupResponse> groups = groupService.getAllActiveGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long id) {
        log.info("Getting group by id: {}", id);
        GroupResponse group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<GroupResponse>> getGroupsByRegion(@PathVariable Long regionId) {
        log.info("Getting groups by region id: {}", regionId);
        List<GroupResponse> groups = groupService.getGroupsByRegion(regionId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<GroupResponse>> getNearbyGroups(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusKm) {
        log.info("Getting nearby groups for coordinates: {}, {}", latitude, longitude);
        List<GroupResponse> groups = groupService.getNearbyGroups(latitude, longitude, radiusKm);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/active-now")
    public ResponseEntity<List<GroupResponse>> getActiveGroupsNow() {
        log.info("Getting groups active now");
        List<GroupResponse> groups = groupService.getActiveGroupsNow();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> joinGroup(@PathVariable Long groupId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUserId());
        log.info("User {} joining group {}",userId,groupId);
        groupService.addMemberToGroup(groupId , userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUserId());
        log.info("User {} leaving group {}", userId, groupId);
        groupService.removeMemberFromGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        log.info("Getting members for group: {}", groupId);
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/user")
    public ResponseEntity<List<GroupResponse>> getGroupsByUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUserId());
        log.info("Getting groups for user: {}", userId);
        List<GroupResponse> groups = groupService.getGroupsByUser(userId);
        return ResponseEntity.ok(groups);
    }
}
