package com.silkroad.ui;

import com.silkroad.api.ApiClient;
import com.silkroad.model.Ad;
import com.silkroad.model.AdDetail;
import com.silkroad.model.Rating;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

/**
 * controller for the ad detail view. shows full ad information,
 * handles image display, favorite toggling, messaging the seller,
 * rating, and owner-specific actions (edit/delete/mark sold).
 * also supports an admin view mode for inspecting any ad.
 */
public class AdDetailsController {

    @FXML private HBox imagesBox;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryCityLabel;
    @FXML private Label statusValueLabel;
    @FXML private Label rejectionReasonLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label sellerLabel;
    @FXML private Label sellerRatingLabel;
    @FXML private Label ownerNoteLabel;
    @FXML private VBox ratingsBox;
    @FXML private Label noRatingsLabel;
    @FXML private Button favoriteButton;
    @FXML private Button messageButton;
    @FXML private Button rateButton;
    @FXML private HBox ownerActionsBox;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button markSoldButton;
    @FXML private Label statusLabel;

    private AdDetail ad;
    private boolean isFavorite;
    private boolean isAdminView;

    /**
     * called by javafx after fxml loading. resolves whether the user is
     * viewing as admin, fetches the appropriate ad detail, and renders
     * the ui. if the current user owns the ad, the owner action buttons
     * are shown instead of buyer actions.
     */
    @FXML
    public void initialize() {
        isAdminView = SceneManager.isViewingAsAdmin();
        SceneManager.setViewingAsAdmin(false);

        Ad selected = SceneManager.getSelectedAd();
        if (selected == null) {
            statusLabel.setText("No ad selected.");
            hideAllActions();
            return;
        }

        try {
            ad = isAdminView
                    ? ApiClient.getAdminAdDetails(selected.getId())
                    : ApiClient.getAdDetails(selected.getId());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not load ad details: " + e.getMessage());
            hideAllActions();
            return;
        }

        render();
        loadImages();
        loadRatings();
        if (!ad.isSubmitter()) {
            loadFavoriteState();
        }
    }

    /**
     * populates all ui labels with ad data and switches between
     * buyer-facing actions and owner-facing actions based on the
     * submitter flag.
     */
    private void render() {
        titleLabel.setText(ad.getTitle());
        priceLabel.setText("$" + ad.getPrice());
        categoryCityLabel.setText(ad.getCategory() + " · " + ad.getCity());
        statusValueLabel.setText("Status: " + ad.getStatus());
        descriptionLabel.setText(ad.getDescription());

        if (ad.getRejectionReason() != null && !ad.getRejectionReason().isBlank()) {
            rejectionReasonLabel.setText("Rejection reason: " + ad.getRejectionReason());
        }

        sellerLabel.setText(ad.getSellerFullName() + " (@" + ad.getSellerUsername() + ") · " + ad.getSellerPhone());
        sellerLabel.setGraphic(UiComponents.avatar(ad.getSellerFullName(), 32));
        sellerLabel.setGraphicTextGap(10);

        Double avg = ad.getAverageRating();
        if (avg != null && avg > 0) {
            sellerRatingLabel.setText(String.format("Average rating: %.1f / 5", avg));
        } else {
            sellerRatingLabel.setText("No ratings yet.");
        }

        if (ad.isSubmitter()) {
            // This is the logged-in user's own ad: swap the buyer-facing actions
            // (favorite / message / rate) for owner-facing ones (edit / delete / mark sold).
            ownerNoteLabel.setText("This is your ad.");

            favoriteButton.setVisible(false);
            favoriteButton.setManaged(false);
            messageButton.setVisible(false);
            messageButton.setManaged(false);
            rateButton.setVisible(false);
            rateButton.setManaged(false);

            ownerActionsBox.setVisible(true);
            ownerActionsBox.setManaged(true);
            markSoldButton.setVisible(!"SOLD".equalsIgnoreCase(ad.getStatus()));
            markSoldButton.setManaged(!"SOLD".equalsIgnoreCase(ad.getStatus()));
        } else {
            ownerNoteLabel.setText("");
            ownerActionsBox.setVisible(false);
            ownerActionsBox.setManaged(false);
        }
    }

