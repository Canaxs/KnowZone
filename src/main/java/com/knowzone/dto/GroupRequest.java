package com.knowzone.dto;

import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequest {
    private String name;
    private String description;
    private Long regionId;
    private Integer maxMembers;
    private LocalTime startTime;
    private LocalTime endTime;
}
