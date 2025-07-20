package com.knowzone.service;

import com.knowzone.dto.UserDTO;

public interface AuthService {
    String login(UserDTO userDTO);
}
