package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

public class MyAdsController {

    @FXML
    private FlowPane myAdsFlowPane;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        loadMyAds();
    }

    private void loadMyAds() {
        try {
            List<Ad> ads = ApiClient.getMyAds();
            renderAds(ads);
            statusLabel.setText(ads.isEmpty() ? "You haven't posted any ads yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load your ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAds(List<Ad> ads) {
        myAdsFlowPane.getChildren().clear();
        for (Ad ad : ads) {
            List<Node> actions = new ArrayList<>();

            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> openEditFor(ad));
            actions.add(editButton);

            if (!"SOLD".equalsIgnoreCase(ad.getStatus())) {
                Button markSoldButton = new Button("Mark Sold");
                markSoldButton.setOnAction(e -> handleMarkSold(ad));
                actions.add(markSoldButton);
            }

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> handleDelete(ad));
            actions.add(deleteButton);

            myAdsFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, true, actions, () -> openEditFor(ad)));
        }
    }

    @FXML
    private void handleRefresh() {
        loadMyAds();
    }

    private void openEditFor(Ad selected) {
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        SceneManager.setSelectedAd(selected);
        SceneManager.switchScene("/fxml/edit-ad-view.fxml");
    }

    private void handleMarkSold(Ad selected) {
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

    private void handleDelete(Ad selected) {
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
