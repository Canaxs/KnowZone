package com.knowzone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicCombination {
    private String category1;
    private String category2;
    private String topic;
}
