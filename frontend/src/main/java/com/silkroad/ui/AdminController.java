package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * controller for the admin panel. provides tabs for a dashboard,
 * pending ads, browsing all ads, managing categories, and
 * blocking/unblocking users.
 */
public class AdminController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label pendingAdsCountLabel;
    @FXML
    private Label activeAdsCountLabel;
    @FXML
    private Label dashboardNoteLabel;
    @FXML
    private BarChart<String, Number> dashboardChart;

    @FXML
    private FlowPane pendingFlowPane;
    @FXML
    private Label statusLabel;

    @FXML
    private TextField allAdsSearchField;
    @FXML
    private FlowPane allAdsFlowPane;
    @FXML
    private Label allAdsStatusLabel;
    private List<Ad> allAdsCache = new ArrayList<>();

    @FXML
    private ListView<Category> categoriesListView;
    @FXML
    private TextField categoryNameField;
    @FXML
    private Label categoryStatusLabel;

    @FXML
    private ListView<ApiClient.AdminUser> usersListView;
    @FXML
    private Label usersStatusLabel;

    @FXML private BorderPane dashboardPane;
    @FXML private BorderPane pendingPane;
    @FXML private BorderPane allAdsPane;
    @FXML private BorderPane categoriesPane;
    @FXML private BorderPane usersPane;

    @FXML private Button navDashboardBtn;
    @FXML private Button navPendingBtn;
    @FXML private Button navAllAdsBtn;
    @FXML private Button navCategoriesBtn;
    @FXML private Button navUsersBtn;

    /**
     * called by javafx after fxml loading. initialises all admin tabs
     * by loading pending ads, all ads, categories, users, and the dashboard.
     */
    @FXML
    public void initialize() {
        loadPendingAds();
        loadAllAds();

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

        loadDashboard();
    }

    /**
     * opens the ad detail view in admin mode, so the admin can inspect
     * any ad regardless of its status. sets the return scene so that
     * the back button returns to the admin panel.
     */
    private void openAdDetails(Ad ad) {
        SceneManager.setSelectedAd(ad);
        SceneManager.setViewingAsAdmin(true);
        SceneManager.setReturnScene("/fxml/admin-view.fxml");
        SceneManager.switchScene("/fxml/ad-details-view.fxml");
    }

    // ---------------------------------------------------------------
    // dashboard
    // ---------------------------------------------------------------

    /**
     * fetches user, pending, and active ad counts from the backend
     * and updates the dashboard labels and bar chart. shows a note
     * if any of the required endpoints are unavailable.
     */
    private void loadDashboard() {
        StringBuilder note = new StringBuilder();

        int totalUsers = -1;
        int pendingCount = -1;
        int activeCount = -1;

        try {
            totalUsers = ApiClient.getUsers().size();
            totalUsersLabel.setText("Total users: " + totalUsers);
        } catch (Exception e) {
            totalUsersLabel.setText("Total users: —");
            note.append("User count needs the backend's /api/admin/users endpoint. ");
        }

        try {
            pendingCount = ApiClient.getPendingAds().size();
            pendingAdsCountLabel.setText("Pending ads: " + pendingCount);
        } catch (Exception e) {
            pendingAdsCountLabel.setText("Pending ads: —");
            note.append("Could not load pending ads. ");
        }

        try {
            activeCount = ApiClient.getAds().size();
            activeAdsCountLabel.setText("Active ads: " + activeCount);
        } catch (Exception e) {
            activeAdsCountLabel.setText("Active ads: —");
            note.append("Could not load active ads. ");
        }

        note.append("Rejected/sold/deleted ad counts and a reports count aren't shown yet "
                + "because the backend doesn't expose that data.");
        dashboardNoteLabel.setText(note.toString());

        renderDashboardChart(totalUsers, pendingCount, activeCount);
    }

    /**
     * populates the bar chart with the retrieved statistics.
     *
     * @param totalUsers   number of registered users
     * @param pendingCount number of pending ads
     * @param activeCount  number of active ads
     */
    private void renderDashboardChart(int totalUsers, int pendingCount, int activeCount) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Users", Math.max(totalUsers, 0)));
        series.getData().add(new XYChart.Data<>("Pending Ads", Math.max(pendingCount, 0)));
        series.getData().add(new XYChart.Data<>("Active Ads", Math.max(activeCount, 0)));

        dashboardChart.getData().clear();
        dashboardChart.getData().add(series);
    }

    @FXML
    private void handleRefreshDashboard() {
        loadDashboard();
    }

    // ---------------------------------------------------------------
    // pending ads
    // ---------------------------------------------------------------

    private void loadPendingAds() {
        try {
            List<Ad> pending = ApiClient.getPendingAds();
            renderPendingAds(pending);
            statusLabel.setText(pending.isEmpty() ? "No ads pending review." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load pending ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * builds a card for each pending ad with view, approve, reject, and delete buttons.
     *
     * @param ads the list of pending ads
     */
    private void renderPendingAds(List<Ad> ads) {
        pendingFlowPane.getChildren().clear();
        for (Ad ad : ads) {
            List<Node> actions = new ArrayList<>();

            Button viewButton = new Button("View Details");
            viewButton.setOnAction(e -> openAdDetails(ad));
            actions.add(viewButton);

            Button approveButton = new Button("Approve");
            approveButton.getStyleClass().add("primary-button");
            approveButton.setOnAction(e -> handleApprove(ad));
            actions.add(approveButton);

            Button rejectButton = new Button("Reject");
            rejectButton.getStyleClass().add("danger-button");
            rejectButton.setOnAction(e -> handleReject(ad));
            actions.add(rejectButton);

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> handleDeletePending(ad));
            actions.add(deleteButton);

            pendingFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, true, actions, () -> openAdDetails(ad)));
        }
    }

    @FXML
    private void handleRefreshPending() {
        loadPendingAds();
    }

    /**
     * approves the given ad, changing its status to active.
     *
     * @param ad the ad to approve
     */
    private void handleApprove(Ad ad) {
        try {
            ApiClient.approveAd(ad.getId());
            statusLabel.setText("Ad approved.");
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not approve ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * rejects the given ad and requires the admin to provide a rejection reason.
     *
     * @param ad the ad to reject
     */
    private void handleReject(Ad ad) {
        Optional<String> reason = Dialogs.prompt("Reject Ad",
                "Reject \"" + ad.getTitle() + "\"", "Rejection reason:");
        if (reason.isEmpty() || reason.get().isBlank()) {
            statusLabel.setText("A rejection reason is required.");
            return;
        }
        try {
            ApiClient.rejectAd(ad.getId(), reason.get());
            statusLabel.setText("Ad rejected.");
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not reject ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * deletes a pending ad immediately (admin-only action).
     *
     * @param ad the ad to delete
     */
    private void handleDeletePending(Ad ad) {
        if (!Dialogs.confirm("Delete \"" + ad.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        try {
            ApiClient.deleteAd(ad.getId());
            statusLabel.setText("Ad deleted.");
            loadPendingAds();
        } catch (Exception e) {
            statusLabel.setText("Could not delete ad. The backend currently only lets an ad's own "
                    + "seller delete it (needs an admin-delete permission): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * loads active ads and pending ads into a single cache for admin browsing.
     * rejected, sold, and deleted ads are not yet retrievable because the
     * backend does not expose an admin endpoint for them.
     */
    private void loadAllAds() {
        try {
            Map<Long, Ad> byId = new LinkedHashMap<>();
            for (Ad ad : ApiClient.getAds()) {
                byId.put(ad.getId(), ad);
            }
            try {
                for (Ad ad : ApiClient.getPendingAds()) {
                    byId.put(ad.getId(), ad);
                }
            } catch (Exception ignored) {
                // pending ads just won't be included in the combined list
            }
            allAdsCache = new ArrayList<>(byId.values());
            applyAllAdsFilter();
        } catch (Exception e) {
            allAdsStatusLabel.setText("Could not load ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * filters the cached ads by the search keyword and renders the result.
     * search is performed on both title and seller username.
     */
    private void applyAllAdsFilter() {
        String keyword = allAdsSearchField.getText();
        List<Ad> filtered = allAdsCache;
        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            filtered = allAdsCache.stream()
                    .filter(ad -> (ad.getTitle() != null && ad.getTitle().toLowerCase().contains(lower))
                            || (ad.getSellerUsername() != null && ad.getSellerUsername().toLowerCase().contains(lower)))
                    .toList();
        }
        renderAllAds(filtered);
        allAdsStatusLabel.setText(filtered.isEmpty()
                ? "No ads match."
                : "Showing approved + pending ads (" + filtered.size() + "). "
                + "Rejected/sold/deleted ads aren't available to admins yet.");
    }

    private void renderAllAds(List<Ad> ads) {
        allAdsFlowPane.getChildren().clear();
        for (Ad ad : ads) {
            List<Node> actions = new ArrayList<>();

            Button viewButton = new Button("View Details");
            viewButton.setOnAction(e -> openAdDetails(ad));
            actions.add(viewButton);

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> handleDeleteAllAdsAd(ad));
            actions.add(deleteButton);

            allAdsFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, true, actions, () -> openAdDetails(ad)));
        }
    }

    @FXML
    private void handleAllAdsSearch() {
        applyAllAdsFilter();
    }

    @FXML
    private void handleRefreshAllAds() {
        loadAllAds();
    }

    /**
     * deletes an ad from the 'all ads' view after confirmation.
     *
     * @param ad the ad to delete
     */
    private void handleDeleteAllAdsAd(Ad ad) {
        if (!Dialogs.confirm("Delete \"" + ad.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        try {
            ApiClient.deleteAd(ad.getId());
            allAdsStatusLabel.setText("Ad deleted.");
            loadAllAds();
        } catch (Exception e) {
            allAdsStatusLabel.setText("Could not delete ad. The backend currently only lets an ad's own "
                    + "seller delete it (needs an admin-delete permission): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // categories
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
    private void showDashboard() {
        setActivePane(dashboardPane, navDashboardBtn);
    }

    @FXML
    private void showPending() {
        setActivePane(pendingPane, navPendingBtn);
    }

    @FXML
    private void showAllAds() {
        setActivePane(allAdsPane, navAllAdsBtn);
    }

    @FXML
    private void showCategories() {
        setActivePane(categoriesPane, navCategoriesBtn);
    }

    @FXML
    private void showUsers() {
        setActivePane(usersPane, navUsersBtn);
    }

    private void setActivePane(BorderPane pane, Button navBtn) {
        BorderPane[] panes = { dashboardPane, pendingPane, allAdsPane, categoriesPane, usersPane };
        for (BorderPane p : panes) {
            boolean active = (p == pane);
            p.setVisible(active);
            p.setManaged(active);
        }

        Button[] navButtons = { navDashboardBtn, navPendingBtn, navAllAdsBtn, navCategoriesBtn, navUsersBtn };
        for (Button b : navButtons) {
            b.getStyleClass().remove("nav-item-active");
        }
        navBtn.getStyleClass().add("nav-item-active");
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}