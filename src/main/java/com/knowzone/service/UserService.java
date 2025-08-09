package com.knowzone.service;

import com.knowzone.dto.OnboardingUpdateRequest;
import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;
import com.knowzone.dto.UserResponse;
import com.knowzone.persistence.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserCreateResponse create(UserCreateRequest userCreateRequest);
    
    UserResponse findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    List<User> findAll();
    
    User updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    void updateOnboardingInfo(OnboardingUpdateRequest request);
}
