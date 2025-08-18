package com.knowzone.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String country;
    private String city;
    private String timezone;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
