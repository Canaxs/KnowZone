package com.knowzone.service.impl;

import com.knowzone.config.security.CustomUserDetails;
import com.knowzone.dto.AITopicResponse;
import com.knowzone.dto.MatchResponseRequest;
import com.knowzone.enums.MatchStatus;
import com.knowzone.enums.MatchUserStatus;
import com.knowzone.persistence.entity.Match;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.MatchRepository;
//import com.knowzone.service.AITopicService;
import com.knowzone.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements MatchingService {

    private final MatchRepository matchRepository;
    //private final AITopicService aiTopicService;
    private final ChatService chatService;
    private final SimilarityService similarityService;
    private final FCMNotificationService fcmNotificationService;
    private final TopicService topicService;

    @Override
    public double calculateCompatibilityScore(User user1, User user2) {
        double score1 = averageSimilarity(user1.getInterests(), user2.getIdealPersonTraits());
        double score2 = averageSimilarity(user2.getInterests(), user1.getIdealPersonTraits());
        double mainScore = (score1 + score2) / 2.0;

        double hobbyScore = averageSimilarity(user1.getHobbies(), user2.getHobbies());

        return (mainScore * 0.8 + hobbyScore * 0.2) * 100;
    }

    private double averageSimilarity(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (String s1 : set1) {
            for (String s2 : set2) {
                total += similarityService.getSimilarityScore(s1, s2);
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    @Override
    public void evaluateAndCreateMatch(User user1, User user2) {
        if (matchRepository.existsByUser1IdAndUser2IdOrUser2IdAndUser1Id(
                user1.getId(), user2.getId(), user1.getId(), user2.getId())) {
            return;
        }

        double compatibilityScore = calculateCompatibilityScore(user1, user2);

        if (compatibilityScore >= 30.0) {
            //AITopicResponse aiResponse = aiTopicService.generateTopicAndKeywords(user1, user2);

            Match match = new Match();
            match.setUser1Id(user1.getId());
            match.setUser2Id(user2.getId());
            match.setCompatibilityScore(compatibilityScore);
            match.setCommonTopic(getTopicForCommonHobbies(user1,user2));
            match.setKeywords(null);

            matchRepository.save(match);

            fcmNotificationService.sendMatchNotification(user1.getId(), match);
            fcmNotificationService.sendMatchNotification(user2.getId(), match);
            
            log.info("New match created between users {} and {} with score: {}", 
                    user1.getId(), user2.getId(), compatibilityScore);
        }
    }

    @Override
    public List<Match> getUserMatches(Long userId) {
        return matchRepository.findPendingMatchesForUser(userId, MatchStatus.PENDING);
    }

    @Override
    public List<Match> getUserAcceptedMatches(Long userId) {
        return matchRepository.findPendingMatchesForUser(userId, MatchStatus.ACCEPTED);
    }

    @Override
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    @Override
    public String respondToMatch(Long matchId, MatchResponseRequest request) {
        try {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new RuntimeException("Match not found"));

            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Long respondingUserId = Long.valueOf(userDetails.getUserId());
            boolean isUser1 = match.getUser1Id().equals(respondingUserId);
            boolean isUser2 = match.getUser2Id().equals(respondingUserId);

            if (!isUser1 && !isUser2) {
                throw new RuntimeException("User is not part of this match");
            }

            if (isUser1) {
                match.setUser1Response(request.isAccepted() ? MatchUserStatus.ACCEPTED : MatchUserStatus.REJECTED);
            } else {
                match.setUser2Response(request.isAccepted() ? MatchUserStatus.ACCEPTED : MatchUserStatus.REJECTED);
            }

            if (match.getUser1Response() != MatchUserStatus.PENDING && match.getUser2Response() != MatchUserStatus.PENDING) {
                if (match.getUser1Response().equals(MatchUserStatus.ACCEPTED) && match.getUser2Response().equals(MatchUserStatus.ACCEPTED)) {
                    match.setStatus(MatchStatus.ACCEPTED);

                    // String matchMessage = "You have a new match! Topic: " + match.getCommonTopic();
                    // chatService.sendMatchNotification(match.getUser1Id(), match.getUser2Id(), matchMessage);

                    log.info("Match accepted by both users {} and {}", match.getUser1Id(), match.getUser2Id());
                } else {
                    match.setStatus(MatchStatus.DECLINED);
                    log.info("Match declined - User1: {}, User2: {}",
                            match.getUser1Response(), match.getUser2Response());
                }
            } else {
                log.info("Partial response recorded - User1: {}, User2: {}",
                        match.getUser1Response(), match.getUser2Response());
            }

            matchRepository.save(match);
            return "Match response recorded";
        }
        catch (Exception e) {
            log.error("Error processing match response: {}", e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<String> getCommonHobbies(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return List.of();
        }
        Set<String> hobbies1 = user1.getHobbies();
        Set<String> hobbies2 = user2.getHobbies();

        if (hobbies1 == null || hobbies2 == null) {
            return List.of();
        }

        Set<String> common = new HashSet<>(hobbies1);
        common.retainAll(hobbies2);

        return new ArrayList<>(common);
    }

    @Override
    public String getTopicForCommonHobbies(User user1, User user2) {
        List<String> commonHobbies = getCommonHobbies(user1, user2);

        if (commonHobbies.isEmpty()) {
            return "Ortak hobi bulunamadı.";
        }

        String cat1, cat2;

        if (commonHobbies.size() == 1) {
            cat1 = commonHobbies.get(0);
            cat2 = commonHobbies.get(0);
        } else {
            Collections.shuffle(commonHobbies);
            cat1 = commonHobbies.get(0);
            cat2 = commonHobbies.get(1);
        }

        return topicService.getTopic(cat1, cat2);
    }
}
