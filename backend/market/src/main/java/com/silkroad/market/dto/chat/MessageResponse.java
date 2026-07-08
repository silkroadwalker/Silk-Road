package com.silkroad.market.dto.chat;

import java.time.LocalDateTime;

public class MessageResponse {

    private String senderUsername;

    private String content;

    private LocalDateTime sentAt;

    public MessageResponse() {
    }

    public MessageResponse(
            String senderUsername,
            String content,
            LocalDateTime sentAt) {

        this.senderUsername = senderUsername;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}