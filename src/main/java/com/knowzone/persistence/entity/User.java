package com.knowzone.persistence.entity;

import com.knowzone.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @ElementCollection
    @CollectionTable(name = "user_interests")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_hobbies")
    private Set<String> hobbies = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_ideal_person_traits")
    private Set<String> idealPersonTraits = new HashSet<>();

    @Column(name = "age_range")
    private String ageRange;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

}
