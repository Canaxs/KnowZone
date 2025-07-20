package com.knowzone.service;

import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;

public interface UserService {
    UserCreateResponse create(UserCreateRequest userCreateRequest);
}
