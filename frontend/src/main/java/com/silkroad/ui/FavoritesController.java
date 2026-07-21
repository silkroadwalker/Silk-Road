package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

/**
 * controller for the favorites view. displays the current user's list of
 * favorited ads as clickable cards, and provides a remove button for each.
 */
public class FavoritesController {

    @FXML
    private FlowPane favoritesFlowPane;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        loadFavorites();
    }

    /**
     * fetches the user's favorite ads from the backend and renders them
     * as cards. shows a message if the list is empty or if an error occurs.
     */
    private void loadFavorites() {
        try {
            List<Ad> favorites = ApiClient.getFavorites();
            renderFavorites(favorites);
            statusLabel.setText(favorites.isEmpty() ? "You have no favorites yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load favorites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * builds a card for each favorite ad with a remove button.
     * clicking the card opens the ad detail view.
     *
     * @param favorites the list of favorited ads to display
     */
    private void renderFavorites(List<Ad> favorites) {
        favoritesFlowPane.getChildren().clear();
        for (Ad ad : favorites) {
            Button removeButton = new Button("Remove");
            removeButton.getStyleClass().add("danger-button");
            removeButton.setOnAction(e -> handleRemoveFavorite(ad));

            favoritesFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, false, List.of(removeButton), () -> openAd(ad)));
        }
    }

    private void openAd(Ad ad) {
        SceneManager.setSelectedAd(ad);
        SceneManager.setViewingAsAdmin(false);
        SceneManager.setReturnScene("/fxml/home-view.fxml");
        SceneManager.switchScene("/fxml/ad-details-view.fxml");
    }

    private void handleRemoveFavorite(Ad ad) {
        try {
            ApiClient.removeFavorite(ad.getId());
            loadFavorites();
        } catch (Exception e) {
            statusLabel.setText("Could not remove favorite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}