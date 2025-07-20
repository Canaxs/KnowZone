package com.knowzone.service;

import com.knowzone.persistence.entity.User;

import java.util.List;

public interface LocationService {
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);
    void updateUserLocation(Long userId, double latitude, double longitude);
    List<User> findNearbyUsers(User user, double radiusKm);
}
