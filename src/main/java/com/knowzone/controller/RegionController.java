package com.knowzone.controller;

import com.knowzone.dto.RegionRequest;
import com.knowzone.dto.RegionResponse;
import com.knowzone.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAllActiveRegions() {
        log.info("Getting all active regions");
        List<RegionResponse> regions = regionService.getAllActiveRegions();
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getRegionById(@PathVariable Long id) {
        log.info("Getting region by id: {}", id);
        RegionResponse region = regionService.getRegionById(id);
        return ResponseEntity.ok(region);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<RegionResponse>> getNearbyRegions(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("Getting nearby regions for coordinates: {}, {}", latitude, longitude);
        List<RegionResponse> regions = regionService.getNearbyRegions(latitude, longitude);
        return ResponseEntity.ok(regions);
    }

    @PostMapping
    public ResponseEntity<RegionResponse> createRegion(@RequestBody RegionRequest regionRequest) {
        log.info("Creating new region: {}", regionRequest.getName());
        RegionResponse createdRegion = regionService.createRegion(regionRequest);
        return ResponseEntity.ok(createdRegion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponse> updateRegion(@PathVariable Long id, @RequestBody RegionRequest regionRequest) {
        log.info("Updating region with id: {}", id);
        RegionResponse updatedRegion = regionService.updateRegion(id, regionRequest);
        return ResponseEntity.ok(updatedRegion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        log.info("Deleting region with id: {}", id);
        regionService.deleteRegion(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<RegionResponse>> getRegionsByCity(@PathVariable String city) {
        log.info("Getting regions by city: {}", city);
        List<RegionResponse> regions = regionService.getRegionsByCity(city);
        return ResponseEntity.ok(regions);
    }
}
