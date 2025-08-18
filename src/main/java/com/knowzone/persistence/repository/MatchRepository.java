package com.knowzone.persistence.repository;

import com.knowzone.enums.MatchStatus;
import com.knowzone.enums.MatchUserStatus;
import com.knowzone.persistence.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match , Long> {
    boolean existsByUser1IdAndUser2IdOrUser2IdAndUser1Id(Long user1Id, Long user2Id, Long user2Id2, Long user1Id2);

    @Query("SELECT m FROM Match m WHERE (m.user1Id = :userId OR m.user2Id = :userId) AND m.status = :status")
    List<Match> findPendingMatchesForUser(@Param("userId") Long userId, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE " +
            "m.status = :status AND " +
            "((m.user1Id = :userId AND m.user1Response = :userResponse ) OR " +
            "(m.user2Id = :userId AND m.user2Response = :userResponse ))")
    List<Match> findPendingMatchesForUserByResponse(@Param("userId") Long userId,
                                                    @Param("status") MatchStatus status,
                                                    @Param("userResponse") MatchUserStatus userResponse);

    List<Match> findByStatusAndExpiresAtBefore(MatchStatus status, LocalDateTime dateTime);
}
