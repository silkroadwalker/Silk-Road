package com.silkroad.model;

public class ChatSummary {
    private Long chatId;
    private Long advertisementId;
    private String advertisementTitle;
    private String otherUsername;
    private String lastMessage;
    private String lastMessageTime;

    public Long getChatId() { return chatId; }
    public Long getAdvertisementId() { return advertisementId; }
    public String getAdvertisementTitle() { return advertisementTitle; }
    public String getOtherUsername() { return otherUsername; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }

    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setAdvertisementId(Long advertisementId) { this.advertisementId = advertisementId; }
    public void setAdvertisementTitle(String advertisementTitle) { this.advertisementTitle = advertisementTitle; }
    public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
