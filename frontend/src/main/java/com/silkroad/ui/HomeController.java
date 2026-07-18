package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.Category;
import com.silkroad.model.City;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Comparator;
import java.util.List;

public class HomeController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button adminButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Category> categoryFilterBox;
    @FXML
    private ComboBox<City> cityFilterBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private ComboBox<String> sortBox;
    @FXML
    private ListView<Ad> adListView;
    @FXML
    private Label statusLabel;

    private static final String SORT_NEWEST = "Newest first";
    private static final String SORT_CHEAPEST = "Cheapest first";
    private static final String SORT_EXPENSIVE = "Most expensive first";

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getUsername() + "!");

        if ("ADMIN".equalsIgnoreCase(Session.getRole())) {
            adminButton.setVisible(true);
            adminButton.setManaged(true);
        }

        adListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null
                        : ad.getTitle() + " - $" + ad.getPrice() + " (" + ad.getCity() + ", " + ad.getCategory() + ")");
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

        sortBox.setItems(FXCollections.observableArrayList(SORT_NEWEST, SORT_CHEAPEST, SORT_EXPENSIVE));

        loadFilterOptions();
        loadAds();
    }

    private void loadFilterOptions() {
        try {
            List<Category> categories = ApiClient.getCategories();
            categoryFilterBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<City> cities = ApiClient.getCities();
            cityFilterBox.setItems(FXCollections.observableArrayList(cities));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAds() {
        try {
            List<Ad> ads = ApiClient.getAds();
            adListView.setItems(FXCollections.observableArrayList(ads));
            statusLabel.setText(ads.isEmpty() ? "No ads found." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        ApiClient.AdFilter filter = new ApiClient.AdFilter();
        filter.keyword = searchField.getText();

        Category category = categoryFilterBox.getValue();
        if (category != null) filter.categoryId = category.getId();

        City city = cityFilterBox.getValue();
        if (city != null) filter.cityId = city.getId();

        String minPrice = minPriceField.getText();
        String maxPrice = maxPriceField.getText();

        if (minPrice != null && !minPrice.isBlank()) {
            if (!isValidNumber(minPrice)) {
                statusLabel.setText("Min price must be a number.");
                return;
            }
            filter.minPrice = minPrice;
        }
        if (maxPrice != null && !maxPrice.isBlank()) {
            if (!isValidNumber(maxPrice)) {
                statusLabel.setText("Max price must be a number.");
                return;
            }
            filter.maxPrice = maxPrice;
        }

        try {
            List<Ad> results = ApiClient.searchAds(filter);
            results = applySort(results);
            adListView.setItems(FXCollections.observableArrayList(results));
            statusLabel.setText(results.isEmpty() ? "No ads match your search." : "");
        } catch (Exception e) {
            statusLabel.setText("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilterBox.getSelectionModel().clearSelection();
        cityFilterBox.getSelectionModel().clearSelection();
        minPriceField.clear();
        maxPriceField.clear();
        sortBox.getSelectionModel().clearSelection();
        loadAds();
    }

    /**
     * The backend doesn't support a sort parameter yet, so we sort the returned
     * page client-side. Ads don't carry a createdAt field in the summary DTO, so
     * "newest first" falls back to reverse-id order (higher id = created later).
     */
    private List<Ad> applySort(List<Ad> ads) {
        String sort = sortBox.getValue();
        if (sort == null) return ads;

        if (SORT_CHEAPEST.equals(sort)) {
            ads.sort(Comparator.comparingDouble(Ad::getPrice));
        } else if (SORT_EXPENSIVE.equals(sort)) {
            ads.sort(Comparator.comparingDouble(Ad::getPrice).reversed());
        } else if (SORT_NEWEST.equals(sort)) {
            ads.sort(Comparator.comparing(Ad::getId).reversed());
        }
        return ads;
    }

    private boolean isValidNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
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
