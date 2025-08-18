package com.knowzone.service.impl;

import com.knowzone.config.security.CustomUserDetails;
import com.knowzone.enums.Gender;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.MatchRepository;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.LocationService;
import com.knowzone.service.MatchingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;
    private final MatchingService matchingService;

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public boolean updateUserLocation(double latitude, double longitude) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(Long.valueOf(userDetails.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setLocationUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return findNearbyUsersAndMatch(user);
    }

    @Override
    public List<User> findNearbyUsers(User user, double radiusKm) {
        double lat = user.getLatitude();
        double lon = user.getLongitude();
        /*
        double latDelta = radiusKm / 111.0;

        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lon - lonDelta;
        double maxLon = lon + lonDelta;

        return userRepository.findNearbyUsersInBoundingBox(
                minLat, maxLat, minLon, maxLon, user.getId()
        );
         */
        return userRepository.findNearbyUsersByGenderHaversine(lat, lon, radiusKm, user.getId(),determineTargetGender(user.getGender()));
    }

    private boolean findNearbyUsersAndMatch(User user) {
        List<User> nearbyUsers = findNearbyUsers(user, 1.0);

        for (User nearbyUser : nearbyUsers) {
            boolean matchCreated = matchingService.evaluateAndCreateMatch(user, nearbyUser);

            if (matchCreated) {
                log.info("Match created for user {} with nearby user {}, stopping search",
                        user.getId(), nearbyUser.getId());
                return true;
            }
        }
        return false;
    }

    private Gender determineTargetGender(Gender userGender) {
        if (userGender == null) {
            return null;
        }

        return switch (userGender) {
            case MALE -> Gender.FEMALE;
            case FEMALE -> Gender.MALE;
        };
    }
}
