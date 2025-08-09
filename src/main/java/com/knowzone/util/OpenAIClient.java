package com.knowzone.util;
/*
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    private final RestTemplate restTemplate;

    @Value("${openai.key}")
    private String openaiApiKey;

    @Value("${openai.url}")
    private String openaiApiUrl;

    public String callOpenAI(String prompt, int maxTokens) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", maxTokens);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(openaiApiUrl, entity, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            String aiResponse = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            log.info("OpenAI API call successful for prompt: {}", prompt.substring(0, Math.min(prompt.length(), 50)));
            return aiResponse.trim();

        } catch (Exception e) {
            log.error("OpenAI API call failed for prompt: {}", prompt.substring(0, Math.min(prompt.length(), 50)), e);
            throw new RuntimeException("Failed to get response from OpenAI API: " + e.getMessage(), e);
        }
    }
}
 */