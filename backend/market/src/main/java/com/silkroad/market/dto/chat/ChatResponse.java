package com.silkroad.market.dto.chat;

import java.util.List;

public class ChatResponse {

    private Long chatId;

    private Long advertisementId;

    private String advertisementTitle;

    private List<MessageResponse> messages;

    public ChatResponse() {
    }

    public ChatResponse(
            Long chatId,
            Long advertisementId,
            String advertisementTitle,
            List<MessageResponse> messages) {

        this.chatId = chatId;
        this.advertisementId = advertisementId;
        this.advertisementTitle = advertisementTitle;
        this.messages = messages;
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

    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }
}