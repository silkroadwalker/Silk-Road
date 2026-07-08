package com.silkroad.market.dto.chat;

import jakarta.validation.constraints.NotBlank;

public class ReplyMessageRequest {

    @NotBlank
    private String message;

    public ReplyMessageRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}