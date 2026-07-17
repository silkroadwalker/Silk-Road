package com.silkroad.model;

import java.util.List;

public class ChatDetail {
    private Long chatId;
    private Long advertisementId;
    private String advertisementTitle;
    private List<ChatMessage> messages;

    public Long getChatId() { return chatId; }
    public Long getAdvertisementId() { return advertisementId; }
    public String getAdvertisementTitle() { return advertisementTitle; }
    public List<ChatMessage> getMessages() { return messages; }

    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setAdvertisementId(Long advertisementId) { this.advertisementId = advertisementId; }
    public void setAdvertisementTitle(String advertisementTitle) { this.advertisementTitle = advertisementTitle; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
