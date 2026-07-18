package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class MyAdsController {

    @FXML
    private ListView<Ad> adListView;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        adListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null : ad.getTitle() + " - $" + ad.getPrice() + " [" + ad.getStatus() + "]");
            }
        });

        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openEditFor(adListView.getSelectionModel().getSelectedItem());
            }
        });

        loadMyAds();
    }

    private void loadMyAds() {
        try {
            List<Ad> ads = ApiClient.getMyAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
            statusLabel.setText(ads.isEmpty() ? "You haven't posted any ads yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load your ads: " + e.getMessage()
                    + "\n(Ask your backend teammate to add GET /api/ads/my — it's not implemented yet.)");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadMyAds();
    }

    @FXML
    private void handleEdit() {
        openEditFor(adListView.getSelectionModel().getSelectedItem());
    }

    private void openEditFor(Ad selected) {
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        SceneManager.setSelectedAd(selected);
        SceneManager.switchScene("/fxml/edit-ad-view.fxml");
    }

    @FXML
    private void handleMarkSold() {
        Ad selected = adListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        if (!Dialogs.confirm("Mark \"" + selected.getTitle() + "\" as sold?")) {
            return;
        }
        try {
            ApiClient.markAdSold(selected.getId());
            statusLabel.setText("Marked as sold.");
            loadMyAds();
        } catch (Exception e) {
            statusLabel.setText("Could not update status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Ad selected = adListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        if (!Dialogs.confirm("Delete \"" + selected.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        try {
            ApiClient.deleteAd(selected.getId());
            statusLabel.setText("Ad deleted.");
            loadMyAds();
        } catch (Exception e) {
            statusLabel.setText("Could not delete ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
