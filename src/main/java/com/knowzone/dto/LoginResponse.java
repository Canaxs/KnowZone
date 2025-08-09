package com.knowzone.dto;

import com.knowzone.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private Boolean onboardingCompleted;
    private Gender gender;
}
