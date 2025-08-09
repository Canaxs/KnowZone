package com.knowzone.service.impl;

import com.knowzone.service.SimilarityService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

@Service
public class SimilarityServiceImpl implements SimilarityService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String similarityApiUrl = "http://localhost:5000/similarity";

    @Override
    public double getSimilarityScore(String sentence1, String sentence2) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("sentence1", sentence1);
            requestBody.put("sentence2", sentence2);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(similarityApiUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.get("similarity_score").asDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
} 