package com.knowzone.controller;

import com.knowzone.dto.OnboardingUpdateRequest;
import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;
import com.knowzone.dto.UserResponse;
import com.knowzone.service.UserService;
import com.knowzone.service.FCMNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FCMNotificationService fcmNotificationService;

    @GetMapping("/test")
    private String test() {
        return "test";
    }

    @PostMapping("/create")
    private ResponseEntity<UserCreateResponse> create(@RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(userService.create(userCreateRequest));
    }

    @GetMapping("/findById/{userId}")
    private ResponseEntity<UserResponse> findById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PatchMapping("/onboarding")
    public ResponseEntity<Void> updateOnboardingInfo(@RequestBody OnboardingUpdateRequest request) {
        userService.updateOnboardingInfo(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            @PathVariable Long userId,
            @RequestBody String fcmToken) {
        fcmNotificationService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok("FCM token updated");
    }
}
