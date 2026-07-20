package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.ChatSummary;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConversationsController {

    @FXML
    private VBox conversationsBox;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        loadConversations();
    }

    private void loadConversations() {
        try {
            List<ChatSummary> chats = ApiClient.getChats();
            renderConversations(chats);
            statusLabel.setText(chats.isEmpty() ? "You have no conversations yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load conversations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderConversations(List<ChatSummary> chats) {
        conversationsBox.getChildren().clear();
        for (ChatSummary chat : chats) {
            conversationsBox.getChildren().add(buildRow(chat));
        }
    }

    private HBox buildRow(ChatSummary chat) {
        var avatar = UiComponents.avatar(chat.getOtherUsername(), 40);

        Label name = new Label(chat.getOtherUsername());
        name.getStyleClass().add("conversation-name");

        Label adTitle = new Label(chat.getAdvertisementTitle());
        adTitle.getStyleClass().add("conversation-ad");

        Label preview = new Label(chat.getLastMessage() == null || chat.getLastMessage().isBlank()
                ? "No messages yet" : chat.getLastMessage());
        preview.getStyleClass().add("conversation-preview");
        preview.setWrapText(false);

        VBox textBox = new VBox(2, name, adTitle, preview);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(formatTimestamp(chat.getLastMessageTime()));
        time.getStyleClass().add("conversation-time");

        HBox row = new HBox(12, avatar, textBox, spacer, time);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("conversation-row");
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setOnMouseClicked(event -> {
            SceneManager.setSelectedChatId(chat.getChatId());
            SceneManager.switchScene("/fxml/chat-detail-view.fxml");
        });

        return row;
    }

    /**
     * The backend sends a plain LocalDateTime (no zone/offset), so it must be
     * parsed as a LocalDateTime rather than an Instant.
     */
    private String formatTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) return "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawTimestamp);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return rawTimestamp;
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
