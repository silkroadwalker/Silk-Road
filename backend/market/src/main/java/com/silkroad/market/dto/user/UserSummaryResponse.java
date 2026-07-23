package com.silkroad.market.dto.user;

import com.silkroad.market.entity.UserStatus;

public class UserSummaryResponse {

    private Long id;
    private String username;
    private String fullName;
    private UserStatus status;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(
            Long id,
            String username,
            String fullName,
            UserStatus status) {

        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}