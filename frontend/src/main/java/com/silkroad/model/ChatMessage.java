package com.silkroad.model;

public class ChatMessage {
    private String senderUsername;
    private String content;
    private String sentAt;

    public String getSenderUsername() { return senderUsername; }
    public String getContent() { return content; }
    public String getSentAt() { return sentAt; }

    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setContent(String content) { this.content = content; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
