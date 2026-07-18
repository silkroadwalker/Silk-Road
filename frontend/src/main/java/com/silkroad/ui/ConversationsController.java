package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.ChatSummary;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class ConversationsController {

    @FXML
    private ListView<ChatSummary> conversationsListView;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        conversationsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatSummary chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setText(null);
                } else {
                    setText(chat.getOtherUsername() + " - " + chat.getAdvertisementTitle()
                            + "\n" + chat.getLastMessage()
                            + "\n" + formatTimestamp(chat.getLastMessageTime()));
                }
            }
        });

        conversationsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ChatSummary selected = conversationsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.setSelectedChatId(selected.getChatId());
                    SceneManager.switchScene("/fxml/chat-detail-view.fxml");
                }
            }
        });

        loadConversations();
    }

    private String formatTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) return "";
        try {
            java.time.Instant instant = java.time.Instant.parse(rawTimestamp);
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm")
                            .withZone(java.time.ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (Exception e) {
            return rawTimestamp;
        }
    }

    private void loadConversations() {
        try {
            List<ChatSummary> chats = ApiClient.getChats();
            conversationsListView.setItems(FXCollections.observableArrayList(chats));
            statusLabel.setText(chats.isEmpty() ? "You have no conversations yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load conversations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