    private void hideAllActions() {
        favoriteButton.setVisible(false);
        favoriteButton.setManaged(false);
        messageButton.setVisible(false);
        messageButton.setManaged(false);
        rateButton.setVisible(false);
        rateButton.setManaged(false);
        ownerActionsBox.setVisible(false);
        ownerActionsBox.setManaged(false);
    }

    /**
     * loads images from the server and displays them as thumbnails.
     * clicking a thumbnail opens a full-size popup. broken images are
     * skipped silently to avoid blocking the rest of the page.
     */
    private void loadImages() {
        List<String> urls = ad.getImageUrls();
        if (urls == null || urls.isEmpty()) {
            return;
        }
        for (String url : urls) {
            try {
                byte[] bytes = ApiClient.getImageBytes(url);
                Image image = new Image(new ByteArrayInputStream(bytes));
                ImageView view = new ImageView(image);
                double fitHeight = 180;
                view.setFitHeight(fitHeight);
                view.setPreserveRatio(true);

                double renderedWidth = image.getHeight() > 0
                        ? fitHeight * (image.getWidth() / image.getHeight())
                        : fitHeight;

                Rectangle clip = new Rectangle(renderedWidth, fitHeight);
                clip.setArcWidth(14);
                clip.setArcHeight(14);
                view.setClip(clip);

                // Click a thumbnail to see it full-size.
                view.setCursor(Cursor.HAND);
                view.setOnMouseClicked(e -> showFullImage(image));

                imagesBox.getChildren().add(view);
            } catch (Exception e) {
                e.printStackTrace();
                // skip broken images rather than blocking the whole page
            }
        }
    }

    private void showFullImage(Image image) {
        ImageView fullView = new ImageView(image);
        fullView.setPreserveRatio(true);

        double maxDim = 800;
        if (image.getWidth() >= image.getHeight() && image.getWidth() > maxDim) {
            fullView.setFitWidth(maxDim);
        } else if (image.getHeight() > maxDim) {
            fullView.setFitHeight(maxDim);
        }

        StackPane pane = new StackPane(fullView);
        pane.setStyle("-fx-background-color: black;");
        pane.setPadding(new Insets(16));

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(ad != null ? ad.getTitle() : "Photo");
        popup.setScene(new Scene(pane));
        popup.show();
    }

