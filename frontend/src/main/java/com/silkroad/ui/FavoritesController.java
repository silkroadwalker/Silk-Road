package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class FavoritesController {

    @FXML
    private ListView<Ad> favoritesListView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button removeButton;

    @FXML
    public void initialize() {
        favoritesListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null : ad.getTitle() + " - $" + ad.getPrice() + " (" + ad.getCity() + ")");
            }
        });

        favoritesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ad selected = favoritesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.setSelectedAd(selected);
                    SceneManager.switchScene("/fxml/ad-details-view.fxml");
                }
            }
        });

        loadFavorites();
    }

    private void loadFavorites() {
        try {
            List<Ad> favorites = ApiClient.getFavorites();
            favoritesListView.setItems(FXCollections.observableArrayList(favorites));
            statusLabel.setText(favorites.isEmpty() ? "You have no favorites yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load favorites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemoveFavorite() {
        Ad selected = favoritesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad to remove first.");
            return;
        }
        try {
            ApiClient.removeFavorite(selected.getId());
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
