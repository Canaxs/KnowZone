package com.knowzone.persistence.repository;

import com.knowzone.persistence.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByIsActiveTrue();
    List<Group> findByRegionIdAndIsActiveTrue(Long regionId);
    
    @Query("""
            SELECT g FROM Group g
            WHERE g.isActive = true
            AND g.region.latitude IS NOT NULL AND g.region.longitude IS NOT NULL
            AND NOT EXISTS (
                SELECT gm FROM GroupMember gm
                WHERE gm.group.id = g.id AND gm.user.id = :userId
            )
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(g.region.latitude)) *
                    cos(radians(g.region.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(g.region.latitude))
                )
            ) <= g.region.radiusKm
           """)
    List<Group> findNearbyGroupsByHaversine(@Param("lat") double lat,
                                            @Param("lon") double lon,
                                            @Param("userId") Long userId);

    @Query("""
            SELECT g FROM Group g
            WHERE g.isActive = false
            AND g.region.latitude IS NOT NULL AND g.region.longitude IS NOT NULL
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(g.region.latitude)) *
                    cos(radians(g.region.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(g.region.latitude))
                )
            ) <= g.region.radiusKm
           """)
    List<Group> findNearbyInactiveGroupsByLocation(@Param("lat") double lat,
                                            @Param("lon") double lon);

    @Query("SELECT DISTINCT g.region.id FROM Group g WHERE g.createdAt >= :threeDaysAgo")
    List<Long> findRegionIdsOfGroupsCreatedInLastDays(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);

    @Modifying
    @Query("""
        UPDATE Group g 
        SET g.isActive = false 
        WHERE g.isActive = true 
        AND g.endTime <= :now
        """)
    int expireExpiredGroups(@Param("now") LocalDateTime now);
}
