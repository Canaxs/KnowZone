package com.knowzone.dto;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Long userId;
    private Double latitude;
    private Double longitude;
}
