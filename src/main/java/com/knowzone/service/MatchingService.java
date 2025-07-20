package com.knowzone.service;

import com.knowzone.dto.MatchResponseRequest;
import com.knowzone.persistence.entity.Match;
import com.knowzone.persistence.entity.User;

import java.util.List;

public interface MatchingService {
    double calculateCompatibilityScore(User user1, User user2);
    void evaluateAndCreateMatch(User user1, User user2);

    List<Match> getUserMatches(Long userId);

    String respondToMatch(Long matchId, MatchResponseRequest request);
}
