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

public class EditAdController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label statusLabel;

    private Ad editingAd;

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

        // GET /api/ads/{id} only returns APPROVED ads, so this will fail for
        // pending/rejected/sold ads owned by the user (a backend gap — the
        // isSubmitter check exists server-side but isn't actually used to bypass
        // the status requirement). We fall back to what the summary already gave us.
        try {
            AdDetail detail = ApiClient.getAdDetails(editingAd.getId());
            descriptionField.setText(detail.getDescription());
        } catch (Exception e) {
            descriptionField.setText("");
            statusLabel.setText("Couldn't fetch the full description for a non-approved ad "
                    + "(backend only exposes GET /api/ads/{id} for approved ads). "
                    + "Please re-type the description if you're changing anything else.");
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
