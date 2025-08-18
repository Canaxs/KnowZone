package com.knowzone.service;

import com.knowzone.dto.MatchResponseRequest;
import com.knowzone.persistence.entity.Match;
import com.knowzone.persistence.entity.User;

import java.util.List;

public interface MatchingService {
    double calculateCompatibilityScore(User user1, User user2);
    boolean evaluateAndCreateMatch(User user1, User user2);

    List<Match> getUserMatches(Long userId);

    List<Match> getAllUserMatches(Long userId);
    
    List<Match> getUserAcceptedMatches(Long userId);

    Match getMatchById(Long matchId);

    String respondToMatch(Long matchId, MatchResponseRequest request);

    List<String> getCommonHobbies(User user1, User user2);

    String getTopicForCommonHobbies(User user1, User user2);
}
