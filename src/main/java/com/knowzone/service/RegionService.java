package com.knowzone.service;

import com.knowzone.dto.RegionRequest;
import com.knowzone.dto.RegionResponse;

import java.util.List;

public interface RegionService {
    
    List<RegionResponse> getAllActiveRegions();
    
    RegionResponse getRegionById(Long id);
    
    List<RegionResponse> getNearbyRegions(Double latitude, Double longitude);
    
    RegionResponse createRegion(RegionRequest regionRequest);
    
    RegionResponse updateRegion(Long id, RegionRequest regionRequest);
    
    void deleteRegion(Long id);
    
    List<RegionResponse> getRegionsByCity(String city);
}
