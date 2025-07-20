package com.knowzone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AITopicResponse {
    private String topic;
    private List<String> keywords;
}
