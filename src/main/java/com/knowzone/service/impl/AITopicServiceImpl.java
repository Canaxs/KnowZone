package com.knowzone.service.impl;

import com.knowzone.dto.AITopicResponse;
import com.knowzone.persistence.entity.User;
import com.knowzone.service.AITopicService;
import com.knowzone.util.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITopicServiceImpl implements AITopicService {

    private final OpenAIClient openAIClient;

    @Override
    public String getTopicFromAI(Set<String> commonInterests) {
        String prompt = "Users have these common interests: " + String.join(", ", commonInterests) +
                ". Suggest a fun and engaging conversation topic as a title.";

        log.info("Generating topic for common interests: {}", commonInterests);
        
        try {
            String aiTopic = openAIClient.callOpenAI(prompt, 30);
            log.info("Generated topic: {}", aiTopic);
            return aiTopic;
        } catch (Exception e) {
            log.error("Failed to generate topic from AI, using fallback", e);
            return "Let's get to know each other!";
        }
    }

    @Override
    public List<String> getKeywordsFromAI(String topic, int keywordCount) {
        String prompt = "Given the topic: '" + topic + "', generate " + keywordCount + 
                " unique, relevant, and concise keywords as a comma-separated list. Only return the keywords, nothing else.";

        log.info("Generating {} keywords for topic: {}", keywordCount, topic);
        
        try {
            String aiKeywords = openAIClient.callOpenAI(prompt, 100 + keywordCount * 3);
            
            List<String> keywords = Arrays.stream(aiKeywords.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            
            log.info("Generated {} keywords for topic: {}", keywords.size(), topic);
            return keywords;
            
        } catch (Exception e) {
            log.error("Failed to generate keywords from AI, using fallback", e);
            return generateFallbackKeywords(topic);
        }
    }

    @Override
    public AITopicResponse generateTopicAndKeywords(User user1, User user2) {
        log.info("Generating topic and keywords for users: {} and {}", user1.getUsername(), user2.getUsername());
        
        // Find common interests
        Set<String> commonInterests = new HashSet<>(user1.getInterests());
        commonInterests.retainAll(user2.getInterests());

        String topic;
        List<String> keywords = new ArrayList<>();

        if (!commonInterests.isEmpty()) {
            log.info("Found common interests: {}", commonInterests);
            topic = getTopicFromAI(commonInterests);
            keywords = getKeywordsFromAI(topic, 5); // Default to 5 keywords
        } else {
            log.info("No common interests found, using default topic");
            topic = "Let's get to know each other!";
            keywords = generateFallbackKeywords(topic);
        }

        return new AITopicResponse(topic, keywords);
    }

    private List<String> generateFallbackKeywords(String topic) {
        log.info("Using fallback keywords for topic: {}", topic);
        
        // Mock keyword generation - replace with AI service
        Map<String, List<String>> interestKeywords = Map.of(
                "technology", Arrays.asList("programming", "AI", "gadgets", "innovation", "software"),
                "music", Arrays.asList("concerts", "instruments", "genres", "artists", "festivals"),
                "sports", Arrays.asList("teams", "games", "fitness", "training", "competitions"),
                "travel", Arrays.asList("destinations", "cultures", "adventures", "experiences", "memories")
        );

        return interestKeywords.getOrDefault(topic.toLowerCase(),
                Arrays.asList("experiences", "thoughts", "preferences", "stories", "interests"));
    }
}
