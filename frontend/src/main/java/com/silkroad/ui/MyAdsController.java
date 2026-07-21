package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

public class MyAdsController {

    @FXML
    private FlowPane myAdsFlowPane;

    @FXML
    private Label statusLabel;

    /**
     * Initializes the view by retrieving and displaying all advertisements
     * posted by the currently authenticated user.
     * <p>
     * This method is automatically invoked by the JavaFX framework after the
     * FXML components have been loaded and injected into the controller.
     * </p>
     */
    @FXML
    public void initialize() {
        loadMyAds();
    }

    /**
     * Retrieves the user's advertisements from the backend service and updates
     * the interface accordingly.
     * <p>
     * On success, all ads are rendered in the flow pane. If no advertisements
     * are found, a message is displayed to inform the user. Any communication
     * or server errors are reported through the status label.
     * </p>
     */
    private void loadMyAds() {
        try {
            List<Ad> ads = ApiClient.getMyAds();
            renderAds(ads);
            statusLabel.setText(ads.isEmpty() ? "You haven't posted any ads yet." : "");
        } catch (Exception e) {
            statusLabel.setText("Could not load your ads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates and displays advertisement cards for the provided list of ads.
     * <p>
     * Each card is populated with actions that allow the owner to edit,
     * delete, or mark the advertisement as sold. The available actions depend
     * on the current status of the advertisement.
     * </p>
     *
     * @param ads advertisements belonging to the current user
     */
    private void renderAds(List<Ad> ads) {
        myAdsFlowPane.getChildren().clear();

        for (Ad ad : ads) {
            List<Node> actions = new ArrayList<>();

            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> openEditFor(ad));
            actions.add(editButton);

            if (!"SOLD".equalsIgnoreCase(ad.getStatus())) {
                Button markSoldButton = new Button("Mark Sold");
                markSoldButton.setOnAction(e -> handleMarkSold(ad));
                actions.add(markSoldButton);
            }

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> handleDelete(ad));
            actions.add(deleteButton);

            myAdsFlowPane.getChildren().add(
                    UiComponents.buildAdCard(ad, true, actions, () -> openEditFor(ad)));
        }
    }

    /**
     * Reloads the user's advertisements from the server and refreshes the view.
     * <p>
     * This action allows the user to synchronize the displayed data with the
     * latest state stored on the backend.
     * </p>
     */
    @FXML
    private void handleRefresh() {
        loadMyAds();
    }

    /**
     * Opens the advertisement editing screen for the selected advertisement.
     * <p>
     * The chosen advertisement is stored in the scene manager so that its
     * details can be accessed and modified by the edit view.
     * </p>
     *
     * @param selected the advertisement to edit
     */
    private void openEditFor(Ad selected) {
        if (selected == null) {
            statusLabel.setText("Select an ad first.");
            return;
        }

        SceneManager.setSelectedAd(selected);
        SceneManager.switchScene("/fxml/edit-ad-view.fxml");
    }

    /**
     * Marks the selected advertisement as sold after obtaining confirmation
     * from the user.
     * <p>
     * Upon successful completion, the advertisement list is refreshed so the
     * updated status is immediately reflected in the interface.
     * </p>
     *
     * @param selected the advertisement to update
     */
    private void handleMarkSold(Ad selected) {
        if (!Dialogs.confirm("Mark \"" + selected.getTitle() + "\" as sold?")) {
            return;
        }

        try {
            ApiClient.markAdSold(selected.getId());
            statusLabel.setText("Marked as sold.");
            loadMyAds();
        } catch (Exception e) {
            statusLabel.setText("Could not update status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permanently removes the selected advertisement after user confirmation.
     * <p>
     * Once deleted, the advertisement can no longer be recovered. The list of
     * advertisements is refreshed after a successful deletion.
     * </p>
     *
     * @param selected the advertisement to delete
     */
    private void handleDelete(Ad selected) {
        if (!Dialogs.confirm("Delete \"" + selected.getTitle() + "\"? This cannot be undone.")) {
            return;
        }

        try {
            ApiClient.deleteAd(selected.getId());
            statusLabel.setText("Ad deleted.");
            loadMyAds();
        } catch (Exception e) {
            statusLabel.setText("Could not delete ad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the user to the application's home page.
     * <p>
     * This action abandons the current management view and navigates back to
     * the main screen.
     * </p>
     */
    @FXML
    private void goBack() {
        SceneManager.switchScene("/fxml/home-view.fxml");
    }
}