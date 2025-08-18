package com.knowzone.service.impl;

import com.knowzone.dto.RegionRequest;
import com.knowzone.dto.RegionResponse;
import com.knowzone.persistence.entity.Region;
import com.knowzone.persistence.repository.RegionRepository;
import com.knowzone.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public List<RegionResponse> getAllActiveRegions() {
        return regionRepository.findActiveRegionsAsResponse();
    }

    @Override
    public RegionResponse getRegionById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found with id: " + id));
        return convertToResponse(region);
    }

    @Override
    public List<RegionResponse> getNearbyRegions(Double latitude, Double longitude) {
        return regionRepository.findActiveRegionsAsResponse().stream()
                .filter(region -> calculateDistance(latitude, longitude, 
                        region.getLatitude(), region.getLongitude()) <= region.getRadiusKm())
                .collect(Collectors.toList());
    }

    @Override
    public RegionResponse createRegion(RegionRequest regionRequest) {
        Region region = Region.builder()
                .name(regionRequest.getName())
                .latitude(regionRequest.getLatitude())
                .longitude(regionRequest.getLongitude())
                .radiusKm(regionRequest.getRadiusKm())
                .country(regionRequest.getCountry())
                .city(regionRequest.getCity())
                .timezone(regionRequest.getTimezone())
                .isActive(true)
                .build();
        
        Region savedRegion = regionRepository.save(region);
        return convertToResponse(savedRegion);
    }

    @Override
    public RegionResponse updateRegion(Long id, RegionRequest regionRequest) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found with id: " + id));
        
        region.setName(regionRequest.getName());
        region.setLatitude(regionRequest.getLatitude());
        region.setLongitude(regionRequest.getLongitude());
        region.setRadiusKm(regionRequest.getRadiusKm());
        region.setCountry(regionRequest.getCountry());
        region.setCity(regionRequest.getCity());
        region.setTimezone(regionRequest.getTimezone());
        
        Region updatedRegion = regionRepository.save(region);
        return convertToResponse(updatedRegion);
    }

    @Override
    public void deleteRegion(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found with id: " + id));
        region.setIsActive(false);
        regionRepository.save(region);
    }

    @Override
    public List<RegionResponse> getRegionsByCity(String city) {
        return regionRepository.findActiveRegionsAsResponse().stream()
                .filter(region -> city.equalsIgnoreCase(region.getCity()))
                .collect(Collectors.toList());
    }

    private RegionResponse convertToResponse(Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .latitude(region.getLatitude())
                .longitude(region.getLongitude())
                .radiusKm(region.getRadiusKm())
                .country(region.getCountry())
                .city(region.getCity())
                .timezone(region.getTimezone())
                .isActive(region.getIsActive())
                .createdAt(region.getCreatedAt())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
