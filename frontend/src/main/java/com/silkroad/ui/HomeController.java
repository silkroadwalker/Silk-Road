package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HomeController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button adminButton;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<Ad> adListView;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getUsername() + "!");

        if ("ADMIN".equalsIgnoreCase(Session.getRole())) {
            // i'm not sure whether it'll work or not
            adminButton.setVisible(true);
            adminButton.setManaged(true);
        }

        adListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null : ad.getTitle() + " - $" + ad.getPrice());
            }
        });

        // ad detail on double click
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ad selected = adListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.setSelectedAd(selected);
                    SceneManager.switchScene("/fxml/ad-details-view.fxml");
                }
            }
        });

        loadAds();
    }

    private void loadAds() {
        try {
            List<Ad> ads = ApiClient.getAds();
            ObservableList<Ad> items = FXCollections.observableArrayList(ads);
            adListView.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        try {
            List<Ad> results = ApiClient.searchAds(query);
            adListView.setItems(FXCollections.observableArrayList(results));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCreateAd() {
        SceneManager.switchScene("/fxml/create-ad-view.fxml");
    }

    @FXML
    private void goToMyAds() {
        SceneManager.switchScene("/fxml/my-ads-view.fxml");
    }

    @FXML
    private void goToFavorites() {
        SceneManager.switchScene("/fxml/favorites-view.fxml");
    }

    @FXML
    private void goToMessages() {
        SceneManager.switchScene("/fxml/conversations-view.fxml");
    }

    @FXML
    private void goToAdmin() {
        SceneManager.switchScene("/fxml/admin-view.fxml");
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        SceneManager.switchScene("/fxml/login-view.fxml");
    }
}