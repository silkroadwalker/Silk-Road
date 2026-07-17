package com.silkroad.market.dto.chat;

import java.time.LocalDateTime;

public class ChatSummaryResponse {

    private Long chatId;

    private Long advertisementId;

    private String advertisementTitle;

    private String otherUsername;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    public ChatSummaryResponse() {
    }

    public ChatSummaryResponse(
            Long chatId,
            Long advertisementId,
            String advertisementTitle,
            String otherUsername,
            String lastMessage,
            LocalDateTime lastMessageTime) {

        this.chatId = chatId;
        this.advertisementId = advertisementId;
        this.advertisementTitle = advertisementTitle;
        this.otherUsername = otherUsername;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getAdvertisementTitle() {
        return advertisementTitle;
    }

    public void setAdvertisementTitle(String advertisementTitle) {
        this.advertisementTitle = advertisementTitle;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}