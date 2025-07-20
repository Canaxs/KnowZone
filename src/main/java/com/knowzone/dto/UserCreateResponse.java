package com.knowzone.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateResponse {
    private Long userId;
    private String username;
    private String email;
}
