package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Category;
import com.silkroad.model.City;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * controller for the create ad view. handles form input, image selection,
 * category and city dropdowns, and submission of a new ad to the backend.
 */
public class CreateAdController {
    @FXML
    private Label headerLabel;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<City> cityComboBox;
    @FXML
    private ComboBox<Category> subCategoryComboBox;
    @FXML
    private ListView<String> imagesListView;
    @FXML
    private Label statusLabel;

    private final List<Path> selectedImages = new ArrayList<>();

    /**
     * called by javafx after fxml loading. populates the category and city
     * dropdowns from the server and prepares the image list view.
     */
    @FXML
    public void initialize() {
        loadCategories();
        loadCities();
        imagesListView.setItems(FXCollections.observableArrayList());

        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadSubcategories(newVal));
    }

    private void loadCategories() {
        try {
            List<Category> categories = ApiClient.getCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load categories: " + e.getMessage());
        }
    }

    private void loadSubcategories(Category parent) {
        subCategoryComboBox.getItems().clear();
        subCategoryComboBox.setValue(null);

        if (parent == null || !parent.isHasChildren()) {
            subCategoryComboBox.setDisable(true);
            return;
        }

        try {
            List<Category> subcategories = ApiClient.getSubcategories(parent.getId());
            subCategoryComboBox.setItems(FXCollections.observableArrayList(subcategories));
            subCategoryComboBox.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load subcategories: " + e.getMessage());
        }
    }

    private void loadCities() {
        try {
            List<City> cities = ApiClient.getCities();
            cityComboBox.setItems(FXCollections.observableArrayList(cities));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load cities: " + e.getMessage());
        }
    }

    /**
     * opens a file chooser dialog that allows the user to select multiple
     * image files. the selected files are added to the list view and stored
     * for submission along with the ad data.
     */
    @FXML
    private void handleChooseImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose images for this ad");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        List<File> files = chooser.showOpenMultipleDialog(imagesListView.getScene().getWindow());
        if (files == null) return;

        ObservableList<String> names = imagesListView.getItems();
        for (File file : files) {
            selectedImages.add(file.toPath());
            names.add(file.getName());
        }
    }

    /**
     * validates all form fields (title, description, price, category, city)
     * and submits the ad creation request to the backend. on success, the
     * user is informed that the ad will be reviewed by an admin and then
     * navigated back to the home view. validation errors are displayed
     * on the status label.
     */
    @FXML
    private void handleSubmit() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String priceText = priceField.getText();
        Category selectedCategory = categoryComboBox.getValue();
        Category selectedSubCategory = subCategoryComboBox.getValue();
        City selectedCity = cityComboBox.getValue();

        if (isBlank(title) || isBlank(description) || isBlank(priceText)) {
            showError("Please fill in title, description, and price.");
            return;
        }

        if (selectedCategory == null) {
            showError("Please select a category.");
            return;
        }

        if (selectedCategory.isHasChildren() && selectedSubCategory == null) {
            showError("Please select a subcategory.");
            return;
        }

        Category categoryForAd = selectedSubCategory != null ? selectedSubCategory : selectedCategory;

        if (selectedCity == null) {
            showError("Please select a city.");
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

        try {
            ApiClient.createAd(title, description, priceText, selectedCategory.getId(), selectedCity.getId(),
                    selectedImages);
            Dialogs.info("Ad created! It will appear once an admin approves it.");
            SceneManager.switchScene("/fxml/home-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to create ad: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void showError(String message) {
        statusLabel.setText(message);
    }
}