package com.knowzone.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowzone.dto.GroupTopicCombination;
import com.knowzone.dto.TopicCombination;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TopicService {
    private List<TopicCombination> combinations;
    private List<GroupTopicCombination> groupTopicCombinations;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        combinations = mapper.readValue(
                new ClassPathResource("data/topics.json").getInputStream(),
                new TypeReference<List<TopicCombination>>() {}
        );
        groupTopicCombinations = mapper.readValue(
                new ClassPathResource("data/group-topics.json").getInputStream(),
                new TypeReference<List<GroupTopicCombination>>() {}
        );
    }

    public String getTopic(String cat1, String cat2) {
        return combinations.stream()
                .filter(c ->
                        (c.getCategory1().equalsIgnoreCase(cat1) && c.getCategory2().equalsIgnoreCase(cat2)) ||
                                (c.getCategory1().equalsIgnoreCase(cat2) && c.getCategory2().equalsIgnoreCase(cat1))
                )
                .map(TopicCombination::getTopic)
                .findFirst()
                .orElse("Konu bulunamadı");
    }

    public String getRandomTopic() {
        if (combinations == null || combinations.isEmpty()) {
            return "Konu bulunamadı";
        }

        int randomIndex = (int) (Math.random() * combinations.size());
        return combinations.get(randomIndex).getTopic();
    }

    public GroupTopicCombination getRandomGroupTopic() {
        if (groupTopicCombinations == null || groupTopicCombinations.isEmpty()) {
            return null;
        }

        int randomIndex = (int) (Math.random() * groupTopicCombinations.size());
        return groupTopicCombinations.get(randomIndex);
    }
}

