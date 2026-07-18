package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AdminController {

    // --- Pending ads tab ---
    @FXML private ListView<Ad> pendingListView;
    @FXML private TextArea reasonField;
    @FXML private Label statusLabel;

    // --- Categories tab ---
    @FXML private ListView<Category> categoriesListView;
    @FXML private TextField categoryNameField;
    @FXML private Label categoryStatusLabel;

    // --- Users tab ---
    @FXML private ListView<ApiClient.AdminUser> usersListView;
    @FXML private Label usersStatusLabel;

    @FXML
    public void initialize() {
        pendingListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                setText(empty || ad == null ? null
                        : ad.getTitle() + " - $" + ad.getPrice() + " (seller: " + ad.getSellerUsername() + ")");
            }
        });
        loadPendingAds();

        categoriesListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty || category == null ? null : category.getName());
            }
        });
        loadCategories();

        usersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ApiClient.AdminUser user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null
                        : user.fullName + " (@" + user.username + ") - " + user.status);
            }
        });
        loadUsers();
    }

    // ---------------------------------------------------------------
    // Pending ads
    // ---------------------------------------------------------------

    private void loadPendingAds() {
        try {
            List<Ad> pending = ApiClient.getPendingAds();
            pendingListView.setItems(FXCollections.observableArrayList(pending));
            statusLabel.setText(pending.isEmpty() ? "No ads pending review." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load pending ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefreshPending() {
        loadPendingAds();
    }

    @FXML
    private void handleApprove() {
        Ad selected = pendingListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        try {
            ApiClient.approveAd(selected.getId());
            statusLabel.setText("Ad approved.");
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not approve ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReject() {
        Ad selected = pendingListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }
        String reason = reasonField.getText();
        if (reason == null || reason.isBlank()) {
            statusLabel.setText("A rejection reason is required.");
            return;
        }
        try {
            ApiClient.rejectAd(selected.getId(), reason);
            statusLabel.setText("Ad rejected.");
            reasonField.clear();
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not reject ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Categories
    // ---------------------------------------------------------------

    private void loadCategories() {
        try {
            List<Category> categories = ApiClient.getCategories();
            categoriesListView.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            categoryStatusLabel.setText("Could not load categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCategory() {
        String name = categoryNameField.getText();
        if (name == null || name.isBlank()) {
            categoryStatusLabel.setText("Enter a category name first.");
            return;
        }
        try {
            ApiClient.createCategory(name);
            categoryNameField.clear();
            categoryStatusLabel.setText("Category added.");
            loadCategories();
        } catch (Exception e) {
            categoryStatusLabel.setText("Could not add category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRenameCategory() {
        Category selected = categoriesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            categoryStatusLabel.setText("Select a category first.");
            return;
        }
        String newName = categoryNameField.getText();
        if (newName == null || newName.isBlank()) {
            categoryStatusLabel.setText("Type the new name in the field, then click Rename Selected.");
            return;
        }
        try {
            ApiClient.updateCategory(selected.getId(), newName);
            categoryNameField.clear();
            categoryStatusLabel.setText("Category renamed.");
            loadCategories();
        } catch (Exception e) {
            categoryStatusLabel.setText("Could not rename category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoriesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            categoryStatusLabel.setText("Select a category first.");
            return;
        }
        if (!Dialogs.confirm("Delete category \"" + selected.getName() + "\"? "
                + "Ads already using it may be affected.")) {
            return;
        }
        try {
            ApiClient.deleteCategory(selected.getId());
            categoryStatusLabel.setText("Category deleted.");
            loadCategories();
        } catch (Exception e) {
            categoryStatusLabel.setText("Could not delete category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Users (block/unblock)
    // NOTE: the backend has no user-management endpoints yet (no UserController).
    // The User entity already has a UserStatus field, so this just needs the
    // backend teammate to expose GET /api/admin/users, PATCH .../block and
    // PATCH .../unblock. Until then these calls will show a clear error here.
    // ---------------------------------------------------------------

    private void loadUsers() {
        try {
            List<ApiClient.AdminUser> users = ApiClient.getUsers();
            usersListView.setItems(FXCollections.observableArrayList(users));
            usersStatusLabel.setText("");
        } catch (Exception e) {
            usersStatusLabel.setText("User management isn't available yet: " + e.getMessage()
                    + "\n(Ask your backend teammate to add GET /api/admin/users, "
                    + "PATCH /api/admin/users/{id}/block and .../unblock.)");
        }
    }

    @FXML
    private void handleRefreshUsers() {
        loadUsers();
    }

    @FXML
    private void handleBlockUser() {
        ApiClient.AdminUser selected = usersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersStatusLabel.setText("Select a user first.");
            return;
        }
        try {
            ApiClient.blockUser(selected.id);
            usersStatusLabel.setText("User blocked.");
            loadUsers();
        } catch (Exception e) {
            usersStatusLabel.setText("Could not block user: " + e.getMessage());
        }
    }

    @FXML
    private void handleUnblockUser() {
        ApiClient.AdminUser selected = usersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersStatusLabel.setText("Select a user first.");
            return;
        }
        try {
            ApiClient.unblockUser(selected.id);
            usersStatusLabel.setText("User unblocked.");
            loadUsers();
        } catch (Exception e) {
            usersStatusLabel.setText("Could not unblock user: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}
