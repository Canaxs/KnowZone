package com.knowzone.service;

import com.knowzone.dto.AITopicResponse;
import com.knowzone.persistence.entity.User;

import java.util.List;
import java.util.Set;

public interface AITopicService {

    String getTopicFromAI(Set<String> commonInterests);
    List<String> getKeywordsFromAI(String topic, int keywordCount);
    AITopicResponse generateTopicAndKeywords(User user1, User user2);
}
