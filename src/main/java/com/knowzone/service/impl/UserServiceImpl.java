package com.knowzone.service.impl;

import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;
import com.knowzone.exception.UsernameAlreadyExistsException;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserCreateResponse create(UserCreateRequest userCreateRequest) {
        Optional<User> existingUser = userRepository.findByUsername(userCreateRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException("Username '" + userCreateRequest.getUsername() + "' is already taken.");
        }
        User user = userRepository.save(User.builder()
                .username(userCreateRequest.getUsername())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                        .email(userCreateRequest.getEmail())
                .build());

        return UserCreateResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

    }
}
