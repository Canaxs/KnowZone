package com.knowzone.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionRequest {
    private String name;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String country;
    private String city;
    private String timezone;
}
