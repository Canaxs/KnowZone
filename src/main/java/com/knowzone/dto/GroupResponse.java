package com.knowzone.dto;

import com.knowzone.enums.GroupCreationType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private RegionResponse region;
    private Integer maxMembers;
    private Integer currentMembers;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private GroupCreationType groupCreationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
