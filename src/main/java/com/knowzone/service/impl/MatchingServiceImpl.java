package com.knowzone.service.impl;

import com.knowzone.dto.AITopicResponse;
import com.knowzone.dto.MatchResponseRequest;
import com.knowzone.enums.MatchStatus;
import com.knowzone.persistence.entity.Match;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.MatchRepository;
import com.knowzone.service.AITopicService;
import com.knowzone.service.ChatService;
import com.knowzone.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements MatchingService {

    private final MatchRepository matchRepository;
    private final AITopicService aiTopicService;
    private final ChatService chatService;

    @Override
    public double calculateCompatibilityScore(User user1, User user2) {
        Set<String> interests1 = user1.getInterests();
        Set<String> interests2 = user2.getInterests();

        if (interests1.isEmpty() || interests2.isEmpty()) {
            return 0.0;
        }

        Set<String> commonInterests = new HashSet<>(interests1);
        commonInterests.retainAll(interests2);

        Set<String> allInterests = new HashSet<>(interests1);
        allInterests.addAll(interests2);

        return (double) commonInterests.size() / allInterests.size() * 100;
    }

    @Override
    public void evaluateAndCreateMatch(User user1, User user2) {
        if (matchRepository.existsByUser1IdAndUser2IdOrUser2IdAndUser1Id(
                user1.getId(), user2.getId(), user1.getId(), user2.getId())) {
            return;
        }

        double compatibilityScore = calculateCompatibilityScore(user1, user2);

        if (compatibilityScore >= 30.0) {
            AITopicResponse aiResponse = aiTopicService.generateTopicAndKeywords(user1, user2);

            Match match = new Match();
            match.setUser1Id(user1.getId());
            match.setUser2Id(user2.getId());
            match.setCompatibilityScore(compatibilityScore);
            match.setCommonTopic(aiResponse.getTopic());
            match.setKeywords(aiResponse.getKeywords());

            matchRepository.save(match);
            
            log.info("New match created between users {} and {} with score: {}", 
                    user1.getId(), user2.getId(), compatibilityScore);
        }
    }

    @Override
    public List<Match> getUserMatches(Long userId) {
        return matchRepository.findByUser1IdOrUser2IdAndStatus(
                userId, userId, MatchStatus.PENDING);
    }

    @Override
    public String respondToMatch(Long matchId, MatchResponseRequest request) {
        try {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new RuntimeException("Match not found"));

            if (request.isAccepted()) {
                match.setStatus(MatchStatus.ACCEPTED);
                
                // Send chat notification when match is accepted
                String matchMessage = "You have a new match! Topic: " + match.getCommonTopic();
                chatService.sendMatchNotification(match.getUser1Id(), match.getUser2Id(), matchMessage);
                
                log.info("Match accepted between users {} and {}", match.getUser1Id(), match.getUser2Id());
            } else {
                match.setStatus(MatchStatus.DECLINED);
                log.info("Match declined between users {} and {}", match.getUser1Id(), match.getUser2Id());
            }

            matchRepository.save(match);
            return "Match response recorded";
        }
        catch (Exception e) {
            log.error("Error processing match response: {}", e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
