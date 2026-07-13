package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class CreateAdController {
    @FXML private Label headerLabel;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<String> cityComboBox; // TODO: swap for ComboBox<City> once GET /api/cities exists
    @FXML private Button submitButton;

    @FXML
    public void initialize() {
        loadCategories();

        cityComboBox.setItems(FXCollections.observableArrayList("(city list not available yet)"));
        cityComboBox.getSelectionModel().selectFirst();
        cityComboBox.setDisable(true);
    }

    private void loadCategories() {
        try {
            List<Category> categories = ApiClient.getCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String priceText = priceField.getText();
        Category selectedCategory = categoryComboBox.getValue();

        if (isBlank(title) || isBlank(description) || isBlank(priceText)) {
            showError("Please fill in title, description, and price.");
            return;
        }

        if (selectedCategory == null) {
            showError("Please select a category.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            return;
        }

        // TODO: i will remove this once city selection works
        showError("City selection isn't available yet. Ask your backend teammate to add GET /api/cities first.");

        /* once a real cityId is selectable:
        try {
            ApiClient.createAd(title, description, priceText, selectedCategory.getId(), selectedCityId);
            SceneManager.switchScene("/fxml/home-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to create ad: " + e.getMessage());
        }
        */
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}