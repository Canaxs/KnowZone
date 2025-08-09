package com.knowzone.persistence.repository;

import com.knowzone.enums.Gender;
import com.knowzone.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    List<User> findByIsActiveTrue();
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.latitude IS NOT NULL AND u.longitude IS NOT NULL AND u.latitude BETWEEN :minLat AND :maxLat AND u.longitude BETWEEN :minLon AND :maxLon AND u.id <> :userId")
    List<User> findNearbyUsersInBoundingBox(@Param("minLat") double minLat,
                                            @Param("maxLat") double maxLat,
                                            @Param("minLon") double minLon,
                                            @Param("maxLon") double maxLon,
                                            @Param("userId") Long userId);

    @Query("""
            SELECT u FROM User u
            WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL
            AND u.id <> :userId
            AND u.gender = :targetGender
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(u.latitude)) *
                    cos(radians(u.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(u.latitude))
                )
            ) <= :radiusKm
           """)
    List<User> findNearbyUsersByGenderHaversine(@Param("lat") double lat,
                                                @Param("lon") double lon,
                                                @Param("radiusKm") double radiusKm,
                                                @Param("userId") Long userId,
                                                @Param("targetGender") Gender gender);
}
