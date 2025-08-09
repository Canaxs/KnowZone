package com.knowzone.service;

import com.knowzone.persistence.entity.Match;

public interface FCMNotificationService {
    void sendMatchNotification(Long userId, Match match);
    void updateFcmToken(Long userId, String fcmToken);
} 