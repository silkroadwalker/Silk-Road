package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.ChatDetail;
import com.silkroad.model.ChatMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatDetailController {

    @FXML private Label adTitleLabel;
    @FXML private ListView<ChatMessage> messagesListView;
    @FXML private TextField messageField;
    @FXML private Label statusLabel;

    private Long chatId;

    @FXML
    public void initialize() {
        chatId = SceneManager.getSelectedChatId();

        messagesListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    boolean mine = message.getSenderUsername() != null
                            && message.getSenderUsername().equalsIgnoreCase(Session.getUsername());
                    String who = mine ? "You" : message.getSenderUsername();
                    setText("[" + message.getSentAt() + "] " + who + ": " + message.getContent());
                }
            }
        });

        if (chatId == null) {
            statusLabel.setText("No conversation selected.");
            return;
        }

        loadChat();
    }

    private void loadChat() {
        try {
            ChatDetail detail = ApiClient.getChat(chatId);
            adTitleLabel.setText(detail.getAdvertisementTitle());
            messagesListView.setItems(FXCollections.observableArrayList(detail.getMessages()));
            messagesListView.scrollTo(messagesListView.getItems().size() - 1);
        } catch (Exception e) {
            statusLabel.setText("Could not load conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        String text = messageField.getText();
        if (text == null || text.isBlank()) {
            statusLabel.setText("Message cannot be empty.");
            return;
        }
        try {
            ApiClient.replyToChat(chatId, text);
            messageField.clear();
            statusLabel.setText("");
            loadChat();
        } catch (Exception e) {
            statusLabel.setText("Could not send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/conversations-view.fxml");
    }
}
