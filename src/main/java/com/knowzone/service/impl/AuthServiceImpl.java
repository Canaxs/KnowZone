package com.knowzone.service.impl;

import com.knowzone.dto.UserDTO;
import com.knowzone.exception.InvalidPasswordException;
import com.knowzone.exception.UserNotFoundException;
import com.knowzone.persistence.entity.User;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.AuthService;
import com.knowzone.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public String login(UserDTO userDTO) {
        Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());
        if (userOpt.isPresent()) {
            if(passwordEncoder.matches(userDTO.getPassword(), userOpt.get().getPassword())) {
                return jwtUtil.generateToken(userOpt.get().getUsername(), String.valueOf(userOpt.get().getId()));
            }
            else {
                throw new InvalidPasswordException("Invalid password for username '" + userDTO.getUsername() + "'.");
            }
        }
        else {
            throw new UserNotFoundException("User with username '" + userDTO.getUsername() + "' not found.");
        }
    }
}
