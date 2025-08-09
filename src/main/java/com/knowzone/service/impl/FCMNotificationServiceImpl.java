package com.knowzone.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.knowzone.persistence.entity.Match;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.FCMNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMNotificationServiceImpl implements FCMNotificationService {
    
    private final UserRepository userRepository;
    
    @Override
    public void sendMatchNotification(Long userId, Match match) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                log.warn("FCM token not found for user: {}", userId);
                return;
            }

            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle("Yeni Eşleşme! 🎉")
                            .setBody("Sizinle uyumlu biri var! Hemen bakın.")
                            .build())
                    .putData("type", "NEW_MATCH")
                    .putData("matchId", match.getId().toString())
                    .putData("compatibilityScore", String.valueOf(match.getCompatibilityScore()))
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM notification sent to user {}: {}", userId, response);
            
        } catch (Exception e) {
            log.error("Error sending FCM notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    @Override
    public void updateFcmToken(Long userId, String fcmToken) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setFcmToken(fcmToken);
            userRepository.save(user);
            
            log.info("FCM token updated for user: {}", userId);
        } catch (Exception e) {
            log.error("Error updating FCM token for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error updating FCM token: " + e.getMessage());
        }
    }
} 