package com.knowzone.dto;

import java.util.Set;

import com.knowzone.enums.Gender;
import lombok.Data;

@Data
public class OnboardingUpdateRequest {
    private Set<String> interests;
    private Set<String> hobbies;
    private Set<String> idealPersonTraits;
    private Gender gender;
} 