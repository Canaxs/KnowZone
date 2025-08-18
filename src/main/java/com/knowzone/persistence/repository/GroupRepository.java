package com.knowzone.persistence.repository;

import com.knowzone.persistence.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByIsActiveTrue();

    List<Group> findActiveGroupsAsResponse();
    List<Group> findByRegionIdAndIsActiveTrue(Long regionId);
    List<Group> findByCreatedByAndIsActiveTrue(Long userId);
    
    @Query("""
            SELECT g FROM Group g
            WHERE g.isActive = true
            AND g.region.latitude IS NOT NULL AND g.region.longitude IS NOT NULL
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(g.region.latitude)) *
                    cos(radians(g.region.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(g.region.latitude))
                )
            ) <= :radiusKm
           """)
    List<Group> findNearbyGroupsByHaversine(@Param("lat") double lat,
                                            @Param("lon") double lon,
                                            @Param("radiusKm") double radiusKm);

    @Query("SELECT DISTINCT g.region.id FROM Group g WHERE g.createdAt >= :threeDaysAgo")
    List<Long> findRegionIdsOfGroupsCreatedInLastDays(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);
}
