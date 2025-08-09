package com.knowzone.persistence.repository;

import com.knowzone.persistence.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
            "((cm.senderId = :user1Id AND cm.receiverId = :user2Id) OR " +
            "(cm.senderId = :user2Id AND cm.receiverId = :user1Id)) " +
            "AND cm.messageType = 'CHAT' " +
            "ORDER BY cm.timestamp ASC")
    List<ChatMessage> findChatHistoryBetweenUsers(@Param("user1Id") Long user1Id,
                                                 @Param("user2Id") Long user2Id);

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.receiverId = :userId AND cm.isRead = false " +
           "ORDER BY cm.timestamp DESC")
    List<ChatMessage> findUnreadMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
           "cm.receiverId = :userId AND cm.isRead = false")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);

    List<ChatMessage> findBySenderIdAndReceiverIdOrderByTimestampAsc(Long senderId, Long receiverId);
} 