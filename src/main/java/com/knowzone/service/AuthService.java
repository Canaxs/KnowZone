package com.knowzone.service;

import com.knowzone.dto.LoginResponse;
import com.knowzone.dto.UserDTO;

public interface AuthService {
    LoginResponse login(UserDTO userDTO);
}
