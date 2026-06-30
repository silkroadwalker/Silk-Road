package com.silkroad.market.dto;

import com.silkroad.market.entity.Role;

public class AuthResponse {

    private String message;
    private String username;
    private Role role;
    private String token;

    public AuthResponse(String message, String username, Role role, String token) {
        this.message = message;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}