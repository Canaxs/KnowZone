package com.knowzone.service.impl;

import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.MatchRepository;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.LocationService;
import com.knowzone.service.MatchingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
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
    public void updateUserLocation(Long userId, double latitude, double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setLocationUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        findNearbyUsersAndMatch(user);
    }

    @Override
    public List<User> findNearbyUsers(User user, double radiusKm) {
        List<User> allActiveUsers = userRepository.findByIsActiveTrue();
        List<User> nearbyUsers = new ArrayList<>();

        for (User otherUser : allActiveUsers) {
            if (!otherUser.getId().equals(user.getId())
                    && otherUser.getLatitude() != null
                    && otherUser.getLongitude() != null) {

                double distance = calculateDistance(
                        user.getLatitude(), user.getLongitude(),
                        otherUser.getLatitude(), otherUser.getLongitude()
                );

                if (distance <= radiusKm) {
                    nearbyUsers.add(otherUser);
                }
            }
        }

        return nearbyUsers;
    }

    private void findNearbyUsersAndMatch(User user) {
        List<User> nearbyUsers = findNearbyUsers(user, 1.0);

        for (User nearbyUser : nearbyUsers) {
            matchingService.evaluateAndCreateMatch(user, nearbyUser);
        }
    }
}
