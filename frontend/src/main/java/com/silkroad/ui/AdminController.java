package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AdminController {

    @FXML
    private ListView<Ad> pendingListView;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea reasonField;

    @FXML
    public void initialize() {
        pendingListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null
                        : ad.getTitle() + " - $" + ad.getPrice() + " (seller: " + ad.getSellerUsername() + ")");
            }
        });

        loadPendingAds();
    }

    private void loadPendingAds() {
        try {
            List<Ad> pending = ApiClient.getPendingAds();
            pendingListView.setItems(FXCollections.observableArrayList(pending));
            statusLabel.setText(pending.isEmpty() ? "No ads pending review." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load pending ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApprove() {
        Ad selected = pendingListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        try {
            ApiClient.approveAd(selected.getId());
            statusLabel.setText("Ad approved.");
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not approve ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReject() {
        Ad selected = pendingListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        String reason = reasonField.getText();
        if (reason == null || reason.isBlank()) {
            statusLabel.setText("A rejection reason is required.");
            return;
        }
        try {
            ApiClient.rejectAd(selected.getId(), reason);
            statusLabel.setText("Ad rejected.");
            reasonField.clear();
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not reject ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
