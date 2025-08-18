package com.knowzone.persistence.repository;

import com.knowzone.persistence.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    
    List<GroupChat> findByGroupIdOrderBySentAtDesc(Long groupId);
    List<GroupChat> findByGroupIdAndUserIdOrderBySentAtDesc(Long groupId, Long userId);
}
