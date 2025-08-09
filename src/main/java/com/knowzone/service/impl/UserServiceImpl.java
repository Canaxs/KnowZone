package com.knowzone.service.impl;

import com.knowzone.config.security.CustomUserDetails;
import com.knowzone.dto.OnboardingUpdateRequest;
import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;
import com.knowzone.dto.UserResponse;
import com.knowzone.exception.UserNotFoundException;
import com.knowzone.exception.UsernameAlreadyExistsException;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserCreateResponse create(UserCreateRequest userCreateRequest) {
        log.info("Creating new user with username: {}", userCreateRequest.getUsername());
        
        Optional<User> existingUser = userRepository.findByUsername(userCreateRequest.getUsername());
        if (existingUser.isPresent()) {
            log.warn("Username already exists: {}", userCreateRequest.getUsername());
            throw new UsernameAlreadyExistsException("Username '" + userCreateRequest.getUsername() + "' is already taken.");
        }
        
        User user = userRepository.save(User.builder()
                .username(userCreateRequest.getUsername())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .email(userCreateRequest.getEmail())
                .createdAt(LocalDateTime.now())
                .build());

        log.info("User created successfully with ID: {}", user.getId());
        return UserCreateResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    public UserResponse findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        User user = userRepository.findById(id).get();
        return UserResponse.builder()
                .username(user.getUsername())
                .isActive(user.getIsActive())
                .build();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {
        log.info("Updating user with ID: {}", id);
        
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            log.warn("User not found for update with ID: {}", id);
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        User updatedUser = existingUser.get();
        updatedUser.setUsername(user.getUsername());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setInterests(user.getInterests());
        updatedUser.setAgeRange(user.getAgeRange());
        updatedUser.setIsActive(user.getIsActive());
        
        User savedUser = userRepository.save(updatedUser);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion with ID: {}", id);
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("Checking if user exists by username: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateOnboardingInfo(OnboardingUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUserId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setInterests(request.getInterests());
        user.setHobbies(request.getHobbies());
        user.setIdealPersonTraits(request.getIdealPersonTraits());
        user.setGender(request.getGender());
        userRepository.save(user);
    }
}
