package com.knowzone.controller;

import com.knowzone.dto.UserCreateRequest;
import com.knowzone.dto.UserCreateResponse;
import com.knowzone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    private String test() {
        return "test";
    }

    @PostMapping("/create")
    private ResponseEntity<UserCreateResponse> create(@RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(userService.create(userCreateRequest));
    }
}
