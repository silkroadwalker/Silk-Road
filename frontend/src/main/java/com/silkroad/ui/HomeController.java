package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.Category;
import com.silkroad.model.City;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.util.Comparator;
import java.util.List;

/**
 * main controller for the home view. displays the list of active ads,
 * provides search and filter functionality, and serves as the primary
 * navigation hub for regular users and admins.
 */
public class HomeController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button adminButton;
    @FXML
    private TextField searchField;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML
    private ComboBox<City> cityFilterBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private ComboBox<String> sortBox;
    @FXML
    private FlowPane adsFlowPane;
    @FXML
    private Label statusLabel;

    private static final String SORT_NEWEST = "Newest first";
    private static final String SORT_CHEAPEST = "Cheapest first";
    private static final String SORT_EXPENSIVE = "Most expensive first";
    private static final String CATEGORY_PLACEHOLDER = "Category";

    /** id of the category (or subcategory) currently selected in the flyout menu, or null for "all". */
    private Long selectedCategoryId;

    /**
     * called by javafx after fxml loading. sets up the welcome message,
     * shows the admin button if applicable, loads filter dropdowns,
     * and fetches the initial list of ads.
     */
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getUsername() + "!");

        if ("ADMIN".equalsIgnoreCase(Session.getRole())) {
            adminButton.setVisible(true);
            adminButton.setManaged(true);
        }

        sortBox.setItems(FXCollections.observableArrayList(SORT_NEWEST, SORT_CHEAPEST, SORT_EXPENSIVE));

        loadCityFilterOptions();
        loadCategoryMenu();
        loadAds();
    }

    private void loadCityFilterOptions() {
        try {
            List<City> cities = ApiClient.getCities();
            cityFilterBox.setItems(FXCollections.observableArrayList(cities));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * builds the category flyout menu: top-level categories that have
     * subcategories become a Menu whose children fly out to the side on
     * hover; leaf categories become a plain, directly-clickable MenuItem.
     * an "All categories" item at the top clears the filter.
     */
    private void loadCategoryMenu() {
        categoryMenuButton.getItems().clear();

        MenuItem allItem = new MenuItem("All categories");
        allItem.setOnAction(e -> selectCategory(null, CATEGORY_PLACEHOLDER));
        categoryMenuButton.getItems().add(allItem);
        categoryMenuButton.getItems().add(new SeparatorMenuItem());

        try {
            List<Category> categories = ApiClient.getCategories();
            for (Category category : categories) {
                categoryMenuButton.getItems().add(buildCategoryMenuItem(category));
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load categories: " + e.getMessage());
        }
    }

    /**
     * builds a single top-level menu entry: a leaf MenuItem if the category
     * has no children, or a Menu (JavaFX automatically shows its children as
     * a flyout submenu when the user hovers over it) if it does.
     */
    private MenuItem buildCategoryMenuItem(Category category) {
        if (!category.isHasChildren()) {
            MenuItem item = new MenuItem(category.getName());
            item.setOnAction(e -> selectCategory(category.getId(), category.getName()));
            return item;
        }

        Menu categoryMenu = new Menu(category.getName());

        MenuItem wholeCategoryItem = new MenuItem("All " + category.getName());
        wholeCategoryItem.setOnAction(e -> selectCategory(category.getId(), category.getName()));
        categoryMenu.getItems().add(wholeCategoryItem);
        categoryMenu.getItems().add(new SeparatorMenuItem());

        try {
            for (Category sub : ApiClient.getSubcategories(category.getId())) {
                MenuItem subItem = new MenuItem(sub.getName());
                subItem.setOnAction(e -> selectCategory(sub.getId(), category.getName() + " \u203a " + sub.getName()));
                categoryMenu.getItems().add(subItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryMenu;
    }

    private void selectCategory(Long categoryId, String buttonLabel) {
        selectedCategoryId = categoryId;
        categoryMenuButton.setText(buttonLabel);
    }

    /**
     * fetches all active ads from the backend and displays them as cards.
     * shows an error message if the request fails.
     */
    private void loadAds() {
        try {
            List<Ad> ads = ApiClient.getAds();
            renderAds(ads);
            statusLabel.setText(ads.isEmpty() ? "No ads found." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAds(List<Ad> ads) {
        adsFlowPane.getChildren().clear();
        for (Ad ad : ads) {
            adsFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, false, null, () -> openAd(ad)));
        }
    }

    private void openAd(Ad ad) {
        SceneManager.setSelectedAd(ad);
        SceneManager.setViewingAsAdmin(false);
        SceneManager.setReturnScene("/fxml/home-view.fxml");
        SceneManager.switchScene("/fxml/ad-details-view.fxml");
    }

    /**
     * executes a search with the current filter values (keyword, category,
     * city, price range, and sort order). displays matching ads or a message
     * if no results are found. validation errors are shown on the status label.
     */
    @FXML
    private void handleSearch() {
        ApiClient.AdFilter filter = new ApiClient.AdFilter();
        filter.keyword = searchField.getText();

        if (selectedCategoryId != null) {
            filter.categoryId = selectedCategoryId;
        }

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
            renderAds(results);
            statusLabel.setText(results.isEmpty() ? "No ads match your search." : "");
        } catch (Exception e) {
            statusLabel.setText("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * resets all filter fields (search text, category, city, price range,
     * and sort order) and reloads the full list of ads.
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        selectedCategoryId = null;
        categoryMenuButton.setText(CATEGORY_PLACEHOLDER);
        cityFilterBox.getSelectionModel().clearSelection();
        minPriceField.clear();
        maxPriceField.clear();
        sortBox.getSelectionModel().clearSelection();
        loadAds();
    }

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