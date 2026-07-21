package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.ChatDetail;
import com.silkroad.model.ChatMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * controller for the chat detail view. displays all messages of a conversation,
 * distinguishes between own and other user's messages with bubble styling,
 * and allows the user to reply.
 */
public class ChatDetailController {

    @FXML
    private Label adTitleLabel;
    @FXML
    private ScrollPane messagesScroll;
    @FXML
    private VBox messagesBox;
    @FXML
    private TextField messageField;
    @FXML
    private Label statusLabel;

    private Long chatId;

    private static final double BUBBLE_MAX_WIDTH = 420;

    /**
     * called by javafx after fxml loading. retrieves the selected chat id
     * from the scene manager and triggers the conversation load.
     */
    @FXML
    public void initialize() {
        chatId = SceneManager.getSelectedChatId();

        if (chatId == null) {
            statusLabel.setText("No conversation selected.");
            return;
        }

        loadChat();
    }

    /**
     * fetches the complete conversation from the server and renders
     * all messages in the chat bubble view. shows an error if the
     * request fails.
     */
    private void loadChat() {
        try {
            ChatDetail detail = ApiClient.getChat(chatId);
            adTitleLabel.setText(detail.getAdvertisementTitle());
            renderMessages(detail);
        } catch (Exception e) {
            statusLabel.setText("Could not load conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * clears the message container and builds a message bubble row
     * for every message in the conversation. the scroll pane is then
     * scrolled to the bottom.
     *
     * @param detail the chat detail containing the message list
     */
    private void renderMessages(ChatDetail detail) {
        messagesBox.getChildren().clear();

        if (detail.getMessages() != null) {
            for (ChatMessage message : detail.getMessages()) {
                messagesBox.getChildren().add(buildBubbleRow(message));
            }
        }

        Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }

    private HBox buildBubbleRow(ChatMessage message) {
        boolean mine = message.getSenderUsername() != null
                && message.getSenderUsername().equalsIgnoreCase(Session.getUsername());

        Label text = new Label(message.getContent());
        text.getStyleClass().add("bubble-text");
        text.setWrapText(true);
        text.setMaxWidth(BUBBLE_MAX_WIDTH - 26);

        Label time = new Label(formatTimestamp(message.getSentAt()));
        time.getStyleClass().add("bubble-time");

        VBox bubble = new VBox(4, text, time);
        bubble.setMaxWidth(BUBBLE_MAX_WIDTH);
        bubble.getStyleClass().add(mine ? "bubble-mine" : "bubble-theirs");
        if (mine) {
            time.setStyle("-fx-text-fill: rgba(255,255,255,0.75);");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(8);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        if (mine) {
            row.getChildren().addAll(spacer, bubble);
        } else {
            row.getChildren().addAll(bubble, spacer);
        }
        return row;
    }

    /** Backend sends a plain LocalDateTime (no zone), so parse it as one. */
    private String formatTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) return "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawTimestamp);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return rawTimestamp;
        }
    }

    /**
     * sends the entered message to the server and reloads the conversation
     * to display the new message.
     */
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

    /**
     * returns to the conversation list view.
     */
    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/conversations-view.fxml");
    }
}