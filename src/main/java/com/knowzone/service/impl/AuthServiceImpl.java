package com.knowzone.service.impl;

import com.knowzone.dto.LoginResponse;
import com.knowzone.dto.UserDTO;
import com.knowzone.exception.InvalidPasswordException;
import com.knowzone.exception.UserNotFoundException;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.AuthService;
import com.knowzone.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(UserDTO userDTO) {
        log.info("Login attempt for username: {}", userDTO.getUsername());
        
        Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if(passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername(), String.valueOf(user.getId()));
                log.info("Login successful for user: {}", user.getUsername());
                return LoginResponse.builder()
                        .token(token)
                        .onboardingCompleted(!userOpt.get().getInterests().isEmpty())
                        .username(userOpt.get().getUsername())
                        .email(userOpt.get().getEmail())
                        .userId(userOpt.get().getId())
                        .gender(userOpt.get().getGender())
                        .build();
            }
            else {
                log.warn("Invalid password for username: {}", userDTO.getUsername());
                throw new InvalidPasswordException("Invalid password for username '" + userDTO.getUsername() + "'.");
            }
        }
        else {
            log.warn("User not found for login attempt: {}", userDTO.getUsername());
            throw new UserNotFoundException("User with username '" + userDTO.getUsername() + "' not found.");
        }
    }
}
