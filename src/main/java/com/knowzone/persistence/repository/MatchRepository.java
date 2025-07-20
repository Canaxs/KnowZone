package com.knowzone.persistence.repository;

import com.knowzone.enums.MatchStatus;
import com.knowzone.persistence.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match , Long> {
    boolean existsByUser1IdAndUser2IdOrUser2IdAndUser1Id(Long user1Id, Long user2Id, Long user2Id2, Long user1Id2);
    List<Match> findByUser1IdOrUser2IdAndStatus(Long userId1, Long userId2, MatchStatus status);
    List<Match> findByStatusAndExpiresAtBefore(MatchStatus status, LocalDateTime dateTime);
}
