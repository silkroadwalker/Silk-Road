package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.AdDetail;
import com.silkroad.model.Category;
import com.silkroad.model.City;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * controller for the edit ad view. loads the selected ad's current data
 * into the form, allows the user to modify title, description, price,
 * category, and city, and submits the changes to the backend.
 */
public class EditAdController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label statusLabel;

    private Ad editingAd;

    /**
     * called by javafx after fxml loading. retrieves the selected ad from
     * the scene manager, populates category and city dropdowns, and fills
     * the form fields with the ad's current data.
     */
    @FXML
    public void initialize() {
        editingAd = SceneManager.getSelectedAd();
        if (editingAd == null) {
            statusLabel.setText("No ad selected.");
            return;
        }

        loadCategoriesAndCities();
        prefillForm();
    }

    private void loadCategoriesAndCities() {
        try {
            List<Category> categories = ApiClient.getCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<City> cities = ApiClient.getCities();
            cityComboBox.setItems(FXCollections.observableArrayList(cities));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prefillForm() {
        titleField.setText(editingAd.getTitle());
        priceField.setText(String.valueOf(editingAd.getPrice()));
        selectByName(categoryComboBox, editingAd.getCategory());
        selectByName(cityComboBox, editingAd.getCity());

        try {
            AdDetail detail = ApiClient.getAdDetails(editingAd.getId());
            descriptionField.setText(detail.getDescription());
        } catch (Exception e) {
            String fallback = editingAd.getDescription();
            descriptionField.setText(fallback != null ? fallback : "");
            statusLabel.setText("Showing cached description — full details unavailable for non-approved ads.");
        }
    }

    private <T> void selectByName(ComboBox<T> box, String name) {
        if (name == null) return;
        for (T item : box.getItems()) {
            String itemName = item instanceof Category ? ((Category) item).getName() : ((City) item).getName();
            if (itemName != null && itemName.equalsIgnoreCase(name)) {
                box.getSelectionModel().select(item);
                return;
            }
        }
    }

    /**
     * validates the form inputs and submits the updated ad data to the
     * backend. on success, the user is notified and returned to the my ads
     * view. validation errors are displayed on the status label.
     */
    @FXML
    private void handleSave() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String priceText = priceField.getText();
        Category category = categoryComboBox.getValue();
        City city = cityComboBox.getValue();

        if (title == null || title.isBlank()) {
            statusLabel.setText("Title cannot be empty.");
            return;
        }
        if (priceText != null && !priceText.isBlank()) {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    statusLabel.setText("Price must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Price must be a valid number.");
                return;
            }
        }

        try {
            ApiClient.updateAd(
                    editingAd.getId(),
                    title,
                    (description == null || description.isBlank()) ? null : description,
                    (priceText == null || priceText.isBlank()) ? null : priceText,
                    category != null ? category.getId() : null,
                    city != null ? city.getId() : null);

            Dialogs.info("Ad updated.");
            SceneManager.switchScene("/fxml/my-ads-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to save changes: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/my-ads-view.fxml");
    }
}