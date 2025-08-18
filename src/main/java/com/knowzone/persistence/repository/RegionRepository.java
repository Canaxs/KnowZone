package com.knowzone.persistence.repository;

import com.knowzone.persistence.entity.Region;
import com.knowzone.dto.RegionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region , Long> {

    List<Region> findByIsActiveTrue();
    
    @Query("SELECT new com.knowzone.dto.RegionResponse(r.id, r.name, r.latitude, r.longitude, r.radiusKm, r.country, r.city, r.timezone, r.isActive, r.createdAt) FROM Region r WHERE r.isActive = true")
    List<RegionResponse> findActiveRegionsAsResponse();
}
