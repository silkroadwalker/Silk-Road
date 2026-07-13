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
                Ad selected = adListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.setSelectedAd(selected);
                    SceneManager.switchScene("/fxml/ad-details-view.fxml");
                }
            }
        });

        loadMyAds();
    }

    private void loadMyAds() {
        try {
            List<Ad> ads = ApiClient.getMyAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
        } catch (Exception e) {
            statusLabel.setText("Could not load your ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