    /**
     * loads the buyer ratings/reviews left on this ad and renders them
     * as a list of rows (star score + comment + reviewer username).
     * shows a "no reviews yet" placeholder when the list is empty.
     */
    private void loadRatings() {
        try {
            List<Rating> ratings = ApiClient.getAdvertisementRatings(ad.getId());
            ratingsBox.getChildren().clear();

            if (ratings == null || ratings.isEmpty()) {
                noRatingsLabel.setVisible(true);
                noRatingsLabel.setManaged(true);
                return;
            }

            noRatingsLabel.setVisible(false);
            noRatingsLabel.setManaged(false);

            for (Rating rating : ratings) {
                ratingsBox.getChildren().add(buildRatingRow(rating));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // don't block the rest of the page if reviews fail to load
        }
    }

    /**
     * builds a single review row: reviewer avatar/username, a star
     * representation of the score, and the optional comment text.
     */
    private VBox buildRatingRow(Rating rating) {
        Label header = new Label("@" + rating.getBuyerUsername() + " · " + stars(rating.getScore()));
        header.setGraphic(UiComponents.avatar(rating.getBuyerUsername(), 24));
        header.setGraphicTextGap(8);
        header.getStyleClass().add("muted-label");

        VBox row = new VBox(4, header);

        if (rating.getComment() != null && !rating.getComment().isBlank()) {
            Label commentLabel = new Label(rating.getComment());
            commentLabel.setWrapText(true);
            row.getChildren().add(commentLabel);
        }

        return row;
    }

    private String stars(Integer score) {
        int filled = score == null ? 0 : Math.max(0, Math.min(5, score));
        return "★".repeat(filled) + "☆".repeat(5 - filled);
    }

    private void loadFavoriteState() {
        try {
            List<Ad> favorites = ApiClient.getFavorites();
            isFavorite = favorites.stream().anyMatch(a -> a.getId().equals(ad.getId()));
            favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * adds or removes the current ad from the user's favorite list.
     */
    @FXML
    private void handleToggleFavorite() {
        try {
            if (isFavorite) {
                ApiClient.removeFavorite(ad.getId());
            } else {
                ApiClient.addFavorite(ad.getId());
            }
            isFavorite = !isFavorite;
            favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not update favorites: " + e.getMessage());
        }
    }

    /**
     * opens a dialog for the user to send an initial message to the seller.
     */
    @FXML
    private void handleMessageSeller() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Message Seller");
        dialog.setHeaderText("Send a message about \"" + ad.getTitle() + "\"");
        dialog.setContentText("Message:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            return;
        }

        try {
            ApiClient.sendMessage(ad.getId(), result.get());
            Dialogs.info("Message sent! You can continue the conversation from Messages.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not send message: " + e.getMessage());
        }
    }

    /**
     * opens a dialog to submit a numeric rating (1-5) and an optional comment
     * for the seller of this ad.
     */
    @FXML
    private void handleRateSeller() {
        TextInputDialog scoreDialog = new TextInputDialog();
        scoreDialog.setTitle("Rate Seller");
        scoreDialog.setHeaderText("Rate " + ad.getSellerFullName() + " for this ad (1-5)");
        scoreDialog.setContentText("Score:");

        Optional<String> scoreResult = scoreDialog.showAndWait();
        if (scoreResult.isEmpty() || scoreResult.get().isBlank()) {
            return;
        }

        int score;
        try {
            score = Integer.parseInt(scoreResult.get().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Score must be a whole number between 1 and 5.");
            return;
        }
        if (score < 1 || score > 5) {
            statusLabel.setText("Score must be between 1 and 5.");
            return;
        }

        TextInputDialog commentDialog = new TextInputDialog();
        commentDialog.setTitle("Rate Seller");
        commentDialog.setHeaderText("Optional comment");
        commentDialog.setContentText("Comment:");
        Optional<String> commentResult = commentDialog.showAndWait();
        String comment = commentResult.orElse(null);

        try {
            ApiClient.rateSeller(ad.getId(), score, comment);
            Dialogs.info("Thanks for rating the seller!");
            ad = ApiClient.getAdDetails(ad.getId());
            render();
            loadRatings();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not submit rating: " + e.getMessage());
        }
    }

    /**
     * navigates to the edit ad view for the current ad (owner only).
     */
    @FXML
    private void handleEditAd() {
        SceneManager.setSelectedAd(ad);
        SceneManager.switchScene("/fxml/edit-ad-view.fxml");
    }

    /**
     * deletes the current ad after confirmation (owner or admin only).
     */
    @FXML
    private void handleDeleteAd() {
        if (!Dialogs.confirm("Delete \"" + ad.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        try {
            ApiClient.deleteAd(ad.getId());
            Dialogs.info("Ad deleted.");
            SceneManager.switchScene("/fxml/my-ads-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not delete ad: " + e.getMessage());
        }
    }

    /**
     * marks the current ad as sold after confirmation (owner only).
     */
    @FXML
    private void handleMarkSoldAd() {
        if (!Dialogs.confirm("Mark \"" + ad.getTitle() + "\" as sold?")) {
            return;
        }
        try {
            ApiClient.markAdSold(ad.getId());
            ad.setStatus("SOLD");
            Dialogs.info("Marked as sold.");
            render();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not update status: " + e.getMessage());
        }
    }

    /**
     * returns to the previous scene (either home, my-ads, or admin panel).
     */
    @FXML
    private void goBack() {
        String target = SceneManager.getReturnScene();
        SceneManager.setReturnScene("/fxml/home-view.fxml");
        SceneManager.switchScene(target);
    }
}